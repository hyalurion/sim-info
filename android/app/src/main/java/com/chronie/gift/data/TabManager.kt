package com.hyalurion.sim.info.data

import android.content.Context
import android.content.SharedPreferences

class TabManager(private val context: Context) {
    companion object {
        private const val TAB_KEY = "selected_tab"
        private const val DEFAULT_TAB = "home"
    }

    private fun getPreferences(): SharedPreferences {
        return context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    }

    fun saveTab(tab: String) {
        val preferences = getPreferences()
        preferences.edit().putString(TAB_KEY, tab).apply()
    }

    fun getSavedTab(): String {
        val preferences = getPreferences()
        return preferences.getString(TAB_KEY, DEFAULT_TAB) ?: DEFAULT_TAB
    }
}
