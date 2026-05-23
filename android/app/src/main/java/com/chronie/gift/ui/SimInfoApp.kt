package com.hyalurion.sim.info.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hyalurion.sim.info.data.LanguageManager
import com.hyalurion.sim.info.data.ThemeManager
import com.hyalurion.sim.info.ui.screens.LicensesScreen
import com.hyalurion.sim.info.ui.screens.SettingsScreen
import com.hyalurion.sim.info.ui.screens.SimInfoScreen
import com.hyalurion.sim.info.ui.theme.ColorSchemeMode
import com.hyalurion.sim.info.ui.theme.SimInfoTheme
import com.hyalurion.sim.info.ui.theme.ThemeController
import com.hyalurion.sim.info.ui.theme.LanguageController
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SimInfoApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    
    // Permission state - must be outside NavHost
    var hasPhonePermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Permission launcher - must be outside NavHost to access ActivityResultRegistryOwner
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPhonePermission = permissions[Manifest.permission.READ_PHONE_STATE] == true ||
                            permissions[Manifest.permission.READ_PHONE_NUMBERS] == true
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    val requestPermission = {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_PHONE_NUMBERS,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }
    
    // Theme management
    val themeManager = remember { ThemeManager(context) }
    val savedTheme = themeManager.getSavedTheme()
    val initialThemeMode = when (savedTheme) {
        "light" -> ColorSchemeMode.Light
        "dark" -> ColorSchemeMode.Dark
        else -> ColorSchemeMode.System
    }
    
    val controller = remember {
        ThemeController(initialThemeMode)
    }
    
    // Language management
    val languageManager = remember { LanguageManager(context) }
    val savedLanguage = languageManager.getSavedLanguage()
    val currentLanguageCode = remember(savedLanguage) { mutableStateOf(savedLanguage) }
    
    val languageController = remember {
        LanguageController(savedLanguage)
    }

    // Update theme mode callback
    val updateThemeMode = { newThemeMode: String ->
        val colorSchemeMode = when (newThemeMode) {
            "light" -> ColorSchemeMode.Light
            "dark" -> ColorSchemeMode.Dark
            else -> ColorSchemeMode.System
        }
        controller.colorSchemeMode = colorSchemeMode
        themeManager.saveTheme(newThemeMode)
    }

    // Update language callback
    val updateLanguageCode = { newLanguageCode: String? ->
        languageController.languageCode = newLanguageCode
        currentLanguageCode.value = newLanguageCode
        if (newLanguageCode == null) {
            languageManager.clearLanguage()
        } else {
            languageManager.saveLanguage(newLanguageCode)
        }
        languageManager.applyLanguage(newLanguageCode)
    }
    
    SimInfoTheme(controller = controller, languageController = languageController) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.background)
        ) {
            NavHost(
                navController = navController,
                startDestination = "sim_info",
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300)
                    )
                },
                popEnterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(300)
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(300)
                    )
                }
            ) {
                composable("sim_info") {
                    SimInfoScreen(
                        hasPermission = hasPhonePermission,
                        onRequestPermission = requestPermission,
                        onNavigateToSettings = { navController.navigate("settings") }
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        onThemeUpdated = updateThemeMode,
                        onLanguageUpdated = updateLanguageCode,
                        currentLanguageCode = currentLanguageCode.value,
                        onNavigateToLicenses = { navController.navigate("licenses") },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("licenses") {
                    LicensesScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
