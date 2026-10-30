package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.KickEventEntity
import com.example.data.model.KickSessionEntity
import com.example.data.repository.KickRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class DailyTrendPoint(
    val dateLabel: String,
    val durationMinutes: Float,
    val kicks: Int,
    val targetReached: Boolean
)

data class TimeOfDayDistribution(
    val morningKicks: Int = 0,   // 6 AM - 12 PM
    val afternoonKicks: Int = 0, // 12 PM - 6 PM
    val eveningKicks: Int = 0,   // 6 PM - 12 AM
    val nightKicks: Int = 0      // 12 AM - 6 AM
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: KickRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = KickRepository(db.kickSessionDao(), db.kickEventDao())
    }

    val allSessions: StateFlow<List<KickSessionEntity>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedDateMillis = MutableStateFlow(System.currentTimeMillis())
    val selectedDateMillis: StateFlow<Long> = _selectedDateMillis.asStateFlow()

    private val _selectedSessionEvents = MutableStateFlow<List<KickEventEntity>>(emptyList())
    val selectedSessionEvents: StateFlow<List<KickEventEntity>> = _selectedSessionEvents.asStateFlow()

    fun selectDate(timestampMs: Long) {
        _selectedDateMillis.value = timestampMs
    }

    fun loadEventsForSession(sessionId: Long) {
        viewModelScope.launch {
            val events = repository.getEventsForSessionSync(sessionId)
            _selectedSessionEvents.value = events
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }

    fun getSessionsForSelectedDate(sessions: List<KickSessionEntity>, selectedMs: Long): List<KickSessionEntity> {
        val selCal = Calendar.getInstance().apply { timeInMillis = selectedMs }
        val selYear = selCal.get(Calendar.YEAR)
        val selDay = selCal.get(Calendar.DAY_OF_YEAR)

        return sessions.filter {
            val sessCal = Calendar.getInstance().apply { timeInMillis = it.startTimeMs }
            sessCal.get(Calendar.YEAR) == selYear && sessCal.get(Calendar.DAY_OF_YEAR) == selDay
        }
    }

    fun computeTrendPoints(sessions: List<KickSessionEntity>, limitDays: Int = 14): List<DailyTrendPoint> {
        return sessions.take(limitDays).reversed().map { session ->
            val cal = Calendar.getInstance().apply { timeInMillis = session.startTimeMs }
            val label = "${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.DAY_OF_MONTH)}"
            DailyTrendPoint(
                dateLabel = label,
                durationMinutes = (session.durationSeconds / 60f),
                kicks = session.totalKicks,
                targetReached = session.isTargetReached
            )
        }
    }

    fun computeTimeOfDayDistribution(sessions: List<KickSessionEntity>): TimeOfDayDistribution {
        var morning = 0
        var afternoon = 0
        var evening = 0
        var night = 0

        for (session in sessions) {
            val cal = Calendar.getInstance().apply { timeInMillis = session.startTimeMs }
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            when (hour) {
                in 6..11 -> morning += session.totalKicks
                in 12..17 -> afternoon += session.totalKicks
                in 18..23 -> evening += session.totalKicks
                else -> night += session.totalKicks
            }
        }
        return TimeOfDayDistribution(morning, afternoon, evening, night)
    }
}
