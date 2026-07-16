package com.hyalurion.sim.info.data

import java.util.Locale

object CountryPresets {
    data class CountryInfo(
        val code: String,
        val displayName: String
    )

    private val countryCodes = listOf(
        "CN", "HK", "MO", "TW", "JP", "KR", "US", "GB", "DE", "FR",
        "IT", "ES", "PT", "RU", "IN", "AU", "NZ", "SG", "MY", "TH",
        "VN", "ID", "PH", "CA", "MX", "BR", "AR", "ZA"
    )

    val countries: List<CountryInfo> by lazy {
        countryCodes.map { code ->
            val name = try {
                Locale.Builder().setRegion(code).build().displayCountry
            } catch (_: Exception) {
                code
            }
            CountryInfo(code, name.ifBlank { code })
        }.sortedBy { it.displayName }
    }
}
