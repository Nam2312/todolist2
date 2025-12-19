package com.example.todolist2.presentation.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.todolist2.domain.model.Priority
import com.example.todolist2.presentation.navigation.Screen
import com.example.todolist2.util.DateUtils
import java.util.Calendar
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState

/**
 * Module 2: Main Task List Screen
 * Shows all task lists and tasks
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun TaskListScreen(
    navController: NavController,
    viewModel: TaskListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("TaskMaster")
                        if (state.userDisplayName.isNotEmpty()) {
                            Text(
                                text = "Xin chào, ${state.userDisplayName}!",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        navController.navigate(Screen.CalendarView.route)
                    }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Lịch")
                    }
                    IconButton(onClick = { 
                        navController.navigate(Screen.Archive.route)
                    }) {
                        Icon(Icons.Default.List, contentDescription = "Lưu trữ")
                    }
                    IconButton(onClick = { viewModel.showAddListDialog() }) {
                        Icon(Icons.Default.List, contentDescription = "Thêm danh sách")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddTaskDialog() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm công việc")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            var isRefreshing by remember { mutableStateOf(false) }
            
            val pullRefreshState = rememberPullRefreshState(
                refreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    viewModel.refresh()
                    isRefreshing = false
                }
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .pullRefresh(pullRefreshState)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                // Lists Section
                item {
                    if (state.lists.isNotEmpty()) {
                        Column {
                            Text(
                                text = "Danh sách",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // "All" option
                                item {
                                    FilterChip(
                                        selected = state.selectedListId == null,
                                        onClick = { viewModel.selectList(null) },
                                        label = { Text("Tất cả") }
                                    )
                                }
                                // List items
                                items(state.lists) { list ->
                                    var showDeleteConfirm by remember { mutableStateOf(false) }
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        FilterChip(
                                            selected = state.selectedListId == list.id,
                                            onClick = { viewModel.selectList(list.id) },
                                            label = { 
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(12.dp)
                                                            .background(
                                                                androidx.compose.ui.graphics.Color(
                                                                    android.graphics.Color.parseColor(list.color)
                                                                ),
                                                                androidx.compose.foundation.shape.CircleShape
                                                            )
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(list.name)
                                                }
                                            }
                                        )
                                        IconButton(
                                            onClick = { showDeleteConfirm = true },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Xóa danh sách",
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                    
                                    // Delete confirmation dialog
                                    if (showDeleteConfirm) {
                                        AlertDialog(
                                            onDismissRequest = { showDeleteConfirm = false },
                                            title = { Text("Xóa danh sách") },
                                            text = { 
                                                Text("Bạn có chắc chắn muốn xóa danh sách \"${list.name}\"? Tất cả công việc trong danh sách này cũng sẽ bị xóa.")
                                            },
                                            confirmButton = {
                                                TextButton(
                                                    onClick = {
                                                        viewModel.deleteList(list.id)
                                                        showDeleteConfirm = false
                                                    }
                                                ) {
                                                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                                                }
                                            },
                                            dismissButton = {
                                                TextButton(onClick = { showDeleteConfirm = false }) {
                                                    Text("Hủy")
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                
                // Search Bar
                item {
                    var searchQuery by remember { mutableStateOf(state.searchQuery) }
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
                            viewModel.updateSearchQuery(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Tìm kiếm công việc...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Tìm kiếm")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    viewModel.updateSearchQuery("")
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Xóa")
                                }
                            }
                        },
                        singleLine = true
                    )
                }
                
                // Filter Chips
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.example.todolist2.presentation.task.TaskFilter.entries.forEach { filter ->
                            FilterChip(
                                selected = state.currentFilter == filter,
                                onClick = { viewModel.setFilter(filter) },
                                label = {
                                    Text(
                                        when (filter) {
                                            com.example.todolist2.presentation.task.TaskFilter.ALL -> "Tất cả"
                                            com.example.todolist2.presentation.task.TaskFilter.ACTIVE -> "Đang làm"
                                            com.example.todolist2.presentation.task.TaskFilter.COMPLETED -> "Hoàn thành"
                                            com.example.todolist2.presentation.task.TaskFilter.OVERDUE -> "Quá hạn"
                                        }
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Display filtered tasks
                if (state.filteredTasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (state.searchQuery.isNotBlank() || state.currentFilter != TaskFilter.ALL) {
                                    // Search/Filter empty state
                                    Text("🔍", style = MaterialTheme.typography.displayMedium)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Không tìm thấy công việc nào",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                } else {
                                    // General empty state (no tasks in selected list)
                                    Text("🎯", style = MaterialTheme.typography.displayMedium)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        if (state.selectedListId != null) {
                                            "Danh sách này chưa có công việc nào"
                                        } else {
                                            "Chưa có công việc nào"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Bắt đầu bằng cách thêm công việc đầu tiên!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(onClick = { viewModel.showAddTaskDialog() }) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Thêm công việc")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(
                        items = state.filteredTasks,
                        key = { it.id }
                    ) { task ->
                        TaskItemCard(
                            task = task,
                            onToggleComplete = { viewModel.toggleTaskComplete(task) },
                            onClick = { 
                                navController.navigate(Screen.TaskDetail.createRoute(task.id))
                            },
                            onDelete = {
                                viewModel.deleteTask(task)
                            }
                        )
                    }
                }
                }
                
                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
        
        // Add Task Dialog
        if (state.showAddTaskDialog) {
            AddTaskDialog(
                onDismiss = { viewModel.hideAddTaskDialog() },
                onConfirm = { title, description, priority, dueDate, reminderTime, tags ->
                    viewModel.addTask(title, description, priority, dueDate, reminderTime, tags)
                }
            )
        }
        
        // Edit Task Dialog
        state.editingTask?.let { task ->
            EditTaskDialog(
                task = task,
                onDismiss = { viewModel.hideEditTaskDialog() },
                onSave = { title, description, priority, dueDate, reminderTime, tags ->
                    viewModel.updateTask(task, title, description, priority, dueDate, reminderTime, tags)
                },
                onDelete = {
                    viewModel.deleteTask(task)
                }
            )
        }
        
        // Add List Dialog
        if (state.showAddListDialog) {
            AddListDialog(
                onDismiss = { viewModel.hideAddListDialog() },
                onConfirm = { name, color ->
                    viewModel.createList(name, color)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItemCard(
    task: com.example.todolist2.domain.model.Task,
    onToggleComplete: () -> Unit,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart && onDelete != null) {
                onDelete()
                true
            } else {
                false
            }
        }
    )
    
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error)
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Xóa",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleComplete() }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium,
                            color = if (task.isCompleted) 
                                MaterialTheme.colorScheme.onSurfaceVariant 
                            else 
                                MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Priority indicator
                        PriorityBadge(priority = task.priority)
                    }
                    
                    if (task.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Tags and metadata row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tags
                        if (task.tags.isNotEmpty()) {
                            task.tags.take(3).forEach { tag ->
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    shape = MaterialTheme.shapes.extraSmall
                                ) {
                                    Text(
                                        text = tag,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            if (task.tags.size > 3) {
                                Text(
                                    text = "+${task.tags.size - 3}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        
                            // Due date
                            task.dueDate?.let { dueDate ->
                                val isOverdue = !task.isCompleted && dueDate < System.currentTimeMillis()
                                val isToday = com.example.todolist2.util.DateUtils.isToday(dueDate)
                                val isTomorrow = com.example.todolist2.util.DateUtils.isTomorrow(dueDate)
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DateRange,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isOverdue) 
                                            MaterialTheme.colorScheme.error 
                                        else if (isToday) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                Text(
                                    text = when {
                                        isToday -> "Hôm nay"
                                        isTomorrow -> "Ngày mai"
                                        isOverdue -> "Quá hạn"
                                        else -> com.example.todolist2.util.DateUtils.formatDate(dueDate, "dd/MM")
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isOverdue) 
                                        MaterialTheme.colorScheme.error 
                                    else if (isToday) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PriorityBadge(priority: com.example.todolist2.domain.model.Priority) {
    val (color, textColor, label) = when (priority) {
        com.example.todolist2.domain.model.Priority.LOW -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Thấp"
        )
        com.example.todolist2.domain.model.Priority.MEDIUM -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "TB"
        )
        com.example.todolist2.domain.model.Priority.HIGH -> Triple(
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.onTertiary,
            "Cao"
        )
        com.example.todolist2.domain.model.Priority.URGENT -> Triple(
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.onError,
            "Gấp"
        )
    }
    
    Surface(
        color = color,
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontSize = 10.sp
        )
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, com.example.todolist2.domain.model.Priority, Long?, Long?, List<String>) -> Unit
) {
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(com.example.todolist2.domain.model.Priority.MEDIUM) }
    var dueDate by remember { mutableStateOf<Long?>(null) }
    var reminderTime by remember { mutableStateOf<Long?>(null) }
    var tags by remember { mutableStateOf<List<String>>(emptyList()) }
    var newTag by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm công việc mới") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Tên công việc *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = taskDescription,
                    onValueChange = { taskDescription = it },
                    label = { Text("Mô tả") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Priority Selector
                Column {
                    Text(
                        text = "Ưu tiên",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.example.todolist2.domain.model.Priority.entries.forEach { priority ->
                            FilterChip(
                                selected = selectedPriority == priority,
                                onClick = { selectedPriority = priority },
                                label = {
                                    Text(
                                        when (priority) {
                                            com.example.todolist2.domain.model.Priority.LOW -> "Thấp"
                                            com.example.todolist2.domain.model.Priority.MEDIUM -> "Trung bình"
                                            com.example.todolist2.domain.model.Priority.HIGH -> "Cao"
                                            com.example.todolist2.domain.model.Priority.URGENT -> "Gấp"
                                        }
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                // Due Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                    ) {
                    Text("Hạn chót")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (dueDate != null) {
                            Text(
                                text = com.example.todolist2.util.DateUtils.formatDate(dueDate!!),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            IconButton(onClick = { dueDate = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Xóa")
                            }
                        }
                        TextButton(onClick = { showDatePicker = true }) {
                            Text(if (dueDate == null) "Chọn ngày" else "Đổi ngày")
                        }
                    }
                }
                
                // Reminder Time
                if (dueDate != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Nhắc nhở")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (reminderTime != null) {
                                Text(
                                    text = com.example.todolist2.util.DateUtils.formatTime(reminderTime!!),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                IconButton(onClick = { reminderTime = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Xóa")
                                }
                            }
                            TextButton(onClick = { showTimePicker = true }) {
                                Text(if (reminderTime == null) "Chọn giờ" else "Đổi giờ")
                            }
                        }
                    }
                }
                
                // Tags
                Column {
                    Text(
                        text = "Nhãn",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newTag,
                            onValueChange = { newTag = it },
                            label = { Text("Thêm nhãn") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            trailingIcon = {
                                if (newTag.isNotBlank()) {
                                    IconButton(onClick = {
                                        if (newTag.isNotBlank() && !tags.contains(newTag.trim())) {
                                            tags = tags + newTag.trim()
                                            newTag = ""
                                        }
                                    }) {
                                        Icon(Icons.Default.Add, contentDescription = "Thêm")
                                    }
                                }
                            }
                        )
                    }
                    if (tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            tags.forEach { tag ->
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(tag, style = MaterialTheme.typography.bodySmall)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { tags = tags.filter { it != tag } },
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Xóa",
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (taskTitle.isNotBlank()) {
                        onConfirm(taskTitle.trim(), taskDescription.trim(), selectedPriority, dueDate, reminderTime, tags)
                        onDismiss()
                    }
                },
                enabled = taskTitle.isNotBlank()
            ) {
                Text("Thêm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
    
    // Date Picker
    if (showDatePicker) {
        DatePickerDialog(
            initialDate = dueDate ?: System.currentTimeMillis(),
            onDateSelected = { date ->
                dueDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
    
    // Time Picker
    if (showTimePicker && dueDate != null) {
        TimePickerDialog(
            initialTime = reminderTime ?: System.currentTimeMillis(),
            onTimeSelected = { time ->
                // Combine date and time
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = dueDate!!
                    val timeCalendar = Calendar.getInstance().apply { timeInMillis = time }
                    set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                }
                reminderTime = calendar.timeInMillis
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: com.example.todolist2.domain.model.Task,
    onDismiss: () -> Unit,
    onSave: (String, String, Priority?, Long?, Long?, List<String>?) -> Unit,
    onDelete: () -> Unit
) {
    var taskTitle by remember { mutableStateOf(task.title) }
    var taskDescription by remember { mutableStateOf(task.description) }
    var selectedPriority by remember { mutableStateOf(task.priority) }
    var dueDate by remember { mutableStateOf(task.dueDate) }
    var reminderTime by remember { mutableStateOf(task.reminderTime) }
    var tags by remember { mutableStateOf(task.tags) }
    var newTag by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sửa công việc", modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Xóa",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Tên công việc *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = taskDescription,
                    onValueChange = { taskDescription = it },
                    label = { Text("Mô tả") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Priority Selector
                Column {
                    Text(
                        text = "Ưu tiên",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Priority.entries.forEach { priority ->
                            FilterChip(
                                selected = selectedPriority == priority,
                                onClick = { selectedPriority = priority },
                                label = {
                                    Text(
                                        when (priority) {
                                            Priority.LOW -> "Thấp"
                                            Priority.MEDIUM -> "Trung bình"
                                            Priority.HIGH -> "Cao"
                                            Priority.URGENT -> "Gấp"
                                        }
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                // Due Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hạn chót")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (dueDate != null) {
                            Text(
                                text = DateUtils.formatDate(dueDate!!),
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            IconButton(onClick = { dueDate = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Xóa")
                            }
                        }
                        TextButton(onClick = { showDatePicker = true }) {
                            Text(if (dueDate == null) "Chọn ngày" else "Đổi ngày")
                        }
                    }
                }
                
                // Reminder Time
                if (dueDate != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Nhắc nhở")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (reminderTime != null) {
                                Text(
                                    text = DateUtils.formatTime(reminderTime!!),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                IconButton(onClick = { reminderTime = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Xóa")
                                }
                            }
                            TextButton(onClick = { showTimePicker = true }) {
                                Text(if (reminderTime == null) "Chọn giờ" else "Đổi giờ")
                            }
                        }
                    }
                }
                
                // Tags
                Column {
                    Text(
                        text = "Nhãn",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newTag,
                            onValueChange = { newTag = it },
                            label = { Text("Thêm nhãn") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            trailingIcon = {
                                if (newTag.isNotBlank()) {
                                    IconButton(onClick = {
                                        if (newTag.isNotBlank() && !tags.contains(newTag.trim())) {
                                            tags = tags + newTag.trim()
                                            newTag = ""
                                        }
                                    }) {
                                        Icon(Icons.Default.Add, contentDescription = "Thêm")
                                    }
                                }
                            }
                        )
                    }
                    if (tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            tags.forEach { tag ->
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(tag, style = MaterialTheme.typography.bodySmall)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { tags = tags.filter { it != tag } },
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Xóa",
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Text(
                    text = "Tạo: ${DateUtils.formatDate(task.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (taskTitle.isNotBlank()) {
                        onSave(taskTitle.trim(), taskDescription.trim(), selectedPriority, dueDate, reminderTime, tags)
                        onDismiss()
                    }
                },
                enabled = taskTitle.isNotBlank()
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
    
    // Date Picker
    if (showDatePicker) {
        DatePickerDialog(
            initialDate = dueDate ?: System.currentTimeMillis(),
            onDateSelected = { date ->
                dueDate = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
    
    // Time Picker
    if (showTimePicker && dueDate != null) {
        TimePickerDialog(
            initialTime = reminderTime ?: System.currentTimeMillis(),
            onTimeSelected = { time ->
                val calendar = Calendar.getInstance().apply {
                    timeInMillis = dueDate!!
                    val timeCalendar = Calendar.getInstance().apply { timeInMillis = time }
                    set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
                }
                reminderTime = calendar.timeInMillis
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Xóa công việc") },
            text = { Text("Bạn có chắc chắn muốn xóa công việc này?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                        onDismiss()
                    }
                ) {
                    Text("Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    initialDate: Long,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Chọn ngày",
                    style = MaterialTheme.typography.titleLarge
                )
                DatePicker(state = datePickerState)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                onDateSelected(it)
                            }
                        }
                    ) {
                        Text("Chọn")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialTime: Long,
    onTimeSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = remember { Calendar.getInstance().apply { timeInMillis = initialTime } }
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE)
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Chọn giờ",
                    style = MaterialTheme.typography.titleLarge
                )
                TimePicker(state = timePickerState)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            val resultCalendar = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                set(Calendar.MINUTE, timePickerState.minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            onTimeSelected(resultCalendar.timeInMillis)
                        }
                    ) {
                        Text("Chọn")
                    }
                }
            }
        }
    }
}

@Composable
fun AddListDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var listName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#6200EE") }
    
    val colors = listOf(
        "#6200EE", "#03DAC6", "#018786", "#B00020",
        "#FF6D00", "#3700B3", "#03A9F4", "#4CAF50"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm danh sách mới") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = listName,
                    onValueChange = { listName = it },
                    label = { Text("Tên danh sách *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Column {
                    Text(
                        text = "Màu sắc",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Color(android.graphics.Color.parseColor(color)),
                                        CircleShape
                                    )
                                    .then(
                                        if (selectedColor == color) {
                                            Modifier.border(
                                                2.dp,
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            )
                                        } else Modifier
                                    )
                                    .clickable { selectedColor = color },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColor == color) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (listName.isNotBlank()) {
                        onConfirm(listName.trim(), selectedColor)
                        onDismiss()
                    }
                },
                enabled = listName.isNotBlank()
            ) {
                Text("Thêm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}


