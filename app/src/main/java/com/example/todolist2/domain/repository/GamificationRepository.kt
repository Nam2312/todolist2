package com.example.todolist2.domain.repository

import com.example.todolist2.domain.model.Badge
import com.example.todolist2.domain.model.DailyStats
import com.example.todolist2.domain.model.WeeklyStats
import com.example.todolist2.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Module 3: Gamification & Statistics repository interface
 */
interface GamificationRepository {
    // User stats
    suspend fun getUserStats(userId: String): Resource<UserStats>
    fun observeUserStats(userId: String): Flow<UserStats>
    
    // Badges
    suspend fun getUserBadges(userId: String): Resource<List<Badge>>
    fun observeUserBadges(userId: String): Flow<List<Badge>>
    suspend fun checkAndUnlockBadges(userId: String): Resource<List<Badge>>
    
    // Statistics
    suspend fun getDailyStats(userId: String, date: String): Resource<DailyStats>
    suspend fun getWeeklyStats(userId: String, weekStart: String): Resource<WeeklyStats>
    fun observeDailyStats(userId: String, days: Int): Flow<List<DailyStats>>
    
    // Points & Level
    suspend fun addPoints(userId: String, points: Int): Resource<Int>
    suspend fun calculateLevel(totalPoints: Int): Int
    suspend fun updateUserLevel(userId: String): Resource<Unit>
    
    // Streaks
    suspend fun updateStreak(userId: String): Resource<Int>
    suspend fun getCurrentStreak(userId: String): Resource<Int>
}

data class UserStats(
    val totalPoints: Int = 0,
    val currentLevel: Int = 1,
    val currentStreak: Int = 0,
    val tasksCompleted: Int = 0,
    val focusSessionsCompleted: Int = 0,
    val totalFocusMinutes: Int = 0,
    val badgesEarned: Int = 0
)








