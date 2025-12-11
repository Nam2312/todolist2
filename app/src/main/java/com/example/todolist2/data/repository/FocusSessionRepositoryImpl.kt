package com.example.todolist2.data.repository

import com.example.todolist2.data.local.dao.FocusSessionDao
import com.example.todolist2.data.local.entity.toDomain
import com.example.todolist2.data.local.entity.toEntity
import com.example.todolist2.data.remote.firebase.FirestoreTaskDataSource
import com.example.todolist2.domain.model.FocusSession
import com.example.todolist2.domain.repository.FocusSessionRepository
import com.example.todolist2.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Module 2: Focus Session repository implementation with offline-first approach
 */
@Singleton
class FocusSessionRepositoryImpl @Inject constructor(
    private val focusSessionDao: FocusSessionDao,
    private val firestoreDataSource: FirestoreTaskDataSource
) : FocusSessionRepository {
    
    override fun observeSessions(userId: String): Flow<List<FocusSession>> {
        return focusSessionDao.getAllSessions(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getSessionById(sessionId: String): FocusSession? {
        return focusSessionDao.getSessionById(sessionId)?.toDomain()
    }
    
    override suspend fun createSession(userId: String, session: FocusSession): Resource<FocusSession> {
        // Save to local first
        focusSessionDao.insertSession(session.toEntity())
        
        // TODO: Sync to Firestore if needed
        // For now, focus sessions are stored locally only
        
        return Resource.Success(session)
    }
    
    override suspend fun updateSession(userId: String, session: FocusSession): Resource<Unit> {
        focusSessionDao.updateSession(session.toEntity())
        
        // TODO: Sync to Firestore if needed
        
        return Resource.Success(Unit)
    }
    
    override suspend fun deleteSession(userId: String, sessionId: String): Resource<Unit> {
        focusSessionDao.getSessionById(sessionId)?.let {
            focusSessionDao.deleteSession(it)
        }
        
        // TODO: Sync to Firestore if needed
        
        return Resource.Success(Unit)
    }
    
    override suspend fun getCompletedSessions(userId: String, limit: Int): List<FocusSession> {
        return focusSessionDao.getCompletedSessions(userId, limit).map { entities ->
            entities.map { it.toDomain() }
        }.firstOrNull() ?: emptyList()
    }
    
    override suspend fun getTotalFocusMinutes(userId: String, fromTime: Long): Int {
        return focusSessionDao.getTotalFocusMinutes(userId, fromTime) ?: 0
    }
    
    // Helper extension to get first item from Flow safely
    private suspend fun <T> Flow<T>.firstOrNull(): T? {
        var result: T? = null
        collect { value ->
            result = value
            return@collect
        }
        return result
    }
}




