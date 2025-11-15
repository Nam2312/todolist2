package com.example.todolist2.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.todolist2.presentation.task.TaskListScreen

/**
 * Main Home Screen with Bottom Navigation
 * Combines all 3 modules: Task Management, Focus Mode, Analytics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    mainNavController: NavController
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Tasks.route,
            modifier = Modifier.padding(padding)
        ) {
            // Module 2: Task Management
            composable(BottomNavItem.Tasks.route) {
                TaskListScreen(navController = mainNavController)
            }
            
            // Module 3: Analytics & Gamification
            composable(BottomNavItem.Stats.route) {
                com.example.todolist2.presentation.gamification.StatsScreen()
            }
            
            // Module 1: Profile
            composable(BottomNavItem.Profile.route) {
                com.example.todolist2.presentation.profile.ProfileScreen(navController = mainNavController)
            }
        }
    }
}

sealed class BottomNavItem(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Tasks : BottomNavItem("tasks", "Công việc", Icons.Default.Check)
    object Focus : BottomNavItem("focus", "Tập trung", Icons.Default.PlayArrow)
    object Stats : BottomNavItem("stats", "Thống kê", Icons.Default.Star)
    object Profile : BottomNavItem("profile", "Cá nhân", Icons.Default.Person)
}

val bottomNavItems = listOf(
    BottomNavItem.Tasks,
    BottomNavItem.Stats,
    BottomNavItem.Profile
)

// Placeholder screens (will be implemented in next steps)
@Composable
fun FocusPlaceholderScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Chế độ tập trung (Focus Mode)", style = MaterialTheme.typography.titleLarge)
            Text("Sẽ được triển khai tiếp", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun StatsPlaceholderScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Thống kê & Gamification", style = MaterialTheme.typography.titleLarge)
            Text("Module 3 - Đang phát triển", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ProfilePlaceholderScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Hồ sơ cá nhân", style = MaterialTheme.typography.titleLarge)
            Text("Module 1 - Đang phát triển", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

