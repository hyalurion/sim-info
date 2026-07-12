package com.hyalurion.sim.info.data

data class SimInfo(
    val carrierName: String? = null,
    val countryIso: String? = null,
    val countryName: String? = null,
    val mcc: String? = null,
    val mnc: String? = null,
    val networkType: String? = null,
    val isVoipAvailable: Boolean? = null,
    val phoneNumber: String? = null,
    val isNetworkRoaming: Boolean? = null,
    val dataActivity: String? = null,
    val dataState: String? = null,
    val signalStrength: String? = null,
    val cellIdentity: CellIdentityInfo? = null,
    val neighboringCellInfo: String? = null,
    val simState: String? = null
)

data class CellIdentityInfo(
    val lac: String? = null,
    val tac: String? = null,
    val ci: String? = null,
    val cid: String? = null,
    val nci: String? = null,
    val pci: String? = null,
    val psc: String? = null,
    val earfcn: String? = null,
    val uarfcn: String? = null,
    val nrarfcn: String? = null,
    val type: String? = null
)
