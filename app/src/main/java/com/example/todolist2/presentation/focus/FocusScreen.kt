package com.example.todolist2.presentation.focus

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.todolist2.domain.model.FocusDuration
import com.example.todolist2.domain.model.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    navController: NavController,
    viewModel: FocusViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Chế độ Tập trung")
                        Text(
                            "Pomodoro Timer",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )
        }
    ) { padding ->
        // Show timer UI if running, otherwise show setup UI
        if (state.isTimerRunning) {
            FocusTimerContent(
                state = state,
                viewModel = viewModel,
                navController = navController,
                padding = padding
            )
        } else {
            FocusSetupContent(
                state = state,
                viewModel = viewModel,
                padding = padding
            )
        }
    }
}

@Composable
fun FocusSetupContent(
    state: FocusState,
    viewModel: FocusViewModel,
    padding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Duration Selection
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Thời gian tập trung",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FocusDuration.entries.forEach { duration ->
                        FilterChip(
                            selected = state.selectedDuration == duration,
                            onClick = { viewModel.selectDuration(duration) },
                            label = { 
                                Text("${duration.minutes} phút")
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        // Task Selection
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Chọn công việc (tùy chọn)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                if (state.tasks.isEmpty()) {
                    Text(
                        text = "Không có công việc nào chưa hoàn thành",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            TaskOptionItem(
                                task = null,
                                isSelected = state.selectedTask == null,
                                onClick = { viewModel.selectTask(null) }
                            )
                        }
                        items(state.tasks) { task ->
                            TaskOptionItem(
                                task = task,
                                isSelected = state.selectedTask?.id == task.id,
                                onClick = { viewModel.selectTask(task) }
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Start Button
        Button(
            onClick = { viewModel.startTimer() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !state.isLoading
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Bắt đầu tập trung",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun FocusTimerContent(
    state: FocusState,
    viewModel: FocusViewModel,
    navController: NavController,
    padding: PaddingValues
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Task Info
            state.selectedTask?.let { task ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Công việc",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            // Timer Circle
            val progress = if (state.totalSeconds > 0) {
                (state.totalSeconds - state.remainingSeconds).toFloat() / state.totalSeconds
            } else {
                0f
            }
            
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(1000),
                label = "timer_progress"
            )
            
            Box(
                modifier = Modifier.size(300.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background circle
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.size(300.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 12.dp
                )
                
                // Progress circle
                CircularProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier.size(300.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 12.dp
                )
                
                // Time display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val minutes = state.remainingSeconds / 60
                    val seconds = state.remainingSeconds % 60
                    
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 64.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = if (state.isTimerPaused) "Đã tạm dừng" else "Đang chạy",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.isTimerRunning && !state.isTimerPaused) {
                    OutlinedButton(
                        onClick = { viewModel.pauseTimer() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("⏸", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tạm dừng")
                    }
                } else if (state.isTimerPaused) {
                    Button(
                        onClick = { viewModel.resumeTimer() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tiếp tục")
                    }
                }
                
                OutlinedButton(
                    onClick = { 
                        viewModel.stopTimer()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dừng")
                }
            }
        }
    }
}

@Composable
fun TaskOptionItem(
    task: Task?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task?.title ?: "Không có task cụ thể",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                if (task?.description?.isNotEmpty() == true) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

