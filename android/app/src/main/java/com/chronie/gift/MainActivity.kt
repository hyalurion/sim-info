package com.hyalurion.sim.info

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.hyalurion.sim.info.data.LanguageManager
import com.hyalurion.sim.info.data.ThemeManager
import com.hyalurion.sim.info.ui.SimInfoApp
import com.hyalurion.sim.info.ui.theme.SimInfoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Load saved language setting (null means follow system language)
        val languageManager = LanguageManager(this)
        val savedLanguage = languageManager.getSavedLanguage()
        languageManager.applyLanguage(savedLanguage)
        
        setContent {
            SimInfoApp()
        }
    }
    
    // Handle configuration changes to ensure language setting is applied
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        
        // Reapply language setting
        val languageManager = LanguageManager(this)
        val savedLanguage = languageManager.getSavedLanguage()
        languageManager.applyLanguage(savedLanguage)
    }
}

@Preview(showBackground = true)
@Composable
fun SimInfoAppPreview() {
    SimInfoTheme {
        SimInfoApp()
    }
}