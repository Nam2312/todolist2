package com.example.todolist2.domain.model

/**
 * Module 1: User domain model
 * Represents user profile with gamification stats
 * 
 * NOTE: Password is NOT stored here! 
 * Firebase Authentication handles password securely.
 */
data class User(
    val id: String = "",
    val email: String = "",
    val username: String = "",  // Changed from displayName
    val avatarUrl: String = "",
    val preferredTheme: ThemePreference = ThemePreference.SYSTEM,
    val createdAt: Long = System.currentTimeMillis(),
    
    // Gamification stats (Module 3)
    val totalPoints: Int = 0,
    val currentLevel: Int = 1,
    val currentStreak: Int = 0,
    val tasksCompleted: Int = 0,
    val badgesEarned: List<String> = emptyList()
)
// Password is managed by Firebase Authentication, not stored in Firestore!

enum class ThemePreference {
    LIGHT, DARK, SYSTEM
}

