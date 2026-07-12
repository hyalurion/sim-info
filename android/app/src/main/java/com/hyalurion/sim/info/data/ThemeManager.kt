package com.hyalurion.sim.info.data

import android.content.Context
import android.content.SharedPreferences

class ThemeManager(private val context: Context) {
    companion object {
        private const val THEME_KEY = "theme_mode"
        private const val DEFAULT_THEME = "auto"
    }

    // Save theme setting
    fun saveTheme(themeMode: String) {
        val preferences: SharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        preferences.edit().putString(THEME_KEY, themeMode).apply()
    }

    // Get saved theme setting
    fun getSavedTheme(): String {
        val preferences: SharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return preferences.getString(THEME_KEY, DEFAULT_THEME) ?: DEFAULT_THEME
    }

    // Get current theme mode
    fun getCurrentTheme(): String {
        // Since Compose theme system is managed by SimInfoTheme, here we only return the saved theme setting
        return getSavedTheme()
    }
}