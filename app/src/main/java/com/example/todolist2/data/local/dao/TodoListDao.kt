package com.example.todolist2.data.local.dao

import androidx.room.*
import com.example.todolist2.data.local.entity.TodoListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoListDao {
    
    @Query("SELECT * FROM todo_lists WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllLists(userId: String): Flow<List<TodoListEntity>>
    
    @Query("SELECT * FROM todo_lists WHERE id = :listId")
    suspend fun getListById(listId: String): TodoListEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: TodoListEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLists(lists: List<TodoListEntity>)
    
    @Update
    suspend fun updateList(list: TodoListEntity)
    
    @Delete
    suspend fun deleteList(list: TodoListEntity)
    
    @Query("DELETE FROM todo_lists WHERE userId = :userId")
    suspend fun deleteAllUserLists(userId: String)
    
    @Query("SELECT * FROM todo_lists WHERE isSynced = 0")
    suspend fun getUnsyncedLists(): List<TodoListEntity>
    
    @Query("UPDATE todo_lists SET isSynced = 1 WHERE id = :listId")
    suspend fun markAsSynced(listId: String)
}








