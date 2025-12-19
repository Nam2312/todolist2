package com.example.todolist2.presentation.auth.splash

import androidx.lifecycle.ViewModel
import com.example.todolist2.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    fun isUserLoggedIn(): Boolean {
        return authRepository.getCurrentUser() != null
    }
}


















