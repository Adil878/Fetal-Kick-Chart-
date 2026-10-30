package com.example.ui

import android.app.Application
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.KickSessionEntity
import com.example.data.repository.KickRepository
import com.example.data.security.SecureStorageManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActiveSessionState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val selectedTargetKicks: Int = 10, // 0 = continuous
    val selectedDurationMinutes: Int? = 120, // null = untimed
    val startTimeMs: Long = 0L,
    val elapsedSeconds: Long = 0L,
    val kickTimestamps: List<Long> = emptyList(),
    val isTargetReached: Boolean = false,
    val showFinishDialog: Boolean = false,
    val notes: String = "",
    val movementStrength: Int = 2, // 1 = gentle, 2 = moderate, 3 = vigorous
    val waterGlasses: Int = 0 // number of 250ml water glasses logged
) {
    val currentKicks: Int get() = kickTimestamps.size

    val remainingSeconds: Long?
        get() = selectedDurationMinutes?.let { mins ->
            val totalSecs = mins * 60L
            (totalSecs - elapsedSeconds).coerceAtLeast(0L)
        }

    val averageIntervalSeconds: Long
        get() {
            if (kickTimestamps.size <= 1) return 0L
            val intervals = mutableListOf<Long>()
            for (i in 1 until kickTimestamps.size) {
                intervals.add((kickTimestamps[i] - kickTimestamps[i - 1]) / 1000)
            }
            return if (intervals.isNotEmpty()) intervals.average().toLong() else 0L
        }
}

class SessionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KickRepository
    private val secureStorage = SecureStorageManager(application)
    private val vibrator = application.getSystemService(Vibrator::class.java)

    private val _uiState = MutableStateFlow(ActiveSessionState())
    val uiState: StateFlow<ActiveSessionState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        val db = AppDatabase.getInstance(application)
        repository = KickRepository(db.kickSessionDao(), db.kickEventDao())

        // Load default preferences
        _uiState.value = _uiState.value.copy(
            selectedTargetKicks = secureStorage.defaultTargetKicks,
            selectedDurationMinutes = if (secureStorage.defaultDurationMinutes > 0)
                secureStorage.defaultDurationMinutes else null
        )
    }

    fun setTargetKicks(target: Int) {
        if (!_uiState.value.isRunning) {
            _uiState.value = _uiState.value.copy(selectedTargetKicks = target)
        }
    }

    fun setDurationMinutes(minutes: Int?) {
        if (!_uiState.value.isRunning) {
            _uiState.value = _uiState.value.copy(selectedDurationMinutes = minutes)
        }
    }

    fun startSession() {
        _uiState.value = _uiState.value.copy(
            isRunning = true,
            isPaused = false,
            startTimeMs = System.currentTimeMillis(),
            elapsedSeconds = 0L,
            kickTimestamps = emptyList(),
            isTargetReached = false,
            showFinishDialog = false,
            notes = "",
            waterGlasses = 0
        )
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.isRunning) {
                delay(1000)
                if (!_uiState.value.isPaused) {
                    val currentElapsed = _uiState.value.elapsedSeconds + 1
                    val durationLimitMins = _uiState.value.selectedDurationMinutes

                    if (durationLimitMins != null && currentElapsed >= durationLimitMins * 60) {
                        // Duration limit reached!
                        _uiState.value = _uiState.value.copy(
                            elapsedSeconds = durationLimitMins * 60L,
                            isRunning = false,
                            showFinishDialog = true
                        )
                        timerJob?.cancel()
                        break
                    } else {
                        _uiState.value = _uiState.value.copy(elapsedSeconds = currentElapsed)
                    }
                }
            }
        }
    }

    fun recordKick() {
        if (!_uiState.value.isRunning || _uiState.value.isPaused) return

        triggerHaptic()

        val now = System.currentTimeMillis()
        val updatedKicks = _uiState.value.kickTimestamps + now
        val target = _uiState.value.selectedTargetKicks
        val isTargetReached = target in 1..updatedKicks.size

        if (isTargetReached && !_uiState.value.isTargetReached) {
            // Target achieved! Open completion dialog
            _uiState.value = _uiState.value.copy(
                kickTimestamps = updatedKicks,
                isTargetReached = true,
                showFinishDialog = true,
                isRunning = false
            )
            timerJob?.cancel()
        } else {
            _uiState.value = _uiState.value.copy(
                kickTimestamps = updatedKicks
            )
        }
    }

    fun undoLastKick() {
        if (_uiState.value.kickTimestamps.isNotEmpty()) {
            val updated = _uiState.value.kickTimestamps.dropLast(1)
            _uiState.value = _uiState.value.copy(
                kickTimestamps = updated,
                isTargetReached = false
            )
        }
    }

    fun togglePauseResume() {
        val currentPause = _uiState.value.isPaused
        _uiState.value = _uiState.value.copy(isPaused = !currentPause)
    }

    fun requestManualFinish() {
        if (_uiState.value.isRunning) {
            _uiState.value = _uiState.value.copy(
                isRunning = false,
                showFinishDialog = true
            )
            timerJob?.cancel()
        }
    }

    fun cancelSession() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isRunning = false,
            isPaused = false,
            elapsedSeconds = 0L,
            kickTimestamps = emptyList(),
            showFinishDialog = false
        )
    }

    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun updateMovementStrength(strength: Int) {
        _uiState.value = _uiState.value.copy(movementStrength = strength)
    }

    fun addWaterGlass() {
        _uiState.value = _uiState.value.copy(waterGlasses = _uiState.value.waterGlasses + 1)
        triggerHaptic()
    }

    fun removeWaterGlass() {
        if (_uiState.value.waterGlasses > 0) {
            _uiState.value = _uiState.value.copy(waterGlasses = _uiState.value.waterGlasses - 1)
        }
    }

    fun saveCompletedSession(onSaved: () -> Unit) {
        val state = _uiState.value
        val endTime = System.currentTimeMillis()
        val sessionEntity = KickSessionEntity(
            startTimeMs = state.startTimeMs,
            endTimeMs = endTime,
            durationSeconds = state.elapsedSeconds,
            targetDurationMinutes = state.selectedDurationMinutes,
            targetKicks = state.selectedTargetKicks,
            totalKicks = state.currentKicks,
            isTargetReached = if (state.selectedTargetKicks > 0) state.currentKicks >= state.selectedTargetKicks else true,
            notes = state.notes.trim(),
            movementStrength = state.movementStrength,
            waterIntakeGlasses = state.waterGlasses
        )

        viewModelScope.launch {
            repository.saveCompletedSession(sessionEntity, state.kickTimestamps)
            _uiState.value = _uiState.value.copy(
                showFinishDialog = false,
                isRunning = false,
                isPaused = false,
                kickTimestamps = emptyList(),
                elapsedSeconds = 0L
            )
            onSaved()
        }
    }

    private fun triggerHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(45)
            }
        } catch (_: Exception) {
            // Ignore if vibration not available on emulator
        }
    }
}
