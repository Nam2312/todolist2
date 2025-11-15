package com.example.todolist2.data.repository

import com.example.todolist2.data.preferences.UserPreferences
import com.example.todolist2.data.remote.firebase.FirebaseAuthDataSource
import com.example.todolist2.domain.model.User
import com.example.todolist2.domain.repository.AuthRepository
import com.example.todolist2.util.Resource
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Module 1: Authentication repository implementation
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource,
    private val userPreferences: UserPreferences
) : AuthRepository {
    
    override fun getCurrentUser(): FirebaseUser? {
        return authDataSource.getCurrentUser()
    }
    
    override fun observeAuthState(): Flow<FirebaseUser?> {
        return authDataSource.observeAuthState()
    }
    
    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Resource<User> {
        val result = authDataSource.signUpWithEmail(email, password, displayName)
        if (result is Resource.Success) {
            userPreferences.setUserId(result.data.id)
        }
        return result
    }
    
    override suspend fun signInWithEmail(email: String, password: String): Resource<User> {
        val result = authDataSource.signInWithEmail(email, password)
        if (result is Resource.Success) {
            userPreferences.setUserId(result.data.id)
        }
        return result
    }
    
    override suspend fun signInWithGoogle(idToken: String): Resource<User> {
        val result = authDataSource.signInWithGoogle(idToken)
        if (result is Resource.Success) {
            userPreferences.setUserId(result.data.id)
        }
        return result
    }
    
    override suspend fun signOut() {
        authDataSource.signOut()
        userPreferences.clearUserId()
    }
    
    override suspend fun resetPassword(email: String): Resource<Unit> {
        return authDataSource.resetPassword(email)
    }
    
    override suspend fun getUserProfile(userId: String): Resource<User> {
        return authDataSource.getUserProfile(userId)
    }
    
    override fun observeUserProfile(userId: String): Flow<User> {
        return authDataSource.observeUserProfile(userId)
    }
    
    override suspend fun updateUserProfile(user: User): Resource<Unit> {
        return authDataSource.updateUserProfile(user)
    }
}





