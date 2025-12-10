package com.example.todolist2.presentation.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist2.domain.model.Task
import com.example.todolist2.domain.repository.AuthRepository
import com.example.todolist2.domain.repository.GamificationRepository
import com.example.todolist2.domain.repository.TaskRepository
import com.example.todolist2.util.Constants
import com.example.todolist2.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskDetailState(
    val task: Task? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val editingTask: Task? = null,
    val showDeleteDialog: Boolean = false
)

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository,
    private val gamificationRepository: GamificationRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(TaskDetailState())
    val state = _state.asStateFlow()
    
    private val userId: String?
        get() = authRepository.getCurrentUser()?.uid
    
    fun loadTask(taskId: String) {
        val uid = userId ?: return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val task = taskRepository.getTaskById(taskId)
            _state.update { 
                it.copy(
                    task = task,
                    isLoading = false
                )
            }
        }
    }
    
    fun toggleComplete() {
        val task = _state.value.task ?: return
        val uid = userId ?: return
        
        viewModelScope.launch {
            val isCompleting = !task.isCompleted
            val updatedTask = task.copy(
                isCompleted = isCompleting,
                completedAt = if (isCompleting) System.currentTimeMillis() else null
            )
            
            taskRepository.updateTask(uid, task.listId, updatedTask)
            
            // Update gamification if completing
            if (isCompleting) {
                var points = Constants.XP_PER_TASK
                when (task.priority.value) {
                    3 -> points += Constants.XP_PER_PRIORITY_HIGH
                    4 -> points += Constants.XP_PER_PRIORITY_URGENT
                }
                gamificationRepository.addPoints(uid, points)
                gamificationRepository.updateStreak(uid)
                gamificationRepository.checkAndUnlockBadges(uid)
            }
            
            _state.update { it.copy(task = updatedTask) }
        }
    }
    
    fun showEditDialog() {
        _state.update { it.copy(editingTask = _state.value.task) }
    }
    
    fun hideEditDialog() {
        _state.update { it.copy(editingTask = null) }
    }
    
    fun showDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = true) }
    }
    
    fun hideDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = false) }
    }
    
    fun deleteTask() {
        val task = _state.value.task ?: return
        val uid = userId ?: return
        
        viewModelScope.launch {
            taskRepository.deleteTask(uid, task.listId, task.id)
        }
    }
    
    fun addSubTask(title: String) {
        val task = _state.value.task ?: return
        val uid = userId ?: return
        
        if (title.isBlank()) return
        
        viewModelScope.launch {
            val newSubTask = com.example.todolist2.domain.model.SubTask(
                id = java.util.UUID.randomUUID().toString(),
                title = title.trim(),
                isCompleted = false
            )
            val updatedTask = task.copy(
                subTasks = task.subTasks + newSubTask
            )
            taskRepository.updateTask(uid, task.listId, updatedTask)
            _state.update { it.copy(task = updatedTask) }
        }
    }
    
    fun toggleSubTask(subTaskId: String) {
        val task = _state.value.task ?: return
        val uid = userId ?: return
        
        viewModelScope.launch {
            val updatedSubTasks = task.subTasks.map { subTask ->
                if (subTask.id == subTaskId) {
                    subTask.copy(isCompleted = !subTask.isCompleted)
                } else {
                    subTask
                }
            }
            val updatedTask = task.copy(subTasks = updatedSubTasks)
            taskRepository.updateTask(uid, task.listId, updatedTask)
            _state.update { it.copy(task = updatedTask) }
        }
    }
    
    fun deleteSubTask(subTaskId: String) {
        val task = _state.value.task ?: return
        val uid = userId ?: return
        
        viewModelScope.launch {
            val updatedSubTasks = task.subTasks.filter { it.id != subTaskId }
            val updatedTask = task.copy(subTasks = updatedSubTasks)
            taskRepository.updateTask(uid, task.listId, updatedTask)
            _state.update { it.copy(task = updatedTask) }
        }
    }
}

