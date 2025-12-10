package com.example.todolist2.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.todolist2.MainActivity
import java.util.concurrent.TimeUnit

object NotificationHelper {
    const val CHANNEL_ID = "taskmaster_notifications"
    const val CHANNEL_NAME = "TaskMaster Reminders"
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Nhắc nhở công việc"
                enableVibration(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun scheduleTaskReminder(
        context: Context,
        taskId: String,
        taskTitle: String,
        reminderTime: Long
    ) {
        val now = System.currentTimeMillis()
        val delay = reminderTime - now
        
        if (delay <= 0) {
            return // Reminder time has passed
        }
        
        val workRequest = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "taskId" to taskId,
                    "taskTitle" to taskTitle
                )
            )
            .addTag("task_reminder_$taskId")
            .build()
        
        WorkManager.getInstance(context).enqueue(workRequest)
    }
    
    fun cancelTaskReminder(context: Context, taskId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag("task_reminder_$taskId")
    }
    
    fun showNotification(
        context: Context,
        taskId: String,
        taskTitle: String,
        taskDescription: String = ""
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("taskId", taskId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⏰ Nhắc nhở: $taskTitle")
            .setContentText(taskDescription.ifEmpty { "Đã đến giờ làm việc này!" })
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(taskDescription.ifEmpty { "Đã đến giờ làm việc này!" }))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(taskId.hashCode(), notification)
    }
}

