package com.example.todolist2

import android.app.Application
import com.example.todolist2.util.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TodoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}









