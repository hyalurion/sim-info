package com.hyalurion.sim.info.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.util.Locale

val LocalLocale = staticCompositionLocalOf<Locale> { Locale.ENGLISH }

@Composable
fun SimInfoTheme(
    themeMode: String = "auto",
    content: @Composable () -> Unit
) {
    val colorSchemeMode = when (themeMode) {
        "dark" -> ColorSchemeMode.Dark
        "light" -> ColorSchemeMode.Light
        else -> ColorSchemeMode.System
    }
    
    val darkTheme = when (colorSchemeMode) {
        ColorSchemeMode.Dark -> true
        ColorSchemeMode.Light -> false
        else -> isSystemInDarkTheme()
    }

    UpdateSystemUi(darkTheme)

    val controller = ThemeController(colorSchemeMode)
    MiuixTheme(controller = controller) {
        content()
    }
}

@Composable
fun Theme(
    controller: ThemeController,
    content: @Composable () -> Unit
) {
    val darkTheme = when (controller.colorSchemeMode) {
        ColorSchemeMode.Dark -> true
        ColorSchemeMode.Light -> false
        else -> isSystemInDarkTheme()
    }
    
    UpdateSystemUi(darkTheme)
    
    MiuixTheme(controller = controller) {
        content()
    }
}

@SuppressLint("LocalContextConfigurationRead")
@Composable
fun Theme(
    controller: ThemeController,
    languageController: LanguageController,
    content: @Composable () -> Unit
) {
    val locale = languageController.currentLocale
    val darkTheme = when (controller.colorSchemeMode) {
        ColorSchemeMode.Dark -> true
        ColorSchemeMode.Light -> false
        else -> isSystemInDarkTheme()
    }
    
    UpdateSystemUi(darkTheme)
    
    val context = LocalContext.current
    val configuration = Configuration(context.resources.configuration)
    configuration.setLocale(locale)
    val newContext = context.createConfigurationContext(configuration)
    
    CompositionLocalProvider(
        LocalContext provides newContext,
        LocalLocale provides locale
    ) {
        MiuixTheme(controller = controller) {
            content()
        }
    }
}

@Composable
private fun UpdateSystemUi(darkTheme: Boolean) {
    val view = LocalView.current
    LaunchedEffect(darkTheme) {
        val context = view.context
        if (context is android.app.Activity) {
            val windowInsetsController = WindowCompat.getInsetsController(context.window, view)
            windowInsetsController.isAppearanceLightStatusBars = !darkTheme
        }
    }
}