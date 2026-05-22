package com.hyalurion.sim.info.ui.theme

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import top.yukonga.miuix.kmp.theme.Colors
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import java.util.Locale

val LocalLocale = staticCompositionLocalOf<Locale> { Locale.ENGLISH }

internal val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryColor,
    secondary = PrimaryLight,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkTextColor,
    onSecondary = DarkTextColor,
    onBackground = DarkTextColor,
    onSurface = DarkTextColor,
)

internal val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    secondary = PrimaryLight,
    background = BackgroundWhite,
    surface = BackgroundWhite,
    onPrimary = TextColor,
    onSecondary = TextColor,
    onBackground = TextColor,
    onSurface = TextColor,
)

@Composable
fun GiftTheme(
    themeMode: String = "auto", // add themeMode parameter, support "light", "dark", "auto"
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // disable dynamic color, use custom theme
    content: @Composable () -> Unit
) {
    // let Compose decide whether to use dark theme based on themeMode
    val darkTheme = when (themeMode) {
        "dark" -> true
        "light" -> false
        "auto" -> isSystemInDarkTheme()
        else -> isSystemInDarkTheme()
    }
    
    val colors = if (darkTheme) {
        top.yukonga.miuix.kmp.theme.darkColorScheme()
    } else {
        top.yukonga.miuix.kmp.theme.lightColorScheme()
    }

    MiuixTheme(colors = colors) {
        content()
    }
}

@Composable
fun GiftTheme(
    controller: ThemeController,
    content: @Composable () -> Unit
) {
    val colors = controller.currentColors()
    
    MiuixTheme(colors = colors) {
        content()
    }
}

@Composable
fun GiftTheme(
    controller: ThemeController,
    languageController: LanguageController,
    content: @Composable () -> Unit
) {
    val colors = controller.currentColors()
    val locale = languageController.currentLocale
    
    val context = LocalContext.current
    val configuration = Configuration(context.resources.configuration)
    configuration.setLocale(locale)
    val newContext = context.createConfigurationContext(configuration)
    
    CompositionLocalProvider(
        LocalContext provides newContext,
        LocalLocale provides locale
    ) {
        MiuixTheme(colors = colors) {
            content()
        }
    }
}