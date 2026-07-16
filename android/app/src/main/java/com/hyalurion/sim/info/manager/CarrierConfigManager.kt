package com.hyalurion.sim.info.manager

import android.annotation.SuppressLint
import android.content.Context
import android.os.IBinder
import android.os.PersistableBundle
import android.telephony.SubscriptionManager
import android.util.Log
import com.hyalurion.sim.info.data.SimCardInfo
import rikka.shizuku.ShizukuBinderWrapper

object CarrierConfigManager {

    private const val TAG = "CarrierConfigManager"
    private const val KEY_SIM_COUNTRY_ISO_OVERRIDE = "sim_country_iso_override_string"
    private const val KEY_CARRIER_NAME_OVERRIDE_BOOL = "carrier_name_override_bool"
    private const val KEY_CARRIER_NAME_STRING = "carrier_name_string"
    private const val CALLING_PACKAGE = "com.hyalurion.sim.info"

    @SuppressLint("MissingPermission", "HardwareIds")
    fun getSimCards(context: Context): List<SimCardInfo> {
        val simCards = mutableListOf<SimCardInfo>()

        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE)
            as? SubscriptionManager ?: return simCards

        val subscriptionInfoList = try {
            subscriptionManager.activeSubscriptionInfoList
        } catch (_: SecurityException) {
            null
        }

        if (subscriptionInfoList.isNullOrEmpty()) return simCards

        for ((index, subscriptionInfo) in subscriptionInfoList.withIndex()) {
            val subId = subscriptionInfo.subscriptionId
            val carrierName = subscriptionInfo.carrierName?.toString()?.takeIf { it.isNotBlank() }
                ?: subscriptionInfo.displayName?.toString() ?: "SIM ${index + 1}"
            val config = getCurrentConfig(subId)
            simCards.add(
                SimCardInfo(
                    slot = subscriptionInfo.simSlotIndex + 1,
                    subId = subId,
                    carrierName = carrierName,
                    currentConfig = config
                )
            )
        }

        return simCards
    }

    private fun getCurrentConfig(subId: Int): Map<String, String> {
        return try {
            val binder = getCarrierConfigBinder() ?: return emptyMap()
            val carrierConfigLoader = getICarrierConfigLoader(binder) ?: return emptyMap()
            val config = getConfigForSubId(carrierConfigLoader, subId)
                ?: return emptyMap()

            val result = mutableMapOf<String, String>()

            config.getString(KEY_SIM_COUNTRY_ISO_OVERRIDE)?.let {
                if (it.isNotBlank()) result["country_code"] = it
            }

            if (config.getBoolean(KEY_CARRIER_NAME_OVERRIDE_BOOL, false)) {
                config.getString(KEY_CARRIER_NAME_STRING)?.let {
                    if (it.isNotBlank()) result["carrier_name"] = it
                }
            }

            result
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun setCarrierConfig(subId: Int, countryCode: String?, carrierName: String? = null): Result<Unit> {
        val bundle = PersistableBundle()

        if (!countryCode.isNullOrEmpty() && countryCode.length == 2) {
            bundle.putString(KEY_SIM_COUNTRY_ISO_OVERRIDE, countryCode.lowercase())
        }

        if (!carrierName.isNullOrEmpty()) {
            bundle.putBoolean(KEY_CARRIER_NAME_OVERRIDE_BOOL, true)
            bundle.putString(KEY_CARRIER_NAME_STRING, carrierName)
        }

        return overrideCarrierConfig(subId, bundle)
    }

    fun resetCarrierConfig(subId: Int): Result<Unit> {
        return overrideCarrierConfig(subId, null)
    }

    private fun overrideCarrierConfig(subId: Int, bundle: PersistableBundle?): Result<Unit> {
        val binder = try { getCarrierConfigBinder() } catch (e: Exception) {
            Log.e(TAG, "getCarrierConfigBinder threw", e)
            return Result.failure(e)
        }
        if (binder == null) {
            val msg = "binder is null (getCarrierConfigServiceRegisterer failed)"
            Log.e(TAG, msg)
            return Result.failure(NullPointerException(msg))
        }

        val carrierConfigLoader = try { getICarrierConfigLoader(binder) } catch (e: Exception) {
            Log.e(TAG, "getICarrierConfigLoader threw", e)
            return Result.failure(e)
        }
        if (carrierConfigLoader == null) {
            val msg = "getICarrierConfigLoader returned null"
            Log.e(TAG, msg)
            return Result.failure(NullPointerException(msg))
        }

        return callOverrideConfig(carrierConfigLoader, subId, bundle)
    }

    @SuppressLint("PrivateApi")
    private fun getCarrierConfigBinder(): IBinder? {
        return try {
            val telephonyFrameworkInitializerClass =
                Class.forName("android.telephony.TelephonyFrameworkInitializer")
            val telephonyServiceManager = telephonyFrameworkInitializerClass
                .getMethod("getTelephonyServiceManager")
                .invoke(null)

            // In AOSP, carrierConfigServiceRegisterer is a METHOD (getCarrierConfigServiceRegisterer)
            // that returns a ServiceRegisterer, NOT a field.
            // Nrfr uses Kotlin property syntax which translates to the getter method.
            val getRegistererMethod = telephonyServiceManager.javaClass.getMethod("getCarrierConfigServiceRegisterer")
            val registerer = getRegistererMethod.invoke(telephonyServiceManager)

            // ServiceRegisterer.get() returns the IBinder
            val registererClass = registerer.javaClass
            val getMethod = registererClass.getMethod("get")
            val binder = getMethod.invoke(registerer) as IBinder
            Log.i(TAG, "Got carrier config binder via getCarrierConfigServiceRegisterer(): $binder")
            ShizukuBinderWrapper(binder)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get carrier config binder", e)
            null
        }
    }

    @SuppressLint("PrivateApi")
    private fun getICarrierConfigLoader(binder: IBinder): Any? {
        return try {
            val stubClass = Class.forName("com.android.internal.telephony.ICarrierConfigLoader\$Stub")
            val asInterfaceMethod = stubClass.getMethod("asInterface", IBinder::class.java)
            val loader = asInterfaceMethod.invoke(null, binder)
            Log.i(TAG, "Got carrier config loader: $loader")
            loader
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get ICarrierConfigLoader", e)
            null
        }
    }

    @SuppressLint("PrivateApi")
    private fun getConfigForSubId(carrierConfigLoader: Any, subId: Int): PersistableBundle? {
        return try {
            val loaderClass = Class.forName("com.android.internal.telephony.ICarrierConfigLoader")
            val method = loaderClass.getMethod(
                "getConfigForSubId",
                Int::class.javaPrimitiveType,
                String::class.java
            )
            val result = method.invoke(carrierConfigLoader, subId, CALLING_PACKAGE) as? PersistableBundle
            Log.i(TAG, "getConfigForSubId($subId) = $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "getConfigForSubId failed for subId=$subId", e)
            null
        }
    }

    @SuppressLint("PrivateApi")
    private fun callOverrideConfig(carrierConfigLoader: Any, subId: Int, bundle: PersistableBundle?): Result<Unit> {
        return try {
            val loaderClass = Class.forName("com.android.internal.telephony.ICarrierConfigLoader")
            val method = loaderClass.getMethod(
                "overrideConfig",
                Int::class.javaPrimitiveType,
                PersistableBundle::class.java,
                Boolean::class.javaPrimitiveType
            )
            method.invoke(carrierConfigLoader, subId, bundle, true)
            Log.i(TAG, "overrideConfig success: subId=$subId, bundle=$bundle")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "callOverrideConfig failed for subId=$subId", e)
            Result.failure(e)
        }
    }
}
