package com.example.todolist2.util

object Constants {
    
    // XP & Gamification
    const val XP_PER_TASK = 10
    const val XP_PER_PRIORITY_HIGH = 5
    const val XP_PER_PRIORITY_URGENT = 10
    const val XP_PER_FOCUS_SESSION = 25
    const val XP_FOR_STREAK_BONUS = 50
    
    // Level system
    const val XP_PER_LEVEL = 100
    
    // Focus Mode
    const val FOCUS_DURATION_SHORT = 15
    const val FOCUS_DURATION_POMODORO = 25
    const val FOCUS_DURATION_LONG = 50
    
    // Notifications
    const val NOTIFICATION_CHANNEL_ID = "taskmaster_notifications"
    const val NOTIFICATION_CHANNEL_NAME = "TaskMaster Reminders"
    
    // Preferences
    const val PREF_THEME = "theme_preference"
    const val PREF_NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val PREF_FOCUS_DEFAULT_DURATION = "focus_default_duration"
    
    // Collections (Firestore)
    const val COLLECTION_USERS = "users"
    const val COLLECTION_LISTS = "lists"
    const val COLLECTION_TASKS = "tasks"
    const val COLLECTION_FOCUS_SESSIONS = "focus_sessions"
    
    // Task priorities
    const val PRIORITY_LOW = 1
    const val PRIORITY_MEDIUM = 2
    const val PRIORITY_HIGH = 3
    const val PRIORITY_URGENT = 4
}







