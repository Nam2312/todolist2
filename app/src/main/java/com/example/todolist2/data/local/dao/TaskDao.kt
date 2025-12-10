package com.example.todolist2.data.local.dao

import androidx.room.*
import com.example.todolist2.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    
    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllTasks(userId: String): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE listId = :listId ORDER BY createdAt DESC")
    fun getTasksByList(listId: String): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?
    
    @Query("SELECT * FROM tasks WHERE userId = :userId AND isCompleted = 0 ORDER BY dueDate ASC")
    fun getIncompleteTasks(userId: String): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE userId = :userId AND isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTasks(userId: String): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE userId = :userId AND dueDate IS NOT NULL AND dueDate <= :timestamp AND isCompleted = 0")
    fun getOverdueTasks(userId: String, timestamp: Long): Flow<List<TaskEntity>>
    
    @Query("SELECT * FROM tasks WHERE userId = :userId AND title LIKE '%' || :query || '%'")
    fun searchTasks(userId: String, query: String): Flow<List<TaskEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)
    
    @Update
    suspend fun updateTask(task: TaskEntity)
    
    @Delete
    suspend fun deleteTask(task: TaskEntity)
    
    @Query("DELETE FROM tasks WHERE listId = :listId")
    suspend fun deleteTasksByList(listId: String)
    
    @Query("DELETE FROM tasks WHERE userId = :userId")
    suspend fun deleteAllUserTasks(userId: String)
    
    @Query("SELECT * FROM tasks WHERE isSynced = 0")
    suspend fun getUnsyncedTasks(): List<TaskEntity>
    
    @Query("UPDATE tasks SET isSynced = 1 WHERE id = :taskId")
    suspend fun markAsSynced(taskId: String)
}












