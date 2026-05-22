package com.hyalurion.sim.info.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.CellIdentityNr
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellInfoWcdma
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.text.TextUtils
import java.util.Locale

class SimInfoManager(private val context: Context) {

    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

    fun getSimInfoList(): List<SimInfo> {
        val simInfoList = mutableListOf<SimInfo>()

        if (!hasSimCard()) {
            return simInfoList
        }

        val subscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
        if (subscriptionInfoList.isNullOrEmpty()) {
            simInfoList.add(getDefaultSimInfo())
        } else {
            for (subscriptionInfo in subscriptionInfoList) {
                simInfoList.add(getSimInfoFromSubscription(subscriptionInfo))
            }
        }

        return simInfoList
    }

    private fun hasSimCard(): Boolean {
        return telephonyManager.simState == TelephonyManager.SIM_STATE_READY
    }

    private fun getDefaultSimInfo(): SimInfo {
        val countryIso = telephonyManager.networkCountryIso?.takeIf { it.isNotBlank() }?.uppercase()

        return SimInfo(
            carrierName = telephonyManager.networkOperatorName?.takeIf { it.isNotBlank() },
            countryIso = countryIso,
            countryName = getCountryName(countryIso),
            mcc = getMcc(),
            mnc = getMnc(),
            networkType = getNetworkTypeName(),
            isVoipAvailable = isVoipAvailable(),
            phoneNumber = getPhoneNumber(),
            isNetworkRoaming = getIsNetworkRoaming(),
            dataActivity = getDataActivity(),
            dataState = getDataState(),
            signalStrength = getSignalStrength(),
            cellIdentity = getCellIdentity(),
            neighboringCellInfo = getNeighboringCellInfo(),
            simState = getSimStateString(telephonyManager.simState)
        )
    }

    private fun getSimInfoFromSubscription(subscriptionInfo: SubscriptionInfo): SimInfo {
        val subscriptionId = subscriptionInfo.subscriptionId
        val subscriptionTelephonyManager = telephonyManager.createForSubscriptionId(subscriptionId)
        val countryIso = subscriptionInfo.countryIso?.takeIf { it.isNotBlank() }?.uppercase()

        return SimInfo(
            carrierName = subscriptionInfo.carrierName?.toString()?.takeIf { it.isNotBlank() },
            countryIso = countryIso,
            countryName = getCountryName(countryIso),
            mcc = subscriptionInfo.mcc?.toString(),
            mnc = formatMnc(subscriptionInfo.mnc),
            networkType = getNetworkTypeName(subscriptionTelephonyManager),
            isVoipAvailable = isVoipAvailable(subscriptionTelephonyManager),
            phoneNumber = getPhoneNumber(subscriptionTelephonyManager, subscriptionInfo),
            isNetworkRoaming = getIsNetworkRoaming(subscriptionTelephonyManager),
            dataActivity = getDataActivity(subscriptionTelephonyManager),
            dataState = getDataState(subscriptionTelephonyManager),
            signalStrength = getSignalStrength(subscriptionTelephonyManager),
            cellIdentity = getCellIdentity(subscriptionTelephonyManager),
            neighboringCellInfo = getNeighboringCellInfo(subscriptionTelephonyManager),
            simState = getSimStateString(telephonyManager.simState)
        )
    }

    private fun getMcc(): String? {
        return try {
            val operator = telephonyManager.networkOperator
            if (!TextUtils.isEmpty(operator) && operator.length >= 3) {
                operator.substring(0, 3)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun getMnc(): String? {
        return try {
            val operator = telephonyManager.networkOperator
            if (!TextUtils.isEmpty(operator) && operator.length > 3) {
                val mnc = operator.substring(3)
                formatMncString(mnc)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Format MNC to standard 2 or 3 digit representation.
     * MNC should always be displayed with leading zeros (e.g., "02" not "2").
     */
    private fun formatMnc(mncInt: Int): String {
        val mncStr = mncInt.toString()
        return formatMncString(mncStr)
    }

    private fun formatMncString(mnc: String): String {
        // MNC is typically 2 or 3 digits. Pad to 2 digits if single digit.
        return when {
            mnc.length == 1 -> "0$mnc"  // "2" -> "02"
            else -> mnc                  // "02" or "011" stays as-is
        }
    }

    private fun getCountryName(isoCode: String?): String? {
        if (isoCode.isNullOrBlank()) return null
        
        return try {
            val locale = Locale("", isoCode)
            locale.displayCountry
        } catch (e: Exception) {
            isoCode // Fallback to ISO code if conversion fails
        }
    }

    private fun getNetworkTypeName(tm: TelephonyManager = telephonyManager): String? {
        return try {
            when (tm.dataNetworkType) {
                TelephonyManager.NETWORK_TYPE_UNKNOWN -> null
                TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
                TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
                TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
                TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
                TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO rev. 0"
                TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO rev. A"
                TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
                TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
                TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
                TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
                TelephonyManager.NETWORK_TYPE_IDEN -> "iDEN"
                TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO rev. B"
                TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
                TelephonyManager.NETWORK_TYPE_EHRPD -> "eHRPD"
                TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
                TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
                TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "TD-SCDMA"
                TelephonyManager.NETWORK_TYPE_IWLAN -> "IWLAN"
                TelephonyManager.NETWORK_TYPE_NR -> "5G NR"
                else -> tm.dataNetworkType.toString()
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun isVoipAvailable(tm: TelephonyManager = telephonyManager): Boolean {
        return try {
            tm.isVoiceCapable
        } catch (e: Exception) {
            false
        }
    }

    private fun getPhoneNumber(tm: TelephonyManager = telephonyManager, info: SubscriptionInfo? = null): String? {
        return try {
            if (!hasPhoneStatePermission()) return null

            var number: String? = null

            // Try to get from SubscriptionInfo first (API 29+)
            if (info != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                number = try {
                    @Suppress("DEPRECATION")
                    info.number?.takeIf { it.isNotBlank() && it != "unknown" }
                } catch (e: Exception) { null }
            }

            // Fallback to line1Number
            if (number.isNullOrBlank()) {
                number = try {
                    @Suppress("DEPRECATION")
                    tm.line1Number?.takeIf { it.isNotBlank() && it != "unknown" }
                } catch (e: Exception) { null }
            }

            number
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    // Network roaming status - only requires READ_PHONE_STATE
    private fun getIsNetworkRoaming(tm: TelephonyManager = telephonyManager): Boolean? {
        return try {
            if (!hasPhoneStatePermission()) return null
            tm.isNetworkRoaming
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    // Data activity direction - only requires READ_PHONE_STATE
    private fun getDataActivity(tm: TelephonyManager = telephonyManager): String? {
        return try {
            if (!hasPhoneStatePermission()) return null
            when (tm.dataActivity) {
                TelephonyManager.DATA_ACTIVITY_NONE -> "None"
                TelephonyManager.DATA_ACTIVITY_IN -> "In"
                TelephonyManager.DATA_ACTIVITY_OUT -> "Out"
                TelephonyManager.DATA_ACTIVITY_INOUT -> "In/Out"
                TelephonyManager.DATA_ACTIVITY_DORMANT -> "Dormant"
                else -> null
            }
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    // Data connection state - only requires READ_PHONE_STATE
    private fun getDataState(tm: TelephonyManager = telephonyManager): String? {
        return try {
            if (!hasPhoneStatePermission()) return null
            when (tm.dataState) {
                TelephonyManager.DATA_DISCONNECTED -> "Disconnected"
                TelephonyManager.DATA_CONNECTING -> "Connecting"
                TelephonyManager.DATA_CONNECTED -> "Connected"
                TelephonyManager.DATA_SUSPENDED -> "Suspended"
                else -> null
            }
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    // Signal strength - requires READ_PHONE_STATE
    private fun getSignalStrength(tm: TelephonyManager = telephonyManager): String? {
        return try {
            if (!hasPhoneStatePermission()) return null

            val signalStrength = tm.signalStrength ?: return null
            val level = signalStrength.level // 0-4
            val dbm = signalStrength.cellSignalStrengths.firstOrNull()?.dbm
            if (dbm != null) {
                "$dbm dBm (Level $level/4)"
            } else {
                "Level $level/4"
            }
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    // Cell identity - requires ACCESS_FINE_LOCATION on Android 10+
    private fun getCellIdentity(tm: TelephonyManager = telephonyManager): CellIdentityInfo? {
        return try {
            if (!hasLocationPermission()) return null

            val cellInfoList = tm.allCellInfo ?: return null
            val servingCell = cellInfoList.firstOrNull { it.isRegistered } ?: cellInfoList.firstOrNull()
                ?: return null

            parseCellIdentity(servingCell)
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun parseCellIdentity(cellInfo: CellInfo): CellIdentityInfo? {
        return try {
            when (cellInfo) {
                is CellInfoLte -> {
                    val identity = cellInfo.cellIdentity
                    CellIdentityInfo(
                        tac = identity.tac.toString(),
                        ci = identity.ci.toString(),
                        pci = identity.pci.toString(),
                        earfcn = identity.earfcn.toString(),
                        type = "LTE"
                    )
                }
                is CellInfoGsm -> {
                    val identity = cellInfo.cellIdentity
                    CellIdentityInfo(
                        lac = identity.lac.toString(),
                        cid = identity.cid.toString(),
                        type = "GSM"
                    )
                }
                is CellInfoWcdma -> {
                    val identity = cellInfo.cellIdentity
                    CellIdentityInfo(
                        lac = identity.lac.toString(),
                        cid = identity.cid.toString(),
                        psc = identity.psc.toString(),
                        uarfcn = identity.uarfcn.toString(),
                        type = "WCDMA"
                    )
                }
                is CellInfoNr -> {
                    val identity = cellInfo.cellIdentity as? CellIdentityNr
                    identity?.let {
                        CellIdentityInfo(
                            tac = it.tac.toString(),
                            nci = it.nci.toString(),
                            pci = it.pci.toString(),
                            nrarfcn = it.nrarfcn.toString(),
                            type = "NR"
                        )
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Neighboring cell info from allCellInfo - requires ACCESS_FINE_LOCATION
    private fun getNeighboringCellInfo(tm: TelephonyManager = telephonyManager): String? {
        return try {
            if (!hasLocationPermission()) return null

            val cellInfoList = tm.allCellInfo ?: return null
            // Filter non-serving cells as neighboring cells
            val neighbors = cellInfoList.filter { !it.isRegistered }
            if (neighbors.isEmpty()) return null

            neighbors.mapNotNull { cellInfo ->
                parseCellIdentity(cellInfo)?.let { info ->
                    val lacTac = info.lac ?: info.tac
                    val ciCid = info.ci ?: info.cid ?: info.nci
                    val type = info.type
                    val parts = mutableListOf<String>()
                    type?.let { parts.add(it) }
                    lacTac?.let { parts.add("LAC/TAC:$it") }
                    ciCid?.let { parts.add("CI/CID:$it") }
                    parts.joinToString(" ")
                }
            }.joinToString("; ").takeIf { it.isNotBlank() }
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun hasPhoneStatePermission(): Boolean {
        return context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED ||
               context.checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasLocationPermission(): Boolean {
        return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun getSimStateString(state: Int): String {
        return when (state) {
            TelephonyManager.SIM_STATE_ABSENT -> "Absent"
            TelephonyManager.SIM_STATE_PIN_REQUIRED -> "PIN Required"
            TelephonyManager.SIM_STATE_PUK_REQUIRED -> "PUK Required"
            TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Network Locked"
            TelephonyManager.SIM_STATE_READY -> "Ready"
            TelephonyManager.SIM_STATE_NOT_READY -> "Not Ready"
            TelephonyManager.SIM_STATE_PERM_DISABLED -> "Permanently Disabled"
            TelephonyManager.SIM_STATE_CARD_IO_ERROR -> "Card IO Error"
            TelephonyManager.SIM_STATE_CARD_RESTRICTED -> "Card Restricted"
            else -> "Unknown ($state)"
        }
    }
}
