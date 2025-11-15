package com.example.todolist2.presentation.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.todolist2.domain.model.ThemePreference
import com.example.todolist2.domain.repository.GamificationRepository
import com.example.todolist2.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    // Navigate to login after successful logout
    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) {
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hồ sơ cá nhân") },
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = { viewModel.saveProfile() }) {
                            Icon(Icons.Default.Check, contentDescription = "Lưu")
                        }
                        IconButton(onClick = { viewModel.onCancelEdit() }) {
                            Icon(Icons.Default.Close, contentDescription = "Hủy")
                        }
                    } else {
                        IconButton(onClick = { viewModel.onEditClick() }) {
                            Icon(Icons.Default.Edit, contentDescription = "Chỉnh sửa")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            if (state.isLoading && state.user == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                state.user?.let { user ->
                    ProfileHeader(
                        user = user,
                        isEditing = state.isEditing,
                        editedUsername = state.editedUsername,
                        editedAvatarUrl = state.editedAvatarUrl,
                        onUsernameChange = viewModel::onUsernameChange,
                        onAvatarUrlChange = viewModel::onAvatarUrlChange
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    ProfileStats(
                        totalPoints = state.totalPoints,
                        currentLevel = state.currentLevel,
                        currentStreak = state.currentStreak,
                        tasksCompleted = state.tasksCompleted
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    ProfileSettings(
                        selectedTheme = state.selectedTheme,
                        isEditing = state.isEditing,
                        onThemeChange = viewModel::onThemeChange
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    ProfileActions(
                        onLogoutClick = viewModel::onLogoutClick
                    )
                }
            }
        }
    }
    
    LaunchedEffect(state.showLogoutDialog) {
        if (state.showLogoutDialog) {
            // Dialog will be shown
        }
    }
    
    if (state.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onLogoutCancel() },
            title = { Text("Đăng xuất") },
            text = { Text("Bạn có chắc chắn muốn đăng xuất?") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        viewModel.onLogoutConfirm()
                    }
                ) {
                    Text("Đăng xuất")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        viewModel.onLogoutCancel()
                    }
                ) {
                    Text("Hủy")
                }
            }
        )
    }
    
    state.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or handle error
        }
    }
}

@Composable
fun ProfileHeader(
    user: com.example.todolist2.domain.model.User,
    isEditing: Boolean,
    editedUsername: String,
    editedAvatarUrl: String,
    onUsernameChange: (String) -> Unit,
    onAvatarUrlChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            if (user.avatarUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(user.avatarUrl),
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isEditing) {
            OutlinedTextField(
                value = editedUsername,
                onValueChange = onUsernameChange,
                label = { Text("Tên người dùng") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = editedAvatarUrl,
                onValueChange = onAvatarUrlChange,
                label = { Text("URL Avatar") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        } else {
            Text(
                text = user.username.ifEmpty { "Người dùng" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProfileStats(
    totalPoints: Int,
    currentLevel: Int,
    currentStreak: Int,
    tasksCompleted: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Thống kê",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Default.Star,
                    label = "Điểm",
                    value = totalPoints.toString()
                )
                StatItem(
                    icon = Icons.Default.Info,
                    label = "Cấp độ",
                    value = currentLevel.toString()
                )
                StatItem(
                    icon = Icons.Default.Favorite,
                    label = "Chuỗi",
                    value = "$currentStreak ngày"
                )
                StatItem(
                    icon = Icons.Default.CheckCircle,
                    label = "Hoàn thành",
                    value = tasksCompleted.toString()
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileSettings(
    selectedTheme: ThemePreference,
    isEditing: Boolean,
    onThemeChange: (ThemePreference) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Cài đặt",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isEditing) {
                Text(
                    text = "Chủ đề",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                ThemePreference.entries.forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedTheme == theme,
                            onClick = { onThemeChange(theme) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (theme) {
                                ThemePreference.LIGHT -> "Sáng"
                                ThemePreference.DARK -> "Tối"
                                ThemePreference.SYSTEM -> "Theo hệ thống"
                            }
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chủ đề",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = when (selectedTheme) {
                            ThemePreference.LIGHT -> "Sáng"
                            ThemePreference.DARK -> "Tối"
                            ThemePreference.SYSTEM -> "Theo hệ thống"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileActions(
    onLogoutClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        TextButton(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.ExitToApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Đăng xuất",
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

