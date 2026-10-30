package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a recorded kick counting session.
 * @property targetDurationMinutes Customizable duration limit in minutes (e.g. 30, 60, 120), or null if untimed.
 * @property targetKicks Customizable target kick count (e.g. 10), or 0 if continuous/untimed.
 * @property waterIntakeGlasses Number of water glasses (250ml each) consumed/logged during session.
 */
@Entity(tableName = "kick_sessions")
data class KickSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val durationSeconds: Long,
    val targetDurationMinutes: Int?, // null means untimed open-ended session
    val targetKicks: Int, // 0 means continuous tracking without kick ceiling
    val totalKicks: Int,
    val isTargetReached: Boolean,
    val notes: String = "",
    val movementStrength: Int = 2, // 1: gentle flutter, 2: moderate, 3: vigorous/strong
    val waterIntakeGlasses: Int = 0
)

@Entity(
    tableName = "kick_events"
)
data class KickEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val timestampMs: Long,
    val intervalSincePreviousSeconds: Long = 0
)
