package com.hyalurion.sim.info.data

object PresetCarriers {
    data class CarrierPreset(
        val name: String,
        val displayName: String,
        val region: String
    )

    val presets = listOf(
        // China Operators
        CarrierPreset("中国移动", "China Mobile", "CN"),
        CarrierPreset("中国联通", "China Unicom", "CN"),
        CarrierPreset("中国电信", "China Telecom", "CN"),

        // Hong Kong Operators
        CarrierPreset("中國移動香港", "CMHK", "HK"),
        CarrierPreset("香港電訊", "HKT", "HK"),
        CarrierPreset("3香港", "3HK", "HK"),
        CarrierPreset("SmarTone", "SmarTone", "HK"),

        // Macau Operators
        CarrierPreset("澳門電訊", "CTM", "MO"),
        CarrierPreset("3澳門", "3 Macau", "MO"),

        // Taiwan Operators
        CarrierPreset("中華電信", "Chunghwa Telecom", "TW"),
        CarrierPreset("台灣大哥大", "Taiwan Mobile", "TW"),
        CarrierPreset("遠傳電信", "FarEasTone", "TW"),

        // Japan Operators
        CarrierPreset("NTT docomo", "NTT docomo", "JP"),
        CarrierPreset("au", "au by KDDI", "JP"),
        CarrierPreset("Softbank", "Softbank", "JP"),
        CarrierPreset("Rakuten", "Rakuten Mobile", "JP"),

        // Korea Operators
        CarrierPreset("SK Telecom", "SK Telecom", "KR"),
        CarrierPreset("KT", "KT Corporation", "KR"),
        CarrierPreset("LG U+", "LG U+", "KR"),

        // USA Operators
        CarrierPreset("AT&T", "AT&T", "US"),
        CarrierPreset("T-Mobile", "T-Mobile USA", "US"),
        CarrierPreset("Verizon", "Verizon", "US"),
        CarrierPreset("Sprint", "Sprint", "US"),

        // GB Operators
        CarrierPreset("EE", "EE", "GB"),
        CarrierPreset("O2", "O2 UK", "GB"),
        CarrierPreset("Three", "Three UK", "GB"),
        CarrierPreset("Vodafone", "Vodafone UK", "GB"),

        // Singapore Operators
        CarrierPreset("Singtel", "Singtel", "SG"),
        CarrierPreset("StarHub", "StarHub", "SG"),
        CarrierPreset("M1", "M1", "SG"),

        // Malaysia Operators
        CarrierPreset("Maxis", "Maxis", "MY"),
        CarrierPreset("Celcom", "Celcom", "MY"),
        CarrierPreset("Digi", "Digi", "MY"),
        CarrierPreset("U Mobile", "U Mobile", "MY"),

        // Thailand Operators
        CarrierPreset("AIS", "AIS", "TH"),
        CarrierPreset("DTAC", "DTAC", "TH"),
        CarrierPreset("True Move H", "True Move H", "TH"),

        // Vietnam Operators
        CarrierPreset("Viettel", "Viettel Mobile", "VN"),
        CarrierPreset("Vinaphone", "Vinaphone", "VN"),
        CarrierPreset("Mobifone", "Mobifone", "VN"),

        // Indonesia Operators
        CarrierPreset("Telkomsel", "Telkomsel", "ID"),
        CarrierPreset("Indosat", "Indosat Ooredoo", "ID"),
        CarrierPreset("XL Axiata", "XL Axiata", "ID"),

        // Philippines Operators
        CarrierPreset("Globe", "Globe Telecom", "PH"),
        CarrierPreset("Smart", "Smart Communications", "PH"),
        CarrierPreset("DITO", "DITO Telecommunity", "PH"),

        // India Operators
        CarrierPreset("Jio", "Reliance Jio", "IN"),
        CarrierPreset("Airtel", "Bharti Airtel", "IN"),
        CarrierPreset("Vi", "Vodafone Idea", "IN"),

        // Australia Operators
        CarrierPreset("Telstra", "Telstra", "AU"),
        CarrierPreset("Optus", "Optus", "AU"),
        CarrierPreset("Vodafone", "Vodafone AU", "AU"),

        // Canada Operators
        CarrierPreset("Bell", "Bell Mobility", "CA"),
        CarrierPreset("Rogers", "Rogers Wireless", "CA"),
        CarrierPreset("Telus", "Telus Mobility", "CA"),

        // Germany Operators
        CarrierPreset("Telekom", "T-Mobile DE", "DE"),
        CarrierPreset("Vodafone", "Vodafone DE", "DE"),
        CarrierPreset("O2", "O2 DE", "DE"),

        // French Operators
        CarrierPreset("Orange", "Orange FR", "FR"),
        CarrierPreset("SFR", "SFR", "FR"),
        CarrierPreset("Free", "Free Mobile", "FR"),
        CarrierPreset("Bouygues", "Bouygues Telecom", "FR"),

        // Italy Operators
        CarrierPreset("TIM", "Telecom Italia", "IT"),
        CarrierPreset("Vodafone", "Vodafone IT", "IT"),
        CarrierPreset("Wind Tre", "Wind Tre", "IT"),

        // Spanish Operators
        CarrierPreset("Movistar", "Movistar", "ES"),
        CarrierPreset("Vodafone", "Vodafone ES", "ES"),
        CarrierPreset("Orange", "Orange ES", "ES"),

        // Russian Operators
        CarrierPreset("MTS", "MTS", "RU"),
        CarrierPreset("MegaFon", "MegaFon", "RU"),
        CarrierPreset("Beeline", "Beeline", "RU"),

        // Brazil Operators
        CarrierPreset("Vivo", "Vivo", "BR"),
        CarrierPreset("Claro", "Claro", "BR"),
        CarrierPreset("TIM", "TIM Brasil", "BR"),

        // Custom Operators
        CarrierPreset("Custom", "", "")
    )
}
