package com.example.todolist2.presentation.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist2.domain.model.Task
import com.example.todolist2.domain.repository.AuthRepository
import com.example.todolist2.domain.repository.TaskRepository
import com.example.todolist2.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArchiveState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(ArchiveState())
    val state = _state.asStateFlow()
    
    private val userId: String?
        get() = authRepository.getCurrentUser()?.uid
    
    init {
        loadArchivedTasks()
    }
    
    private fun loadArchivedTasks() {
        val uid = userId ?: return
        
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                taskRepository.observeArchivedTasks(uid)
                    .catch { e ->
                        _state.update { 
                            it.copy(error = e.message ?: "Lỗi khi tải dữ liệu", isLoading = false) 
                        }
                    }
                    .collect { tasks ->
                        _state.update { 
                            it.copy(tasks = tasks, isLoading = false, error = null)
                        }
                    }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(error = e.message ?: "Lỗi không xác định", isLoading = false) 
                }
            }
        }
    }
    
    fun unarchiveTask(task: Task) {
        val uid = userId ?: return
        
        viewModelScope.launch {
            taskRepository.unarchiveTask(uid, task.listId, task.id)
        }
    }
    
    fun deleteTask(task: Task) {
        val uid = userId ?: return
        
        viewModelScope.launch {
            taskRepository.deleteTask(uid, task.listId, task.id)
        }
    }
    
    fun clearArchive() {
        val uid = userId ?: return
        
        viewModelScope.launch {
            state.value.tasks.forEach { task ->
                taskRepository.deleteTask(uid, task.listId, task.id)
            }
        }
    }
    
    fun refresh() {
        loadArchivedTasks()
    }
}

