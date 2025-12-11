package com.example.todolist2.presentation.theme

import androidx.lifecycle.ViewModel
import com.example.todolist2.data.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    val userPreferences: UserPreferences
) : ViewModel() {
    val themePreference = userPreferences.themePreference
}




