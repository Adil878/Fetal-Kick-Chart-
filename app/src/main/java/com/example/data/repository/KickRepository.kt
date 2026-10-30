package com.example.data.repository

import com.example.data.local.KickEventDao
import com.example.data.local.KickSessionDao
import com.example.data.model.KickEventEntity
import com.example.data.model.KickSessionEntity
import kotlinx.coroutines.flow.Flow

class KickRepository(
    private val sessionDao: KickSessionDao,
    private val eventDao: KickEventDao
) {
    val allSessions: Flow<List<KickSessionEntity>> = sessionDao.getAllSessions()

    fun getSessionsBetween(startMs: Long, endMs: Long): Flow<List<KickSessionEntity>> {
        return sessionDao.getSessionsBetween(startMs, endMs)
    }

    fun getEventsForSession(sessionId: Long): Flow<List<KickEventEntity>> {
        return eventDao.getEventsForSession(sessionId)
    }

    suspend fun getEventsForSessionSync(sessionId: Long): List<KickEventEntity> {
        return eventDao.getEventsForSessionSync(sessionId)
    }

    suspend fun saveCompletedSession(
        session: KickSessionEntity,
        events: List<Long> // timestamps of kicks
    ): Long {
        val sessionId = sessionDao.insertSession(session)
        if (events.isNotEmpty()) {
            val eventEntities = mutableListOf<KickEventEntity>()
            var lastTime = session.startTimeMs
            for (timestamp in events) {
                val intervalSec = ((timestamp - lastTime).coerceAtLeast(0)) / 1000
                eventEntities.add(
                    KickEventEntity(
                        sessionId = sessionId,
                        timestampMs = timestamp,
                        intervalSincePreviousSeconds = intervalSec
                    )
                )
                lastTime = timestamp
            }
            eventDao.insertEvents(eventEntities)
        }
        return sessionId
    }

    suspend fun deleteSession(sessionId: Long) {
        eventDao.deleteEventsForSession(sessionId)
        sessionDao.deleteSessionById(sessionId)
    }
}
