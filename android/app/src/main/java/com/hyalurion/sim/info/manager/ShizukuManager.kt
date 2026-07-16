package com.hyalurion.sim.info.manager

import android.content.pm.PackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku

object ShizukuManager {

    enum class ShizukuState {
        NOT_INSTALLED,
        NOT_RUNNING,
        WAITING_PERMISSION,
        READY
    }

    private val _state = MutableStateFlow(ShizukuState.NOT_INSTALLED)
    val state: StateFlow<ShizukuState> = _state.asStateFlow()

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            _state.value = ShizukuState.READY
        }
    }

    private val binderListener = Shizuku.OnBinderReceivedListener {
        refreshState()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        _state.value = ShizukuState.NOT_RUNNING
    }

    fun init() {
        Shizuku.addBinderReceivedListener(binderListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionListener)
        refreshState()
    }

    fun destroy() {
        Shizuku.removeBinderReceivedListener(binderListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(permissionListener)
    }

    private fun refreshState() {
        _state.value = if (Shizuku.getBinder() == null) {
            ShizukuState.NOT_RUNNING
        } else if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            ShizukuState.READY
        } else {
            if (Shizuku.shouldShowRequestPermissionRationale()) {
                ShizukuState.WAITING_PERMISSION
            } else {
                Shizuku.requestPermission(0)
                ShizukuState.WAITING_PERMISSION
            }
        }
    }

    fun requestPermission() {
        if (Shizuku.getBinder() != null) {
            Shizuku.requestPermission(0)
        }
    }
}
