package com.example.todolist2.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class TaskReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val taskId = inputData.getString("taskId") ?: return Result.failure()
        val taskTitle = inputData.getString("taskTitle") ?: return Result.failure()
        val taskDescription = inputData.getString("taskDescription") ?: ""
        
        NotificationHelper.showNotification(
            applicationContext,
            taskId,
            taskTitle,
            taskDescription
        )
        
        return Result.success()
    }
}









