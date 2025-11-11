package com.example.todolist2.data.local.dao

import androidx.room.*
import com.example.todolist2.data.local.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    
    @Query("SELECT * FROM focus_sessions WHERE userId = :userId ORDER BY startTime DESC")
    fun getAllSessions(userId: String): Flow<List<FocusSessionEntity>>
    
    @Query("SELECT * FROM focus_sessions WHERE userId = :userId AND completed = 1 ORDER BY startTime DESC LIMIT :limit")
    fun getCompletedSessions(userId: String, limit: Int): Flow<List<FocusSessionEntity>>
    
    @Query("SELECT * FROM focus_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: String): FocusSessionEntity?
    
    @Query("SELECT SUM(durationInMinutes) FROM focus_sessions WHERE userId = :userId AND completed = 1 AND startTime >= :fromTime")
    suspend fun getTotalFocusMinutes(userId: String, fromTime: Long): Int?
    
    @Query("SELECT COUNT(*) FROM focus_sessions WHERE userId = :userId AND completed = 1")
    suspend fun getCompletedSessionCount(userId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity)
    
    @Update
    suspend fun updateSession(session: FocusSessionEntity)
    
    @Delete
    suspend fun deleteSession(session: FocusSessionEntity)
    
    @Query("DELETE FROM focus_sessions WHERE userId = :userId")
    suspend fun deleteAllUserSessions(userId: String)
    
    @Query("SELECT * FROM focus_sessions WHERE isSynced = 0")
    suspend fun getUnsyncedSessions(): List<FocusSessionEntity>
    
    @Query("UPDATE focus_sessions SET isSynced = 1 WHERE id = :sessionId")
    suspend fun markAsSynced(sessionId: String)
}


