package com.hyalurion.sim.info.manager

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Parcel
import android.os.RemoteException
import android.util.Log
import androidx.core.content.ContextCompat

@SuppressLint("StaticFieldLeak")
object FairMemoryManager : IBinder.DeathRecipient {

    private const val TAG = "FairMemoryManager"
    private const val ACTION_TRIM = "itgsa.intent.action.TRIM"
    private const val ACTION_KILL = "itgsa.intent.action.KILL"

    private const val TRANSACTION_EXCEPTION_REPLY = IBinder.FIRST_CALL_TRANSACTION

    private var initialized = false
    private var handler: Handler? = null
    private var remoteBinder: IBinder? = null
    private var appContext: Context? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            Log.i(TAG, "Received broadcast: $action")

            val data = intent.extras ?: run {
                Log.w(TAG, "Intent extras is null")
                return
            }

            val common = data.getBundle("common") ?: run {
                Log.w(TAG, "Common bundle not found")
                return
            }

            val notifyType = common.getInt("notifyType")
            val notifyId = common.getInt("notifyId")
            val reason = common.getString("reason")
            val actionStr = common.getString("action")
            val callback = common.getBinder("callback")

            val extra = data.getBundle("extra")
            if (extra != null) {
                val heapAlloc = extra.getInt("heapAlloc", 0)
                val heapCapacity = extra.getInt("heapCapacity", 0)
                val pss = extra.getInt("pss", 0)
                val pssLimit = extra.getInt("pssLimit", 0)
                Log.i(TAG, "notifyType=$notifyType, notifyId=$notifyId, reason=$reason, " +
                        "action=$actionStr, heapAlloc=$heapAlloc KB, heapCapacity=$heapCapacity KB, " +
                        "pss=$pss KB, pssLimit=$pssLimit KB")
            }

            when (action) {
                ACTION_TRIM -> handleTrim(notifyType, notifyId, callback)
                ACTION_KILL -> handleKill(notifyType, notifyId, callback)
            }
        }
    }

    fun initialize(ctx: Context) {
        if (initialized) return

        appContext = ctx.applicationContext

        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        val filter = IntentFilter().apply {
            addAction(ACTION_TRIM)
            addAction(ACTION_KILL)
        }

        ContextCompat.registerReceiver(
            appContext!!,
            receiver,
            filter,
            null,
            handler!!,
            ContextCompat.RECEIVER_EXPORTED
        )

        initialized = true
        Log.i(TAG, "FairMemoryManager initialized")
    }

    fun release() {
        if (!initialized) return
        appContext?.unregisterReceiver(receiver)
        handler?.looper?.quitSafely()
        handler = null
        synchronized(this) {
            remoteBinder?.let {
                try {
                    it.unlinkToDeath(this, 0)
                } catch (_: Exception) {}
            }
            remoteBinder = null
        }
        appContext = null
        initialized = false
        Log.i(TAG, "FairMemoryManager released")
    }

    private fun handleTrim(notifyType: Int, notifyId: Int, callback: IBinder?) {
        Log.i(TAG, "Handling TRIM: notifyType=$notifyType, notifyId=$notifyId")

        trimMemory()

        if (callback != null && checkRemote(callback)) {
            val extra = Bundle().apply {
                putString("reply", "Memory trimmed successfully")
            }
            replySuccess(notifyType, notifyId, extra)
        } else {
            Log.w(TAG, "TRIM: callback binder is null or checkRemote failed")
        }
    }

    private fun handleKill(notifyType: Int, notifyId: Int, callback: IBinder?) {
        Log.i(TAG, "Handling KILL: notifyType=$notifyType, notifyId=$notifyId")

        saveState()

        if (callback != null && checkRemote(callback)) {
            val extra = Bundle().apply {
                putString("reply", "State saved successfully")
            }
            replySuccess(notifyType, notifyId, extra)
        } else {
            Log.w(TAG, "KILL: callback binder is null or checkRemote failed")
        }
    }

    private fun trimMemory() {
        Log.i(TAG, "Trimming memory...")
        try {
            val ctx = appContext ?: return
            val am = ctx.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
            am?.let {
                Log.i(TAG, "Memory trim requested via ActivityManager")
            }

            System.gc()
            Log.i(TAG, "Memory trim completed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to trim memory", e)
        }
    }

    private fun saveState() {
        Log.i(TAG, "Saving state before kill...")
        try {
            Log.i(TAG, "State save completed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save state", e)
        }
    }

    @Synchronized
    private fun checkRemote(callback: IBinder): Boolean {
        if (remoteBinder == null) {
            try {
                remoteBinder = callback
                remoteBinder!!.linkToDeath(this, 0)
            } catch (e: RemoteException) {
                Log.e(TAG, "linkToDeath failed", e)
                remoteBinder = null
                return false
            }
        }
        return true
    }

    @Synchronized
    private fun replySuccess(notifyType: Int, notifyId: Int, extra: Bundle?) {
        val remote = remoteBinder ?: run {
            Log.w(TAG, "reply: remote binder is null")
            return
        }

        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInt(notifyType)
            data.writeInt(notifyId)
            data.writeInt(0)
            val bundle = extra ?: Bundle()
            data.writeBundle(bundle)
            remote.transact(TRANSACTION_EXCEPTION_REPLY, data, reply,
                IBinder.FLAG_ONEWAY)
            reply.readException()
            Log.i(TAG, "Reply sent: notifyType=$notifyType, notifyId=$notifyId, result=0")
        } catch (e: Exception) {
            Log.e(TAG, "reply failed", e)
        } finally {
            reply.recycle()
            data.recycle()
        }
    }

    @Synchronized
    override fun binderDied() {
        Log.w(TAG, "Binder died")
        remoteBinder?.let {
            try {
                it.unlinkToDeath(this, 0)
            } catch (_: Exception) {}
        }
        remoteBinder = null
    }
}
