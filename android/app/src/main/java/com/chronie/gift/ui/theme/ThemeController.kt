package com.hyalurion.sim.info.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import top.yukonga.miuix.kmp.theme.Colors
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

@Stable
class ThemeController(
    colorSchemeMode: ColorSchemeMode = ColorSchemeMode.System,
) {
    var colorSchemeMode: ColorSchemeMode by mutableStateOf(colorSchemeMode)

    @androidx.compose.runtime.Composable
    fun currentColors(): Colors {
        return when (colorSchemeMode) {
            ColorSchemeMode.System -> {
                val dark = isSystemInDarkTheme()
                if (dark) darkColorScheme() else lightColorScheme()
            }
            ColorSchemeMode.Light -> lightColorScheme()
            ColorSchemeMode.Dark -> darkColorScheme()
        }
    }
}
