package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.security.SecureStorageManager
import com.example.reminder.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val followSystemTheme: Boolean = true,
    val isDailyReminderEnabled: Boolean = true,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val isAppLockEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = true,
    val hasPinSet: Boolean = false,
    val defaultTargetKicks: Int = 10,
    val defaultDurationMinutes: Int = 120
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val secureStorage = SecureStorageManager(application)

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            isDarkMode = secureStorage.isDarkMode,
            followSystemTheme = secureStorage.followSystemTheme,
            isDailyReminderEnabled = secureStorage.isDailyReminderEnabled,
            reminderHour = secureStorage.dailyReminderHour,
            reminderMinute = secureStorage.dailyReminderMinute,
            isAppLockEnabled = secureStorage.isAppLockEnabled,
            isBiometricEnabled = secureStorage.isBiometricEnabled,
            hasPinSet = secureStorage.hasPinSet(),
            defaultTargetKicks = secureStorage.defaultTargetKicks,
            defaultDurationMinutes = secureStorage.defaultDurationMinutes
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        secureStorage.isDarkMode = enabled
        secureStorage.followSystemTheme = false
        _uiState.value = _uiState.value.copy(
            isDarkMode = enabled,
            followSystemTheme = false
        )
    }

    fun setFollowSystemTheme(enabled: Boolean) {
        secureStorage.followSystemTheme = enabled
        _uiState.value = _uiState.value.copy(followSystemTheme = enabled)
    }

    fun setDailyReminder(enabled: Boolean) {
        secureStorage.isDailyReminderEnabled = enabled
        _uiState.value = _uiState.value.copy(isDailyReminderEnabled = enabled)
        val context = getApplication<Application>()
        if (enabled) {
            ReminderScheduler.scheduleDailyReminder(
                context,
                _uiState.value.reminderHour,
                _uiState.value.reminderMinute
            )
        } else {
            ReminderScheduler.cancelReminder(context)
        }
    }

    fun setReminderTime(hour: Int, minute: Int) {
        secureStorage.dailyReminderHour = hour
        secureStorage.dailyReminderMinute = minute
        _uiState.value = _uiState.value.copy(
            reminderHour = hour,
            reminderMinute = minute
        )
        if (_uiState.value.isDailyReminderEnabled) {
            ReminderScheduler.scheduleDailyReminder(
                getApplication(),
                hour,
                minute
            )
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        secureStorage.isBiometricEnabled = enabled
        _uiState.value = _uiState.value.copy(isBiometricEnabled = enabled)
    }

    fun setAppLockEnabled(enabled: Boolean) {
        secureStorage.isAppLockEnabled = enabled
        _uiState.value = _uiState.value.copy(isAppLockEnabled = enabled)
    }

    fun setDefaultTargetKicks(target: Int) {
        secureStorage.defaultTargetKicks = target
        _uiState.value = _uiState.value.copy(defaultTargetKicks = target)
    }

    fun setDefaultDurationMinutes(minutes: Int) {
        secureStorage.defaultDurationMinutes = minutes
        _uiState.value = _uiState.value.copy(defaultDurationMinutes = minutes)
    }

    fun refreshLockState() {
        _uiState.value = _uiState.value.copy(
            hasPinSet = secureStorage.hasPinSet(),
            isAppLockEnabled = secureStorage.isAppLockEnabled
        )
    }
}
