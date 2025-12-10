package com.example.todolist2.data.repository

import com.example.todolist2.data.local.dao.TaskDao
import com.example.todolist2.data.local.dao.TodoListDao
import com.example.todolist2.data.local.entity.toDomain
import com.example.todolist2.data.local.entity.toEntity
import com.example.todolist2.data.remote.firebase.FirestoreTaskDataSource
import com.example.todolist2.domain.model.Task
import com.example.todolist2.domain.model.TodoList
import com.example.todolist2.domain.repository.TaskRepository
import android.content.Context
import com.example.todolist2.util.NotificationHelper
import com.example.todolist2.util.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Module 2: Task repository implementation with offline-first approach
 */
@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val listDao: TodoListDao,
    private val firestoreDataSource: FirestoreTaskDataSource,
    @ApplicationContext private val context: Context
) : TaskRepository {
    
    // Lists
    override fun observeLists(userId: String): Flow<List<TodoList>> {
        // Observe from Firestore (it has offline cache)
        return firestoreDataSource.observeLists(userId)
    }
    
    override suspend fun createList(userId: String, list: TodoList): Resource<TodoList> {
        // Save to local first
        listDao.insertList(list.toEntity())
        
        // Sync to Firestore
        val result = firestoreDataSource.createList(userId, list)
        
        if (result is Resource.Success) {
            // Update local with server ID
            listDao.insertList(result.data.toEntity().copy(isSynced = true))
        }
        
        return result
    }
    
    override suspend fun updateList(userId: String, list: TodoList): Resource<Unit> {
        listDao.updateList(list.toEntity())
        return firestoreDataSource.updateList(userId, list)
    }
    
    override suspend fun deleteList(userId: String, listId: String): Resource<Unit> {
        listDao.getListById(listId)?.let { listDao.deleteList(it) }
        taskDao.deleteTasksByList(listId)
        return firestoreDataSource.deleteList(userId, listId)
    }
    
    // Tasks
    override fun observeTasks(userId: String, listId: String): Flow<List<Task>> {
        // Use local database for immediate UI updates
        // Firestore will sync in background
        return taskDao.getTasksByList(listId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun observeAllTasks(userId: String): Flow<List<Task>> {
        // Fallback to local database for all tasks
        return taskDao.getAllTasks(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getTaskById(taskId: String): Task? {
        return taskDao.getTaskById(taskId)?.toDomain()
    }
    
    override suspend fun createTask(userId: String, listId: String, task: Task): Resource<Task> {
        // Save to local first
        taskDao.insertTask(task.toEntity())
        
        // Schedule notification if reminderTime is set
        task.reminderTime?.let { reminderTime ->
            if (reminderTime > System.currentTimeMillis()) {
                NotificationHelper.scheduleTaskReminder(
                    context,
                    task.id,
                    task.title,
                    reminderTime
                )
            }
        }
        
        // Sync to Firestore
        val result = firestoreDataSource.createTask(userId, listId, task)
        
        if (result is Resource.Success) {
            // Update local with server data
            taskDao.insertTask(result.data.toEntity().copy(isSynced = true))
        }
        
        return result
    }
    
    override suspend fun updateTask(userId: String, listId: String, task: Task): Resource<Unit> {
        // Cancel old notification
        NotificationHelper.cancelTaskReminder(context, task.id)
        
        // Schedule new notification if reminderTime is set
        task.reminderTime?.let { reminderTime ->
            if (reminderTime > System.currentTimeMillis()) {
                NotificationHelper.scheduleTaskReminder(
                    context,
                    task.id,
                    task.title,
                    reminderTime
                )
            }
        }
        
        // Update local database first for immediate UI update
        taskDao.updateTask(task.toEntity().copy(isSynced = false))
        
        // Sync to Firestore
        val result = firestoreDataSource.updateTask(userId, listId, task)
        
        // Mark as synced if successful
        if (result is Resource.Success) {
            taskDao.updateTask(task.toEntity().copy(isSynced = true))
        }
        
        return result
    }
    
    override suspend fun deleteTask(userId: String, listId: String, taskId: String): Resource<Unit> {
        // Cancel notification
        NotificationHelper.cancelTaskReminder(context, taskId)
        
        taskDao.getTaskById(taskId)?.let { taskDao.deleteTask(it) }
        return firestoreDataSource.deleteTask(userId, listId, taskId)
    }
    
    override suspend fun searchTasks(userId: String, query: String): List<Task> {
        return taskDao.searchTasks(userId, query).map { entities ->
            entities.map { it.toDomain() }
        }.firstOrNull() ?: emptyList()
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









