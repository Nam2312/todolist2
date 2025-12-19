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

enum class TaskFilter {
    ALL, ACTIVE, COMPLETED, OVERDUE
}

data class TaskListState(
    val tasks: List<Task> = emptyList(),
    val filteredTasks: List<Task> = emptyList(),
    val lists: List<TodoList> = emptyList(),
    val selectedListId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddTaskDialog: Boolean = false,
    val showAddListDialog: Boolean = false,
    val editingTask: Task? = null,
    val userDisplayName: String = "",
    val searchQuery: String = "",
    val currentFilter: TaskFilter = TaskFilter.ALL
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
        loadLists()
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
    
    private var tasksJob: kotlinx.coroutines.Job? = null
    
    private fun loadTasks() {
        val uid = userId ?: return
        
        // Cancel previous job if exists
        tasksJob?.cancel()
        
        tasksJob = viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                // Load tasks based on selected list
                val listId = _state.value.selectedListId
                val tasksFlow = if (listId != null) {
                    taskRepository.observeTasks(uid, listId)
                } else {
                    taskRepository.observeAllTasks(uid)
                }
                
                tasksFlow
                    .catch { e ->
                        _state.update { it.copy(error = e.message ?: "Lỗi khi tải dữ liệu", isLoading = false) }
                    }
                    .collect { tasks ->
                        _state.update { 
                            it.copy(tasks = tasks, isLoading = false, error = null)
                        }
                        // Apply filters after tasks are updated
                        applyFilters()
                    }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Lỗi không xác định", isLoading = false) }
            }
        }
    }
    
    private fun loadTasksForList(listId: String?) {
        val uid = userId ?: return
        
        // Cancel previous job if exists
        tasksJob?.cancel()
        
        tasksJob = viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                val tasksFlow = if (listId != null) {
                    taskRepository.observeTasks(uid, listId)
                } else {
                    taskRepository.observeAllTasks(uid)
                }
                
                tasksFlow
                    .catch { e ->
                        _state.update { it.copy(error = e.message ?: "Lỗi khi tải dữ liệu", isLoading = false) }
                    }
                    .collect { tasks ->
                        _state.update { 
                            it.copy(tasks = tasks, isLoading = false, error = null)
                        }
                        // Apply filters after tasks are updated
                        applyFilters()
                    }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Lỗi không xác định", isLoading = false) }
            }
        }
    }
    
    private fun loadLists() {
        val uid = userId ?: return
        
        viewModelScope.launch {
            taskRepository.observeLists(uid)
                .catch { e ->
                    _state.update { it.copy(error = e.message) }
                }
                .collect { lists ->
                    _state.update { it.copy(lists = lists) }
                }
        }
    }
    
    fun showAddTaskDialog() {
        _state.update { it.copy(showAddTaskDialog = true) }
    }
    
    fun hideAddTaskDialog() {
        _state.update { it.copy(showAddTaskDialog = false) }
    }
    
    fun addTask(
        title: String,
        description: String = "",
        priority: com.example.todolist2.domain.model.Priority = com.example.todolist2.domain.model.Priority.MEDIUM,
        dueDate: Long? = null,
        reminderTime: Long? = null,
        tags: List<String> = emptyList()
    ) {
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
                priority = priority,
                dueDate = dueDate,
                reminderTime = reminderTime,
                tags = tags,
                createdAt = System.currentTimeMillis()
            )
            
            taskRepository.createTask(uid, listId, task)
        }
    }
    
    fun toggleTaskComplete(task: Task) {
        val uid = userId ?: return
        
        // Don't allow toggling if task is archived
        if (task.isArchived) {
            return
        }
        
        viewModelScope.launch {
            val isCompleting = !task.isCompleted
            
            if (isCompleting) {
                // Archive task when completing
                val archiveResult = taskRepository.archiveTask(uid, task.listId, task.id)
                
                if (archiveResult is Resource.Success) {
                    // Calculate points based on priority
                    var points = Constants.XP_PER_TASK
                    when (task.priority.value) {
                        3 -> points += Constants.XP_PER_PRIORITY_HIGH // HIGH
                        4 -> points += Constants.XP_PER_PRIORITY_URGENT // URGENT
                    }
                    
                    // Add points (this also updates level automatically)
                    try {
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
                    } catch (e: Exception) {
                        // Log error but don't crash
                        e.printStackTrace()
                    }
                }
            } else {
                // Unarchive task when uncompleting
                val unarchiveResult = taskRepository.unarchiveTask(uid, task.listId, task.id)
                
                if (unarchiveResult is Resource.Success) {
                    try {
                        // If uncompleting, decrease tasksCompleted count
                        val userResult = authRepository.getUserProfile(uid)
                        if (userResult is Resource.Success) {
                            val user = userResult.data
                            val updatedUser = user.copy(
                                tasksCompleted = (user.tasksCompleted - 1).coerceAtLeast(0)
                            )
                            authRepository.updateUserProfile(updatedUser)
                        }
                    } catch (e: Exception) {
                        // Log error but don't crash
                        e.printStackTrace()
                    }
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
    
    fun updateTask(
        task: Task,
        title: String,
        description: String = "",
        priority: com.example.todolist2.domain.model.Priority? = null,
        dueDate: Long? = null,
        reminderTime: Long? = null,
        tags: List<String>? = null
    ) {
        val uid = userId ?: return
        
        viewModelScope.launch {
            val updatedTask = task.copy(
                title = title.trim(),
                description = description.trim(),
                priority = priority ?: task.priority,
                dueDate = dueDate,
                reminderTime = reminderTime,
                tags = tags ?: task.tags
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
    
    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        applyFilters()
    }
    
    fun setFilter(filter: TaskFilter) {
        _state.update { it.copy(currentFilter = filter) }
        applyFilters()
    }
    
    private fun applyFilters() {
        val state = _state.value
        var filtered = state.tasks
        
        // Apply filter
        filtered = when (state.currentFilter) {
            TaskFilter.ALL -> filtered
            TaskFilter.ACTIVE -> filtered.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> filtered.filter { it.isCompleted }
            TaskFilter.OVERDUE -> filtered.filter { 
                !it.isCompleted && it.dueDate != null && it.dueDate < System.currentTimeMillis()
            }
        }
        
        // Apply search
        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.lowercase()
            filtered = filtered.filter { task ->
                task.title.lowercase().contains(query) ||
                task.description.lowercase().contains(query) ||
                task.tags.any { it.lowercase().contains(query) }
            }
        }
        
        _state.update { it.copy(filteredTasks = filtered) }
    }
    
    fun selectList(listId: String?) {
        _state.update { it.copy(selectedListId = listId) }
        // Load tasks for the newly selected list
        loadTasksForList(listId)
    }
    
    fun createList(name: String, color: String = "#6200EE") {
        val uid = userId ?: return
        
        viewModelScope.launch {
            val list = TodoList(
                id = UUID.randomUUID().toString(),
                userId = uid,
                name = name,
                color = color
            )
            taskRepository.createList(uid, list)
        }
    }
    
    fun deleteList(listId: String) {
        val uid = userId ?: return
        
        viewModelScope.launch {
            taskRepository.deleteList(uid, listId)
            if (_state.value.selectedListId == listId) {
                _state.update { it.copy(selectedListId = null) }
                loadTasks()
            }
        }
    }
    
    fun showAddListDialog() {
        _state.update { it.copy(showAddListDialog = true) }
    }
    
    fun hideAddListDialog() {
        _state.update { it.copy(showAddListDialog = false) }
    }
    
    fun refresh() {
        loadTasks()
        loadLists()
    }
}

