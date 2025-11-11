package com.example.todolist2.domain.model

/**
 * Module 2: Focus Mode (Pomodoro) domain model
 */
data class FocusSession(
    val id: String = "",
    val userId: String = "",
    val taskId: String? = null,
    val taskTitle: String = "",
    val durationInMinutes: Int = 25, // Default Pomodoro duration
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val completed: Boolean = false,
    val pointsEarned: Int = 0
)

enum class FocusDuration(val minutes: Int, val points: Int) {
    SHORT(15, 10),
    POMODORO(25, 25),
    LONG(50, 50)
}


