package com.example.todolist2.domain.repository

import com.example.todolist2.domain.model.Task
import com.example.todolist2.domain.model.TodoList
import com.example.todolist2.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Module 2: Task management repository interface
 */
interface TaskRepository {
    // Lists
    fun observeLists(userId: String): Flow<List<TodoList>>
    suspend fun createList(userId: String, list: TodoList): Resource<TodoList>
    suspend fun updateList(userId: String, list: TodoList): Resource<Unit>
    suspend fun deleteList(userId: String, listId: String): Resource<Unit>
    
    // Tasks
    fun observeTasks(userId: String, listId: String): Flow<List<Task>>
    fun observeAllTasks(userId: String): Flow<List<Task>>
    suspend fun getTaskById(taskId: String): Task?
    suspend fun createTask(userId: String, listId: String, task: Task): Resource<Task>
    suspend fun updateTask(userId: String, listId: String, task: Task): Resource<Unit>
    suspend fun deleteTask(userId: String, listId: String, taskId: String): Resource<Unit>
    suspend fun searchTasks(userId: String, query: String): List<Task>
}








