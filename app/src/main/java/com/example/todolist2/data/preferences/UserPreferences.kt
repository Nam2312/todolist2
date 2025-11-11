package com.example.todolist2.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.todolist2.domain.model.ThemePreference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Module 1: DataStore for user preferences (theme, settings)
 */
@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore
    
    companion object {
        val THEME_KEY = stringPreferencesKey("theme_preference")
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val FOCUS_DEFAULT_DURATION = intPreferencesKey("focus_default_duration")
    }
    
    val themePreference: Flow<ThemePreference> = dataStore.data.map { preferences ->
        val themeName = preferences[THEME_KEY] ?: ThemePreference.SYSTEM.name
        try {
            ThemePreference.valueOf(themeName)
        } catch (e: IllegalArgumentException) {
            ThemePreference.SYSTEM
        }
    }
    
    suspend fun setThemePreference(theme: ThemePreference) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }
    
    val userId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }
    
    suspend fun setUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = userId
        }
    }
    
    suspend fun clearUserId() {
        dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
    }
    
    val isFirstLaunch: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_FIRST_LAUNCH] ?: true
    }
    
    suspend fun setFirstLaunchCompleted() {
        dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH] = false
        }
    }
    
    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED] ?: true
    }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }
    
    val focusDefaultDuration: Flow<Int> = dataStore.data.map { preferences ->
        preferences[FOCUS_DEFAULT_DURATION] ?: 25 // Default Pomodoro
    }
    
    suspend fun setFocusDefaultDuration(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[FOCUS_DEFAULT_DURATION] = minutes
        }
    }
}


