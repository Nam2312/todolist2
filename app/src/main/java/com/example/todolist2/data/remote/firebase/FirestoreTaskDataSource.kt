package com.example.todolist2.data.remote.firebase

import com.example.todolist2.domain.model.Task
import com.example.todolist2.domain.model.TodoList
import com.example.todolist2.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Module 2: Firestore Task Management data source
 */
@Singleton
class FirestoreTaskDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    // Todo Lists
    
    fun observeLists(userId: String): Flow<List<TodoList>> = callbackFlow {
        val subscription = firestore.collection("users")
            .document(userId)
            .collection("lists")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val lists = snapshot?.documents?.mapNotNull { 
                    it.toObject(TodoList::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(lists)
            }
        awaitClose { subscription.remove() }
    }
    
    suspend fun createList(userId: String, list: TodoList): Resource<TodoList> {
        return try {
            val docRef = firestore.collection("users")
                .document(userId)
                .collection("lists")
                .document(list.id.ifEmpty { firestore.collection("lists").document().id })
            
            val newList = list.copy(id = docRef.id, userId = userId)
            docRef.set(newList).await()
            
            Resource.Success(newList)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create list")
        }
    }
    
    suspend fun updateList(userId: String, list: TodoList): Resource<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("lists")
                .document(list.id)
                .set(list)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update list")
        }
    }
    
    suspend fun deleteList(userId: String, listId: String): Resource<Unit> {
        return try {
            // Delete all tasks in the list first
            val tasks = firestore.collection("users")
                .document(userId)
                .collection("lists")
                .document(listId)
                .collection("tasks")
                .get()
                .await()
            
            tasks.documents.forEach { it.reference.delete().await() }
            
            // Delete the list
            firestore.collection("users")
                .document(userId)
                .collection("lists")
                .document(listId)
                .delete()
                .await()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete list")
        }
    }
    
    // Tasks
    
    fun observeTasks(userId: String, listId: String): Flow<List<Task>> = callbackFlow {
        val subscription = firestore.collection("users")
            .document(userId)
            .collection("lists")
            .document(listId)
            .collection("tasks")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val tasks = snapshot?.documents?.mapNotNull { 
                    it.toObject(Task::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(tasks)
            }
        awaitClose { subscription.remove() }
    }
    
    fun observeAllTasks(userId: String): Flow<List<Task>> = callbackFlow {
        // Observe lists, and whenever lists change, query all tasks from all lists
        val listsSubscription = firestore.collection("users")
            .document(userId)
            .collection("lists")
            .addSnapshotListener { listsSnapshot, listsError ->
                if (listsError != null) {
                    close(listsError)
                    return@addSnapshotListener
                }
                
                val listIds = listsSnapshot?.documents?.map { it.id } ?: emptyList()
                
                if (listIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                // Query tasks from all lists asynchronously
                // Note: This is a simplified approach. For better performance with many lists,
                // consider using collection group query with proper Firestore indexes
                kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val allTasks = mutableListOf<Task>()
                        
                        listIds.forEach { listId ->
                            try {
                                val tasksSnapshot = firestore.collection("users")
                                    .document(userId)
                                    .collection("lists")
                                    .document(listId)
                                    .collection("tasks")
                                    .orderBy("createdAt", Query.Direction.DESCENDING)
                                    .get()
                                    .await()
                                
                                val tasks = tasksSnapshot.documents.mapNotNull {
                                    it.toObject(Task::class.java)?.copy(id = it.id, listId = listId, userId = userId)
                                }
                                allTasks.addAll(tasks)
                            } catch (e: Exception) {
                                // Continue with other lists if one fails
                            }
                        }
                        
                        // Sort by createdAt descending
                        val sortedTasks = allTasks.sortedByDescending { it.createdAt }
                        trySend(sortedTasks)
                    } catch (e: Exception) {
                        close(e)
                    }
                }
            }
        
        awaitClose { listsSubscription.remove() }
    }
    
    suspend fun createTask(userId: String, listId: String, task: Task): Resource<Task> {
        return try {
            val docRef = firestore.collection("users")
                .document(userId)
                .collection("lists")
                .document(listId)
                .collection("tasks")
                .document(task.id.ifEmpty { firestore.collection("tasks").document().id })
            
            val newTask = task.copy(id = docRef.id, listId = listId, userId = userId)
            docRef.set(newTask).await()
            
            // Update list task count
            updateListTaskCount(userId, listId)
            
            Resource.Success(newTask)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create task")
        }
    }
    
    suspend fun updateTask(userId: String, listId: String, task: Task): Resource<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("lists")
                .document(listId)
                .collection("tasks")
                .document(task.id)
                .set(task)
                .await()
            
            // Update list task count
            updateListTaskCount(userId, listId)
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update task")
        }
    }
    
    suspend fun deleteTask(userId: String, listId: String, taskId: String): Resource<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("lists")
                .document(listId)
                .collection("tasks")
                .document(taskId)
                .delete()
                .await()
            
            // Update list task count
            updateListTaskCount(userId, listId)
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete task")
        }
    }
    
    private suspend fun updateListTaskCount(userId: String, listId: String) {
        try {
            val tasks = firestore.collection("users")
                .document(userId)
                .collection("lists")
                .document(listId)
                .collection("tasks")
                .get()
                .await()
            
            val totalTasks = tasks.size()
            val completedTasks = tasks.documents.count { 
                it.getBoolean("isCompleted") == true 
            }
            
            firestore.collection("users")
                .document(userId)
                .collection("lists")
                .document(listId)
                .update(
                    mapOf(
                        "taskCount" to totalTasks,
                        "completedTaskCount" to completedTasks
                    )
                )
                .await()
        } catch (e: Exception) {
            // Silently fail - this is not critical
        }
    }
}









