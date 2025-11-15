package com.example.todolist2.di

import com.example.todolist2.data.repository.AuthRepositoryImpl
import com.example.todolist2.data.repository.GamificationRepositoryImpl
import com.example.todolist2.data.repository.TaskRepositoryImpl
import com.example.todolist2.domain.repository.AuthRepository
import com.example.todolist2.domain.repository.GamificationRepository
import com.example.todolist2.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
    
    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        taskRepositoryImpl: TaskRepositoryImpl
    ): TaskRepository
    
    @Binds
    @Singleton
    abstract fun bindGamificationRepository(
        gamificationRepositoryImpl: GamificationRepositoryImpl
    ): GamificationRepository
}





