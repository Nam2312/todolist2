package com.example.todolist2.domain.repository

import com.example.todolist2.domain.model.User
import com.example.todolist2.util.Resource
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

/**
 * Module 1: Authentication repository interface
 */
interface AuthRepository {
    fun getCurrentUser(): FirebaseUser?
    fun observeAuthState(): Flow<FirebaseUser?>
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Resource<User>
    suspend fun signInWithEmail(email: String, password: String): Resource<User>
    suspend fun signInWithGoogle(idToken: String): Resource<User>
    suspend fun signOut()
    suspend fun resetPassword(email: String): Resource<Unit>
    suspend fun getUserProfile(userId: String): Resource<User>
    fun observeUserProfile(userId: String): Flow<User>
    suspend fun updateUserProfile(user: User): Resource<Unit>
}





