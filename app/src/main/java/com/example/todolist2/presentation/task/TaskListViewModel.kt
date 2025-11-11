package com.example.todolist2.presentation.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist2.domain.model.Task
import com.example.todolist2.domain.model.TodoList
import com.example.todolist2.domain.repository.AuthRepository
import com.example.todolist2.domain.repository.TaskRepository
import com.example.todolist2.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TaskListState(
    val tasks: List<Task> = emptyList(),
    val lists: List<TodoList> = emptyList(),
    val selectedListId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddTaskDialog: Boolean = false,
    val userDisplayName: String = ""
)

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(TaskListState())
    val state = _state.asStateFlow()
    
    private val userId: String?
        get() = authRepository.getCurrentUser()?.uid
    
    init {
        loadUserProfile()
        loadTasks()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            userId?.let { uid ->
                when (val result = authRepository.getUserProfile(uid)) {
                    is Resource.Success -> {
                        _state.update { it.copy(userDisplayName = result.data.username) }
                    }
                    else -> {}
                }
            }
        }
    }
    
    private fun loadTasks() {
        val uid = userId ?: return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // For now, load all tasks (will optimize later with list filtering)
            taskRepository.observeAllTasks(uid)
                .catch { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { tasks ->
                    _state.update { it.copy(tasks = tasks, isLoading = false) }
                }
        }
    }
    
    fun showAddTaskDialog() {
        _state.update { it.copy(showAddTaskDialog = true) }
    }
    
    fun hideAddTaskDialog() {
        _state.update { it.copy(showAddTaskDialog = false) }
    }
    
    fun addTask(title: String) {
        val uid = userId ?: return
        
        viewModelScope.launch {
            // Create a default list if none exists
            val listId = _state.value.selectedListId ?: run {
                val defaultList = TodoList(
                    id = UUID.randomUUID().toString(),
                    userId = uid,
                    name = "Công việc của tôi",
                    color = "#6200EE"
                )
                taskRepository.createList(uid, defaultList)
                defaultList.id
            }
            
            val task = Task(
                id = UUID.randomUUID().toString(),
                listId = listId,
                userId = uid,
                title = title,
                createdAt = System.currentTimeMillis()
            )
            
            taskRepository.createTask(uid, listId, task)
        }
    }
    
    fun toggleTaskComplete(task: Task) {
        val uid = userId ?: return
        
        viewModelScope.launch {
            val updatedTask = task.copy(
                isCompleted = !task.isCompleted,
                completedAt = if (!task.isCompleted) System.currentTimeMillis() else null
            )
            taskRepository.updateTask(uid, task.listId, updatedTask)
        }
    }
}

