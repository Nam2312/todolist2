package com.example.todolist2.presentation.navigation

/**
 * Navigation routes for all screens
 */
sealed class Screen(val route: String) {
    // Module 1: Authentication
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    
    // Main App
    object Home : Screen("home")
    object TaskList : Screen("task_list/{listId}") {
        fun createRoute(listId: String) = "task_list/$listId"
    }
    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: String) = "task_detail/$taskId"
    }
    
    // Module 2: Focus Mode
    object Focus : Screen("focus")
    object FocusActive : Screen("focus_active/{taskId}") {
        fun createRoute(taskId: String) = "focus_active/$taskId"
    }
    
    // Module 3: Gamification & Stats
    object Stats : Screen("stats")
    object Badges : Screen("badges")
    
    // Profile & Settings (Module 1)
    object Profile : Screen("profile")
    object Settings : Screen("settings")
}


