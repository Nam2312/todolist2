package com.example.todolist2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.todolist2.domain.model.FocusSession

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val taskId: String?,
    val taskTitle: String,
    val durationInMinutes: Int,
    val startTime: Long,
    val endTime: Long?,
    val completed: Boolean,
    val pointsEarned: Int,
    val isSynced: Boolean = false
)

fun FocusSessionEntity.toDomain(): FocusSession {
    return FocusSession(
        id = id,
        userId = userId,
        taskId = taskId,
        taskTitle = taskTitle,
        durationInMinutes = durationInMinutes,
        startTime = startTime,
        endTime = endTime,
        completed = completed,
        pointsEarned = pointsEarned
    )
}

fun FocusSession.toEntity(): FocusSessionEntity {
    return FocusSessionEntity(
        id = id,
        userId = userId,
        taskId = taskId,
        taskTitle = taskTitle,
        durationInMinutes = durationInMinutes,
        startTime = startTime,
        endTime = endTime,
        completed = completed,
        pointsEarned = pointsEarned,
        isSynced = false
    )
}













