package com.example.todolist2.domain.repository

import com.example.todolist2.domain.model.FocusSession
import com.example.todolist2.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Module 2: Focus Session repository interface
 */
interface FocusSessionRepository {
    fun observeSessions(userId: String): Flow<List<FocusSession>>
    suspend fun getSessionById(sessionId: String): FocusSession?
    suspend fun createSession(userId: String, session: FocusSession): Resource<FocusSession>
    suspend fun updateSession(userId: String, session: FocusSession): Resource<Unit>
    suspend fun deleteSession(userId: String, sessionId: String): Resource<Unit>
    suspend fun getCompletedSessions(userId: String, limit: Int = 10): List<FocusSession>
    suspend fun getTotalFocusMinutes(userId: String, fromTime: Long = 0): Int
}









