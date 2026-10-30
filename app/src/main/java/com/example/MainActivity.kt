package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AuthViewModel
import com.example.ui.HistoryViewModel
import com.example.ui.MainApp
import com.example.ui.SessionViewModel
import com.example.ui.SettingsViewModel
import com.example.ui.theme.FetalKickTrackerTheme

class MainActivity : FragmentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val sessionViewModel: SessionViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()
            val systemInDark = isSystemInDarkTheme()
            val darkTheme = if (settingsState.followSystemTheme) systemInDark else settingsState.isDarkMode

            FetalKickTrackerTheme(darkTheme = darkTheme) {
                MainApp(
                    activity = this@MainActivity,
                    authViewModel = authViewModel,
                    sessionViewModel = sessionViewModel,
                    historyViewModel = historyViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        settingsViewModel.refreshLockState()
    }
}
