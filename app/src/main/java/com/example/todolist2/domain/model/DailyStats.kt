package com.example.todolist2.domain.model

/**
 * Module 3: Daily statistics for analytics
 */
data class DailyStats(
    val date: String, // Format: YYYY-MM-DD
    val tasksCompleted: Int = 0,
    val tasksCreated: Int = 0,
    val focusMinutes: Int = 0,
    val pointsEarned: Int = 0
)

data class WeeklyStats(
    val weekStart: String,
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val focusHours: Float = 0f,
    val completionRate: Float = 0f,
    val dailyStats: List<DailyStats> = emptyList()
)


