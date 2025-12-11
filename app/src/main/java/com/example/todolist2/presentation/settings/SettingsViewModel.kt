package com.example.todolist2.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist2.data.preferences.UserPreferences
import com.example.todolist2.domain.model.ThemePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val selectedTheme: ThemePreference = ThemePreference.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val focusDefaultDuration: Int = 25
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {
    
    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()
    
    init {
        viewModelScope.launch {
            userPreferences.themePreference.collect { theme ->
                _state.update { it.copy(selectedTheme = theme) }
            }
        }
        
        viewModelScope.launch {
            userPreferences.notificationsEnabled.collect { enabled ->
                _state.update { it.copy(notificationsEnabled = enabled) }
            }
        }
        
        viewModelScope.launch {
            userPreferences.focusDefaultDuration.collect { duration ->
                _state.update { it.copy(focusDefaultDuration = duration) }
            }
        }
    }
    
    fun setTheme(theme: ThemePreference) {
        viewModelScope.launch {
            userPreferences.setThemePreference(theme)
        }
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setNotificationsEnabled(enabled)
        }
    }
}




