package com.example.todolist2.presentation.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist2.domain.model.Task
import com.example.todolist2.domain.model.TodoList
import com.example.todolist2.domain.repository.AuthRepository
import com.example.todolist2.domain.repository.GamificationRepository
import com.example.todolist2.domain.repository.TaskRepository
import com.example.todolist2.util.Constants
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
    val editingTask: Task? = null,
    val userDisplayName: String = ""
)

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository,
    private val gamificationRepository: GamificationRepository
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
    
    fun addTask(title: String, description: String = "") {
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
                title = title.trim(),
                description = description.trim(),
                createdAt = System.currentTimeMillis()
            )
            
            taskRepository.createTask(uid, listId, task)
        }
    }
    
    fun toggleTaskComplete(task: Task) {
        val uid = userId ?: return
        
        viewModelScope.launch {
            val isCompleting = !task.isCompleted
            val updatedTask = task.copy(
                isCompleted = isCompleting,
                completedAt = if (isCompleting) System.currentTimeMillis() else null
            )
            
            // Update task in database
            val updateResult = taskRepository.updateTask(uid, task.listId, updatedTask)
            
            // If task is being completed, update gamification stats
            if (isCompleting && updateResult is Resource.Success) {
                // Calculate points based on priority
                var points = Constants.XP_PER_TASK
                when (task.priority.value) {
                    3 -> points += Constants.XP_PER_PRIORITY_HIGH // HIGH
                    4 -> points += Constants.XP_PER_PRIORITY_URGENT // URGENT
                }
                
                // Add points (this also updates level automatically)
                gamificationRepository.addPoints(uid, points)
                
                // Update tasksCompleted count in user profile
                val userResult = authRepository.getUserProfile(uid)
                if (userResult is Resource.Success) {
                    val user = userResult.data
                    val updatedUser = user.copy(tasksCompleted = user.tasksCompleted + 1)
                    authRepository.updateUserProfile(updatedUser)
                }
                
                // Update streak (after updating user profile)
                gamificationRepository.updateStreak(uid)
                
                // Check and unlock badges (after updating stats)
                gamificationRepository.checkAndUnlockBadges(uid)
            } else if (!isCompleting && updateResult is Resource.Success) {
                // If uncompleting, decrease tasksCompleted count
                val userResult = authRepository.getUserProfile(uid)
                if (userResult is Resource.Success) {
                    val user = userResult.data
                    val updatedUser = user.copy(
                        tasksCompleted = (user.tasksCompleted - 1).coerceAtLeast(0)
                    )
                    authRepository.updateUserProfile(updatedUser)
                }
            }
        }
    }
    
    fun showEditTaskDialog(task: Task) {
        _state.update { it.copy(editingTask = task) }
    }
    
    fun hideEditTaskDialog() {
        _state.update { it.copy(editingTask = null) }
    }
    
    fun updateTask(task: Task, title: String, description: String = "") {
        val uid = userId ?: return
        
        viewModelScope.launch {
            val updatedTask = task.copy(
                title = title.trim(),
                description = description.trim()
            )
            taskRepository.updateTask(uid, task.listId, updatedTask)
            _state.update { it.copy(editingTask = null) }
        }
    }
    
    fun deleteTask(task: Task) {
        val uid = userId ?: return
        
        viewModelScope.launch {
            taskRepository.deleteTask(uid, task.listId, task.id)
            _state.update { it.copy(editingTask = null) }
        }
    }
}

