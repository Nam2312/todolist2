package com.example.todolist2.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist2.domain.model.ThemePreference
import com.example.todolist2.domain.model.User
import com.example.todolist2.domain.repository.AuthRepository
import com.example.todolist2.domain.repository.GamificationRepository
import com.example.todolist2.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val editedUsername: String = "",
    val editedAvatarUrl: String = "",
    val selectedTheme: ThemePreference = ThemePreference.SYSTEM,
    val totalPoints: Int = 0,
    val currentLevel: Int = 1,
    val currentStreak: Int = 0,
    val tasksCompleted: Int = 0,
    val error: String? = null,
    val showLogoutDialog: Boolean = false,
    val isLoggedOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val gamificationRepository: GamificationRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(ProfileState())
    val state = _state.asStateFlow()
    
    private var profileJob: Job? = null
    private var statsJob: Job? = null
    
    init {
        observeUserProfile()
        observeUserStats()
    }
    
    private fun observeUserProfile() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            _state.update { it.copy(error = "Chưa đăng nhập") }
            return
        }
        
        profileJob?.cancel()
        profileJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            authRepository.observeUserProfile(currentUser.uid)
                .collect { user ->
                    _state.update { state ->
                        state.copy(
                            user = user,
                            editedUsername = if (state.isEditing) state.editedUsername else user.username,
                            editedAvatarUrl = if (state.isEditing) state.editedAvatarUrl else user.avatarUrl,
                            selectedTheme = if (state.isEditing) state.selectedTheme else user.preferredTheme,
                            totalPoints = if (state.totalPoints == 0) user.totalPoints else state.totalPoints,
                            currentLevel = if (state.currentLevel <= 1) user.currentLevel else state.currentLevel,
                            currentStreak = user.currentStreak,
                            tasksCompleted = if (state.tasksCompleted == 0) user.tasksCompleted else state.tasksCompleted,
                            isLoading = false
                        )
                    }
                }
        }
    }
    
    private fun observeUserStats() {
        val currentUser = authRepository.getCurrentUser() ?: return
        
        statsJob?.cancel()
        statsJob = viewModelScope.launch {
            gamificationRepository.observeUserStats(currentUser.uid)
                .collect { stats ->
                    _state.update {
                        it.copy(
                            totalPoints = stats.totalPoints,
                            currentLevel = stats.currentLevel,
                            currentStreak = stats.currentStreak,
                            tasksCompleted = stats.tasksCompleted
                        )
                    }
                }
        }
    }
    
    fun onEditClick() {
        _state.update { it.copy(isEditing = true) }
    }
    
    fun onCancelEdit() {
        val user = _state.value.user
        _state.update { 
            it.copy(
                isEditing = false,
                editedUsername = user?.username ?: "",
                editedAvatarUrl = user?.avatarUrl ?: "",
                selectedTheme = user?.preferredTheme ?: ThemePreference.SYSTEM,
                error = null
            )
        }
    }
    
    fun onUsernameChange(username: String) {
        _state.update { it.copy(editedUsername = username) }
    }
    
    fun onAvatarUrlChange(avatarUrl: String) {
        _state.update { it.copy(editedAvatarUrl = avatarUrl) }
    }
    
    fun onThemeChange(theme: ThemePreference) {
        _state.update { it.copy(selectedTheme = theme) }
    }
    
    fun saveProfile() {
        val currentUser = authRepository.getCurrentUser()
        val currentState = _state.value
        
        if (currentUser == null || currentState.user == null) {
            _state.update { it.copy(error = "Chưa đăng nhập") }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val updatedUser = currentState.user.copy(
                username = currentState.editedUsername.trim(),
                avatarUrl = currentState.editedAvatarUrl.trim(),
                preferredTheme = currentState.selectedTheme
            )
            
            val result = authRepository.updateUserProfile(updatedUser)
            if (result is Resource.Error) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false, isEditing = false) }
            }
        }
    }
    
    fun onLogoutClick() {
        _state.update { it.copy(showLogoutDialog = true) }
    }
    
    fun onLogoutConfirm() {
        _state.update { it.copy(showLogoutDialog = false) }
        viewModelScope.launch {
            try {
                authRepository.signOut()
                profileJob?.cancel()
                statsJob?.cancel()
                _state.update { it.copy(isLoggedOut = true) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Lỗi đăng xuất: ${e.message}") }
            }
        }
    }
    
    fun isLoggedOut(): Boolean {
        return _state.value.isLoggedOut || authRepository.getCurrentUser() == null
    }
    
    fun onLogoutCancel() {
        _state.update { it.copy(showLogoutDialog = false) }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

