package com.example.todolist2.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist2.domain.model.Task
import com.example.todolist2.domain.repository.AuthRepository
import com.example.todolist2.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import com.example.todolist2.util.DateUtils

data class CalendarState(
    val calendar: Calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
    },
    val selectedDate: Long = DateUtils.getStartOfDay(System.currentTimeMillis()), // Normalized to start of day
    val tasks: List<Task> = emptyList(),
    val tasksByDate: Map<Long, List<Task>> = emptyMap(),
    val selectedDateTasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(CalendarState())
    val state = _state.asStateFlow()
    
    private val userId: String?
        get() = authRepository.getCurrentUser()?.uid
    
    init {
        loadTasks()
    }
    
    private fun loadTasks() {
        val uid = userId ?: return
        
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                taskRepository.observeAllTasks(uid)
                    .catch { e ->
                        _state.update { 
                            it.copy(error = e.message ?: "Lỗi khi tải dữ liệu", isLoading = false) 
                        }
                    }
                    .collect { tasks ->
                        // Filter tasks that have dueDate and are not archived
                        val tasksWithDueDate = tasks.filter { it.dueDate != null && !it.isArchived }
                        
                        // Group by start of day
                        val tasksByDate = tasksWithDueDate.groupBy { task ->
                            DateUtils.getStartOfDay(task.dueDate!!)
                        }
                        
                        // Get selected date (normalized to start of day)
                        val selectedDateNormalized = DateUtils.getStartOfDay(_state.value.selectedDate)
                        val selectedDateTasks = tasksByDate[selectedDateNormalized] ?: emptyList()
                        
                        _state.update { 
                            it.copy(
                                tasks = tasks,
                                tasksByDate = tasksByDate,
                                selectedDateTasks = selectedDateTasks,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(error = e.message ?: "Lỗi không xác định", isLoading = false) 
                }
            }
        }
    }
    
    fun selectDate(date: Long) {
        // Normalize date to start of day for consistent matching
        val selectedDateNormalized = DateUtils.getStartOfDay(date)
        val selectedDateTasks = _state.value.tasksByDate[selectedDateNormalized] ?: emptyList()
        
        _state.update { 
            it.copy(
                selectedDate = selectedDateNormalized, // Store normalized date
                selectedDateTasks = selectedDateTasks
            )
        }
    }
    
    fun previousMonth() {
        val newCalendar = _state.value.calendar.clone() as Calendar
        newCalendar.add(Calendar.MONTH, -1)
        _state.update { it.copy(calendar = newCalendar) }
    }
    
    fun nextMonth() {
        val newCalendar = _state.value.calendar.clone() as Calendar
        newCalendar.add(Calendar.MONTH, 1)
        _state.update { it.copy(calendar = newCalendar) }
    }
    
    fun today() {
        val today = Calendar.getInstance()
        val todayMillis = DateUtils.getStartOfDay(today.timeInMillis)
        
        // Set calendar to current month
        val currentMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
        
        _state.update { 
            it.copy(
                calendar = currentMonth,
                selectedDate = todayMillis
            )
        }
        selectDate(todayMillis)
    }
    
    fun toggleTaskComplete(task: Task) {
        val uid = userId ?: return
        
        viewModelScope.launch {
            val isCompleting = !task.isCompleted
            val updatedTask = task.copy(
                isCompleted = isCompleting,
                completedAt = if (isCompleting) System.currentTimeMillis() else null
            )
            
            taskRepository.updateTask(uid, task.listId, updatedTask)
        }
    }
    
    fun refresh() {
        loadTasks()
    }
}

