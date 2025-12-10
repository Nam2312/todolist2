package com.example.todolist2.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.todolist2.presentation.auth.forgotpassword.ForgotPasswordScreen
import com.example.todolist2.presentation.auth.login.LoginScreen
import com.example.todolist2.presentation.auth.signup.SignUpScreen
import com.example.todolist2.presentation.auth.splash.SplashScreen
import com.example.todolist2.presentation.focus.FocusActiveScreen
import com.example.todolist2.presentation.group.CreateGroupScreen
import com.example.todolist2.presentation.group.GroupDetailScreen
import com.example.todolist2.presentation.group.GroupListScreen
import com.example.todolist2.presentation.group.JoinGroupScreen
import com.example.todolist2.presentation.settings.SettingsScreen
import com.example.todolist2.presentation.task.TaskDetailScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Module 1: Authentication
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        
        composable(Screen.SignUp.route) {
            SignUpScreen(navController = navController)
        }
        
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }
        
        composable(Screen.Home.route) {
            com.example.todolist2.presentation.home.HomeScreen(mainNavController = navController)
        }
        
        // Module 2: Focus Mode
        composable(
            route = Screen.FocusActive.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            FocusActiveScreen(
                navController = navController,
                taskId = taskId
            )
        }
        
        // Module 2: Task Detail
        composable(
            route = Screen.TaskDetail.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            TaskDetailScreen(
                navController = navController,
                taskId = taskId
            )
        }
        
        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        
        // Groups
        composable(Screen.Groups.route) {
            GroupListScreen(navController = navController)
        }
        
        composable(Screen.CreateGroup.route) {
            CreateGroupScreen(navController = navController)
        }
        
        composable(Screen.JoinGroup.route) {
            JoinGroupScreen(navController = navController)
        }
        
        composable(
            route = Screen.GroupDetail.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            GroupDetailScreen(
                navController = navController,
                groupId = groupId
            )
        }
    }
}

