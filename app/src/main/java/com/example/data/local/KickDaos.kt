package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.KickEventEntity
import com.example.data.model.KickSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KickSessionDao {
    @Query("SELECT * FROM kick_sessions ORDER BY startTimeMs DESC")
    fun getAllSessions(): Flow<List<KickSessionEntity>>

    @Query("SELECT * FROM kick_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): KickSessionEntity?

    @Query("SELECT * FROM kick_sessions WHERE startTimeMs >= :startMs AND startTimeMs <= :endMs ORDER BY startTimeMs DESC")
    fun getSessionsBetween(startMs: Long, endMs: Long): Flow<List<KickSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: KickSessionEntity): Long

    @Query("DELETE FROM kick_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("SELECT COUNT(*) FROM kick_sessions")
    fun getSessionCount(): Flow<Int>
}

@Dao
interface KickEventDao {
    @Query("SELECT * FROM kick_events WHERE sessionId = :sessionId ORDER BY timestampMs ASC")
    fun getEventsForSession(sessionId: Long): Flow<List<KickEventEntity>>

    @Query("SELECT * FROM kick_events WHERE sessionId = :sessionId ORDER BY timestampMs ASC")
    suspend fun getEventsForSessionSync(sessionId: Long): List<KickEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<KickEventEntity>)

    @Query("DELETE FROM kick_events WHERE sessionId = :sessionId")
    suspend fun deleteEventsForSession(sessionId: Long)
}
