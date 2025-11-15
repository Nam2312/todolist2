package com.example.todolist2.data.repository

import com.example.todolist2.data.local.dao.TaskDao
import com.example.todolist2.data.local.dao.TodoListDao
import com.example.todolist2.data.local.entity.toDomain
import com.example.todolist2.data.local.entity.toEntity
import com.example.todolist2.data.remote.firebase.FirestoreTaskDataSource
import com.example.todolist2.domain.model.Task
import com.example.todolist2.domain.model.TodoList
import com.example.todolist2.domain.repository.TaskRepository
import com.example.todolist2.util.Resource
import kotlinx.coroutines.flow.Flow
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
    private val firestoreDataSource: FirestoreTaskDataSource
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
        // Observe from Firestore with offline cache
        return firestoreDataSource.observeTasks(userId, listId)
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
        
        // Sync to Firestore
        val result = firestoreDataSource.createTask(userId, listId, task)
        
        if (result is Resource.Success) {
            // Update local with server data
            taskDao.insertTask(result.data.toEntity().copy(isSynced = true))
        }
        
        return result
    }
    
    override suspend fun updateTask(userId: String, listId: String, task: Task): Resource<Unit> {
        taskDao.updateTask(task.toEntity())
        return firestoreDataSource.updateTask(userId, listId, task)
    }
    
    override suspend fun deleteTask(userId: String, listId: String, taskId: String): Resource<Unit> {
        taskDao.getTaskById(taskId)?.let { taskDao.deleteTask(it) }
        return firestoreDataSource.deleteTask(userId, listId, taskId)
    }
    
    override suspend fun searchTasks(userId: String, query: String): List<Task> {
        return taskDao.searchTasks(userId, query).map { entities ->
            entities.map { it.toDomain() }
        }.first()
    }
}

// Extension function to get first item from Flow
private suspend fun <T> Flow<T>.first(): T {
    var result: T? = null
    collect { value ->
        result = value
        return@collect
    }
    return result ?: throw NoSuchElementException("Flow was empty")
}







