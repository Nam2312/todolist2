package com.example.todolist2.data.repository

import com.example.todolist2.data.local.dao.FocusSessionDao
import com.example.todolist2.data.local.dao.TaskDao
import com.example.todolist2.data.remote.firebase.FirebaseAuthDataSource
import com.example.todolist2.data.remote.firebase.FirestoreGamificationDataSource
import com.example.todolist2.domain.model.Badge
import com.example.todolist2.domain.model.BadgeType
import com.example.todolist2.domain.model.DailyStats
import com.example.todolist2.domain.model.WeeklyStats
import com.example.todolist2.domain.model.User
import com.example.todolist2.domain.repository.AuthRepository
import com.example.todolist2.domain.repository.GamificationRepository
import com.example.todolist2.domain.repository.UserStats
import com.example.todolist2.util.Resource
import com.example.todolist2.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Module 3: Gamification repository implementation
 */
@Singleton
class GamificationRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val focusSessionDao: FocusSessionDao,
    private val authRepository: AuthRepository,
    private val firestoreDataSource: FirestoreGamificationDataSource,
    private val firebaseAuthDataSource: FirebaseAuthDataSource
) : GamificationRepository {
    
    override suspend fun getUserStats(userId: String): Resource<UserStats> {
        return try {
            val userResult = authRepository.getUserProfile(userId)
            if (userResult !is Resource.Success) {
                return Resource.Error("Failed to load user")
            }
            
            val user = userResult.data
            val completedTasks = taskDao.getCompletedTasks(userId).map { it.size }.first()
            val focusSessions = focusSessionDao.getCompletedSessionCount(userId)
            val totalFocusMinutes = focusSessionDao.getTotalFocusMinutes(userId, 0) ?: 0
            
            val stats = UserStats(
                totalPoints = user.totalPoints,
                currentLevel = user.currentLevel,
                currentStreak = user.currentStreak,
                tasksCompleted = completedTasks,
                focusSessionsCompleted = focusSessions,
                totalFocusMinutes = totalFocusMinutes,
                badgesEarned = user.badgesEarned.size
            )
            
            Resource.Success(stats)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get user stats")
        }
    }
    
    override fun observeUserStats(userId: String): Flow<UserStats> {
        return combine(
            authRepository.observeUserProfile(userId),
            taskDao.getCompletedTasks(userId),
            focusSessionDao.getAllSessions(userId)
        ) { user, completedTasks, sessions ->
            val completedSessions = sessions.filter { it.completed }
            val totalFocusMinutes = completedSessions.sumOf { it.durationInMinutes }
            
            UserStats(
                totalPoints = user.totalPoints,
                currentLevel = user.currentLevel,
                currentStreak = user.currentStreak,
                tasksCompleted = completedTasks.size,
                focusSessionsCompleted = completedSessions.size,
                totalFocusMinutes = totalFocusMinutes,
                badgesEarned = user.badgesEarned.size
            )
        }
    }
    
    override suspend fun getUserBadges(userId: String): Resource<List<Badge>> {
        return try {
            val userResult = authRepository.getUserProfile(userId)
            if (userResult !is Resource.Success) {
                return Resource.Error("Failed to load user")
            }
            
            val user = userResult.data
            val unlockedBadgeIds = user.badgesEarned.toSet()
            
            val badges = BadgeType.entries.map { badgeType ->
                Badge(
                    id = badgeType.id,
                    name = badgeType.displayName,
                    description = badgeType.description,
                    iconName = badgeType.id,
                    requirement = badgeType.requirement,
                    isUnlocked = unlockedBadgeIds.contains(badgeType.id),
                    unlockedAt = if (unlockedBadgeIds.contains(badgeType.id)) System.currentTimeMillis() else null
                )
            }
            
            Resource.Success(badges)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get badges")
        }
    }
    
    override fun observeUserBadges(userId: String): Flow<List<Badge>> {
        return authRepository.observeUserProfile(userId).map { user ->
            val unlockedBadgeIds = user.badgesEarned.toSet()
            BadgeType.entries.map { badgeType ->
                Badge(
                    id = badgeType.id,
                    name = badgeType.displayName,
                    description = badgeType.description,
                    iconName = badgeType.id,
                    requirement = badgeType.requirement,
                    isUnlocked = unlockedBadgeIds.contains(badgeType.id),
                    unlockedAt = if (unlockedBadgeIds.contains(badgeType.id)) System.currentTimeMillis() else null
                )
            }
        }
    }
    
    override suspend fun checkAndUnlockBadges(userId: String): Resource<List<Badge>> {
        return try {
            val userResult = authRepository.getUserProfile(userId)
            if (userResult !is Resource.Success) {
                return Resource.Error("Failed to load user")
            }
            
            val user = userResult.data
            val completedTasks = taskDao.getCompletedTasks(userId).map { it.size }.first()
            val focusSessions = focusSessionDao.getCompletedSessionCount(userId)
            val unlockedBadgeIds = user.badgesEarned.toMutableSet()
            val newlyUnlocked = mutableListOf<Badge>()
            
            // Check task completion badges
            BadgeType.entries.filter { it.id.startsWith("task_") }.forEach { badgeType ->
                if (!unlockedBadgeIds.contains(badgeType.id) && completedTasks >= badgeType.requirement) {
                    unlockedBadgeIds.add(badgeType.id)
                    newlyUnlocked.add(
                        Badge(
                            id = badgeType.id,
                            name = badgeType.displayName,
                            description = badgeType.description,
                            iconName = badgeType.id,
                            requirement = badgeType.requirement,
                            isUnlocked = true,
                            unlockedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
            
            // Check streak badges
            BadgeType.entries.filter { it.id.startsWith("streak_") }.forEach { badgeType ->
                if (!unlockedBadgeIds.contains(badgeType.id) && user.currentStreak >= badgeType.requirement) {
                    unlockedBadgeIds.add(badgeType.id)
                    newlyUnlocked.add(
                        Badge(
                            id = badgeType.id,
                            name = badgeType.displayName,
                            description = badgeType.description,
                            iconName = badgeType.id,
                            requirement = badgeType.requirement,
                            isUnlocked = true,
                            unlockedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
            
            
            // Check level badges
            BadgeType.entries.filter { it.id.startsWith("level_") }.forEach { badgeType ->
                if (!unlockedBadgeIds.contains(badgeType.id) && user.currentLevel >= badgeType.requirement) {
                    unlockedBadgeIds.add(badgeType.id)
                    newlyUnlocked.add(
                        Badge(
                            id = badgeType.id,
                            name = badgeType.displayName,
                            description = badgeType.description,
                            iconName = badgeType.id,
                            requirement = badgeType.requirement,
                            isUnlocked = true,
                            unlockedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
            
            // Update user badges if new ones unlocked
            if (newlyUnlocked.isNotEmpty()) {
                val updatedUser = user.copy(badgesEarned = unlockedBadgeIds.toList())
                authRepository.updateUserProfile(updatedUser)
            }
            
            Resource.Success(newlyUnlocked)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to check badges")
        }
    }
    
    override suspend fun getDailyStats(userId: String, date: String): Resource<DailyStats> {
        return try {
            val calendar = DateUtils.parseDate(date)
            val startOfDay = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = calendar.timeInMillis
            
            val tasks = taskDao.getCompletedTasks(userId).map { 
                it.filter { task -> 
                    task.completedAt != null && 
                    task.completedAt!! >= startOfDay && 
                    task.completedAt!! < endOfDay 
                }
            }.first()
            
            val allTasks = taskDao.getAllTasks(userId).map {
                it.filter { task ->
                    task.createdAt >= startOfDay && task.createdAt < endOfDay
                }
            }.first()
            
            val sessions = focusSessionDao.getAllSessions(userId).map {
                it.filter { session ->
                    session.startTime >= startOfDay && session.startTime < endOfDay && session.completed
                }
            }.first()
            
            val focusMinutes = sessions.sumOf { it.durationInMinutes.toInt() }
            val pointsEarned = (tasks.size * 10) + sessions.sumOf { it.pointsEarned.toInt() }
            
            Resource.Success(
                DailyStats(
                    date = date,
                    tasksCompleted = tasks.size,
                    tasksCreated = allTasks.size,
                    focusMinutes = focusMinutes,
                    pointsEarned = pointsEarned
                )
            )
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get daily stats")
        }
    }
    
    override suspend fun getWeeklyStats(userId: String, weekStart: String): Resource<WeeklyStats> {
        return try {
            val calendar = DateUtils.parseDate(weekStart)
            val dailyStatsList = mutableListOf<DailyStats>()
            var totalTasks = 0
            var completedTasks = 0
            var totalFocusMinutes = 0
            
            for (i in 0..6) {
                val dateStr = DateUtils.formatDate(calendar)
                val dailyResult = getDailyStats(userId, dateStr)
                if (dailyResult is Resource.Success) {
                    val stats = dailyResult.data
                    dailyStatsList.add(stats)
                    totalTasks += stats.tasksCreated
                    completedTasks += stats.tasksCompleted
                    totalFocusMinutes += stats.focusMinutes
                }
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            
            val completionRate = if (totalTasks > 0) {
                (completedTasks.toFloat() / totalTasks) * 100f
            } else {
                0f
            }
            
            Resource.Success(
                WeeklyStats(
                    weekStart = weekStart,
                    totalTasks = totalTasks,
                    completedTasks = completedTasks,
                    focusHours = totalFocusMinutes / 60f,
                    completionRate = completionRate,
                    dailyStats = dailyStatsList
                )
            )
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get weekly stats")
        }
    }
    
    override fun observeDailyStats(userId: String, days: Int): Flow<List<DailyStats>> {
        // Observe tasks and focus sessions to update stats in real-time
        return combine(
            taskDao.getCompletedTasks(userId),
            taskDao.getAllTasks(userId),
            focusSessionDao.getAllSessions(userId)
        ) { completedTasks, allTasks, sessions ->
            val statsList = mutableListOf<DailyStats>()
            val calendar = Calendar.getInstance()
            
            for (i in 0 until days) {
                val dateStr = DateUtils.formatDate(calendar)
                val startOfDay = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                val endOfDay = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                
                // Filter tasks for this day
                val dayCompletedTasks = completedTasks.filter { task ->
                    task.completedAt != null &&
                    task.completedAt!! >= startOfDay &&
                    task.completedAt!! < endOfDay
                }
                
                val dayCreatedTasks = allTasks.filter { task ->
                    task.createdAt >= startOfDay && task.createdAt < endOfDay
                }
                
                val daySessions = sessions.filter { session ->
                    session.startTime >= startOfDay &&
                    session.startTime < endOfDay &&
                    session.completed
                }
                
                val focusMinutes = daySessions.sumOf { it.durationInMinutes.toInt() }
                val pointsEarned = (dayCompletedTasks.size * 10) + daySessions.sumOf { it.pointsEarned.toInt() }
                
                statsList.add(0, DailyStats(
                    date = dateStr,
                    tasksCompleted = dayCompletedTasks.size,
                    tasksCreated = dayCreatedTasks.size,
                    focusMinutes = focusMinutes,
                    pointsEarned = pointsEarned
                ))
                
                calendar.add(Calendar.DAY_OF_MONTH, -1)
            }
            
            statsList
        }
    }
    
    override suspend fun addPoints(userId: String, points: Int): Resource<Int> {
        return try {
            val userResult = authRepository.getUserProfile(userId)
            if (userResult !is Resource.Success) {
                return Resource.Error("Failed to load user")
            }
            
            val user = userResult.data
            val newTotalPoints = user.totalPoints + points
            val newLevel = calculateLevel(newTotalPoints)
            
            val updatedUser = user.copy(
                totalPoints = newTotalPoints,
                currentLevel = newLevel
            )
            
            val updateResult = authRepository.updateUserProfile(updatedUser)
            if (updateResult is Resource.Success) {
                Resource.Success(newTotalPoints)
            } else {
                Resource.Error("Failed to update points")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add points")
        }
    }
    
    override suspend fun calculateLevel(totalPoints: Int): Int {
        // Level formula: level = sqrt(points / 100) + 1
        return (kotlin.math.sqrt(totalPoints / 100.0) + 1).toInt()
    }
    
    override suspend fun updateUserLevel(userId: String): Resource<Unit> {
        return try {
            val userResult = authRepository.getUserProfile(userId)
            if (userResult !is Resource.Success) {
                return Resource.Error("Failed to load user")
            }
            
            val user = userResult.data
            val newLevel = calculateLevel(user.totalPoints)
            
            if (newLevel != user.currentLevel) {
                val updatedUser = user.copy(currentLevel = newLevel)
                authRepository.updateUserProfile(updatedUser)
            } else {
                Resource.Success(Unit)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update level")
        }
    }
    
    override suspend fun updateStreak(userId: String): Resource<Int> {
        return try {
            val userResult = authRepository.getUserProfile(userId)
            if (userResult !is Resource.Success) {
                return Resource.Error("Failed to load user")
            }
            
            val user = userResult.data
            val today = DateUtils.formatDate(Calendar.getInstance())
            val yesterday = DateUtils.formatDate(Calendar.getInstance().apply { 
                add(Calendar.DAY_OF_MONTH, -1) 
            })
            
            val todayStats = getDailyStats(userId, today)
            val yesterdayStats = getDailyStats(userId, yesterday)
            
            val completedToday = todayStats is Resource.Success && todayStats.data.tasksCompleted > 0
            val completedYesterday = yesterdayStats is Resource.Success && yesterdayStats.data.tasksCompleted > 0
            
            val newStreak = when {
                completedToday -> {
                    if (completedYesterday) {
                        user.currentStreak + 1
                    } else {
                        1
                    }
                }
                else -> 0
            }
            
            val updatedUser = user.copy(currentStreak = newStreak)
            authRepository.updateUserProfile(updatedUser)
            
            Resource.Success(newStreak)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update streak")
        }
    }
    
    override suspend fun getCurrentStreak(userId: String): Resource<Int> {
        return try {
            val userResult = authRepository.getUserProfile(userId)
            if (userResult is Resource.Success) {
                Resource.Success(userResult.data.currentStreak)
            } else {
                Resource.Error("Failed to load user")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get streak")
        }
    }
    
    // Helper extension to get first item from Flow
    private suspend fun <T> Flow<T>.first(): T {
        var result: T? = null
        collect { value ->
            result = value
            return@collect
        }
        return result ?: throw NoSuchElementException("Flow was empty")
    }
}

