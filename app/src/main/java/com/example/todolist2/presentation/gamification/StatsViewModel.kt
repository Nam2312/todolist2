package com.example.todolist2.presentation.gamification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist2.domain.model.Badge
import com.example.todolist2.domain.model.DailyStats
import com.example.todolist2.domain.repository.AuthRepository
import com.example.todolist2.domain.repository.GamificationRepository
import com.example.todolist2.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

data class StatsState(
    val totalPoints: Int = 0,
    val currentLevel: Int = 1,
    val currentStreak: Int = 0,
    val tasksCompleted: Int = 0,
    val focusSessionsCompleted: Int = 0,
    val totalFocusMinutes: Int = 0,
    val badges: List<Badge> = emptyList(),
    val dailyStats: List<DailyStats> = emptyList(),
    val weeklyStats: List<com.example.todolist2.domain.model.WeeklyStats> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTab: StatsTab = StatsTab.OVERVIEW
)

enum class StatsTab {
    OVERVIEW, BADGES, CHARTS
}

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val gamificationRepository: GamificationRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(StatsState())
    val state = _state.asStateFlow()
    
    private var isInitialized = false
    private var statsObserverJob: Job? = null
    private var badgesObserverJob: Job? = null
    
    fun loadStats() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _state.update { it.copy(error = "Chưa đăng nhập") }
            // Load default badges even if not logged in
            val defaultBadges = com.example.todolist2.domain.model.BadgeType.entries.map { badgeType ->
                com.example.todolist2.domain.model.Badge(
                    id = badgeType.id,
                    name = badgeType.displayName,
                    description = badgeType.description,
                    iconName = badgeType.id,
                    requirement = badgeType.requirement,
                    isUnlocked = false
                )
            }
            _state.update { it.copy(badges = defaultBadges) }
            return
        }
        
        if (isInitialized) return
        isInitialized = true
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                // Load user stats on IO dispatcher
                val statsResult = withContext(Dispatchers.IO) {
                    gamificationRepository.getUserStats(currentUser.uid)
                }
                if (statsResult is Resource.Success) {
                    val stats = statsResult.data
                    _state.update { 
                        it.copy(
                            totalPoints = stats.totalPoints,
                            currentLevel = stats.currentLevel,
                            currentStreak = stats.currentStreak,
                            tasksCompleted = stats.tasksCompleted,
                            focusSessionsCompleted = stats.focusSessionsCompleted,
                            totalFocusMinutes = stats.totalFocusMinutes
                        )
                    }
                }
                
                // Load badges on IO dispatcher - always load, even if user profile fails
                val badgesResult = withContext(Dispatchers.IO) {
                    try {
                        gamificationRepository.getUserBadges(currentUser.uid)
                    } catch (e: Exception) {
                        Resource.Error(e.message ?: "Failed to load badges")
                    }
                }
                when (badgesResult) {
                    is Resource.Success -> {
                        _state.update { it.copy(badges = badgesResult.data) }
                    }
                    is Resource.Error -> {
                        // If badges fail to load, create empty badges list from BadgeType
                        val defaultBadges = com.example.todolist2.domain.model.BadgeType.entries.map { badgeType ->
                            com.example.todolist2.domain.model.Badge(
                                id = badgeType.id,
                                name = badgeType.displayName,
                                description = badgeType.description,
                                iconName = badgeType.id,
                                requirement = badgeType.requirement,
                                isUnlocked = false
                            )
                        }
                        _state.update { it.copy(badges = defaultBadges) }
                    }
                    else -> {
                        // Create default badges list
                        val defaultBadges = com.example.todolist2.domain.model.BadgeType.entries.map { badgeType ->
                            com.example.todolist2.domain.model.Badge(
                                id = badgeType.id,
                                name = badgeType.displayName,
                                description = badgeType.description,
                                iconName = badgeType.id,
                                requirement = badgeType.requirement,
                                isUnlocked = false
                            )
                        }
                        _state.update { it.copy(badges = defaultBadges) }
                    }
                }
                
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
        
        // Observe user stats for live updates
        statsObserverJob?.cancel()
        statsObserverJob = viewModelScope.launch {
            gamificationRepository.observeUserStats(currentUser.uid)
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    _state.update { it.copy(error = e.message) }
                }
                .collect { stats ->
                    _state.update {
                        it.copy(
                            totalPoints = stats.totalPoints,
                            currentLevel = stats.currentLevel,
                            currentStreak = stats.currentStreak,
                            tasksCompleted = stats.tasksCompleted,
                            focusSessionsCompleted = stats.focusSessionsCompleted,
                            totalFocusMinutes = stats.totalFocusMinutes
                        )
                    }
                }
        }
        
        // Observe badges for live updates
        badgesObserverJob?.cancel()
        badgesObserverJob = viewModelScope.launch {
            gamificationRepository.observeUserBadges(currentUser.uid)
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    _state.update { it.copy(error = e.message) }
                }
                .collect { badges ->
                    _state.update { it.copy(badges = badges) }
                }
        }
        
        // Observe daily stats separately to avoid blocking
        viewModelScope.launch {
            gamificationRepository.observeDailyStats(currentUser.uid, 7)
                .flowOn(Dispatchers.IO)
                .catch { e ->
                    _state.update { it.copy(error = e.message) }
                }
                .collect { stats ->
                    _state.update { it.copy(dailyStats = stats) }
                    refreshWeeklyStats()
                }
        }
        
        // Load weekly stats (4 weeks)
        viewModelScope.launch {
            try {
                val weeklyStatsList = mutableListOf<com.example.todolist2.domain.model.WeeklyStats>()
                var calendar = com.example.todolist2.util.DateUtils.getStartOfWeek()
                
                for (i in 0 until 4) {
                    val weekStart = com.example.todolist2.util.DateUtils.formatDate(calendar)
                    val weekResult = withContext(Dispatchers.IO) {
                        gamificationRepository.getWeeklyStats(currentUser.uid, weekStart)
                    }
                    if (weekResult is Resource.Success) {
                        weeklyStatsList.add(0, weekResult.data) // Add to beginning
                    }
                    calendar.add(Calendar.WEEK_OF_YEAR, -1)
                }
                
                _state.update { it.copy(weeklyStats = weeklyStatsList) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun onTabSelected(tab: StatsTab) {
        _state.update { it.copy(selectedTab = tab) }
        
        // Force load badges if badges tab is selected and badges list is empty
        if (tab == StatsTab.BADGES && _state.value.badges.isEmpty()) {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                viewModelScope.launch {
                    val badgesResult = withContext(Dispatchers.IO) {
                        try {
                            gamificationRepository.getUserBadges(currentUser.uid)
                        } catch (e: Exception) {
                            Resource.Error(e.message ?: "Failed to load badges")
                        }
                    }
                    when (badgesResult) {
                        is Resource.Success -> {
                            _state.update { it.copy(badges = badgesResult.data) }
                        }
                        else -> {
                            // Create default badges list
                            val defaultBadges = com.example.todolist2.domain.model.BadgeType.entries.map { badgeType ->
                                com.example.todolist2.domain.model.Badge(
                                    id = badgeType.id,
                                    name = badgeType.displayName,
                                    description = badgeType.description,
                                    iconName = badgeType.id,
                                    requirement = badgeType.requirement,
                                    isUnlocked = false
                                )
                            }
                            _state.update { it.copy(badges = defaultBadges) }
                        }
                    }
                }
            } else {
                // Create default badges list if not logged in
                val defaultBadges = com.example.todolist2.domain.model.BadgeType.entries.map { badgeType ->
                    com.example.todolist2.domain.model.Badge(
                        id = badgeType.id,
                        name = badgeType.displayName,
                        description = badgeType.description,
                        iconName = badgeType.id,
                        requirement = badgeType.requirement,
                        isUnlocked = false
                    )
                }
                _state.update { it.copy(badges = defaultBadges) }
            }
        }
    }
    
    fun refreshStats() {
        isInitialized = false
        _state.update { it.copy(weeklyStats = emptyList()) } // Clear weekly stats to force reload
        statsObserverJob?.cancel()
        badgesObserverJob?.cancel()
        loadStats()
    }
    
    fun loadBadgesIfNeeded() {
        if (_state.value.badges.isNotEmpty()) return
        
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            viewModelScope.launch {
                val badgesResult = withContext(Dispatchers.IO) {
                    try {
                        gamificationRepository.getUserBadges(currentUser.uid)
                    } catch (e: Exception) {
                        Resource.Error(e.message ?: "Failed to load badges")
                    }
                }
                when (badgesResult) {
                    is Resource.Success -> {
                        _state.update { it.copy(badges = badgesResult.data) }
                    }
                    else -> {
                        // Create default badges list
                        val defaultBadges = com.example.todolist2.domain.model.BadgeType.entries.map { badgeType ->
                            com.example.todolist2.domain.model.Badge(
                                id = badgeType.id,
                                name = badgeType.displayName,
                                description = badgeType.description,
                                iconName = badgeType.id,
                                requirement = badgeType.requirement,
                                isUnlocked = false
                            )
                        }
                        _state.update { it.copy(badges = defaultBadges) }
                    }
                }
            }
        } else {
            // Create default badges list if not logged in
            val defaultBadges = com.example.todolist2.domain.model.BadgeType.entries.map { badgeType ->
                com.example.todolist2.domain.model.Badge(
                    id = badgeType.id,
                    name = badgeType.displayName,
                    description = badgeType.description,
                    iconName = badgeType.id,
                    requirement = badgeType.requirement,
                    isUnlocked = false
                )
            }
            _state.update { it.copy(badges = defaultBadges) }
        }
    }
    
    fun refreshWeeklyStats() {
        val currentUser = authRepository.getCurrentUser() ?: return
        
        viewModelScope.launch {
            try {
                val weeklyStatsList = mutableListOf<com.example.todolist2.domain.model.WeeklyStats>()
                var calendar = com.example.todolist2.util.DateUtils.getStartOfWeek()
                
                for (i in 0 until 4) {
                    val weekStart = com.example.todolist2.util.DateUtils.formatDate(calendar)
                    val weekResult = withContext(Dispatchers.IO) {
                        gamificationRepository.getWeeklyStats(currentUser.uid, weekStart)
                    }
                    if (weekResult is Resource.Success) {
                        weeklyStatsList.add(0, weekResult.data) // Add to beginning
                    }
                    calendar.add(Calendar.WEEK_OF_YEAR, -1)
                }
                
                _state.update { it.copy(weeklyStats = weeklyStatsList) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

