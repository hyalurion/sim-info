package com.hyalurion.sim.info.data

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import android.content.SharedPreferences
import java.util.Locale

class LanguageManager(private val context: Context) {
    companion object {
        private const val LANGUAGE_KEY = "preferred_language"
        private const val PREF_NAME = "app_preferences"
    }

    // Get SharedPreferences instance
    private fun getPreferences(): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Save language setting
    fun saveLanguage(languageCode: String) {
        val preferences = getPreferences()
        preferences.edit().putString(LANGUAGE_KEY, languageCode).apply()
    }

    // Clear language setting to follow system language
    fun clearLanguage() {
        val preferences = getPreferences()
        preferences.edit().remove(LANGUAGE_KEY).apply()
    }

    // Get saved language setting, returns null if not set (follow system language)
    fun getSavedLanguage(): String? {
        val preferences = getPreferences()
        return preferences.getString(LANGUAGE_KEY, null)
    }

    // Apply language setting
    fun applyLanguage(languageCode: String?) {
        val locale = if (languageCode == null) {
            // Use system default language from system resources
            val systemConfig = Resources.getSystem().configuration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                systemConfig.locales[0]
            } else {
                @Suppress("DEPRECATION")
                systemConfig.locale
            }
        } else {
            when (languageCode) {
                "zh-CN" -> Locale.SIMPLIFIED_CHINESE
                "zh-TW" -> Locale.TRADITIONAL_CHINESE
                "en" -> Locale.ENGLISH
                "ja" -> Locale.JAPANESE
                else -> Locale.SIMPLIFIED_CHINESE
            }
        }

        Locale.setDefault(locale)

        // Update current context configuration
        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }
        
        // Update resources configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            configuration.setLocales(localeList)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
        
        // Update application context configuration
        val appContext = context.applicationContext
        val appConfig = Configuration(appContext.resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val localeList = LocaleList(locale)
            appConfig.setLocales(localeList)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            appConfig.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            appConfig.locale = locale
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            appContext.createConfigurationContext(appConfig)
        } else {
            @Suppress("DEPRECATION")
            appContext.resources.updateConfiguration(appConfig, appContext.resources.displayMetrics)
        }
    }

    // Get current resources language setting
    fun getCurrentLanguage(): String? {
        val savedLanguage = getSavedLanguage()
        if (savedLanguage != null) {
            return savedLanguage
        }

        // If no saved language, return null to indicate following system language
        return null
    }
}