package com.example.todolist2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.todolist2.domain.model.Priority
import com.example.todolist2.domain.model.SubTask
import com.example.todolist2.domain.model.Task

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val listId: String,
    val userId: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean,
    val priority: Int,
    val dueDate: Long?,
    val reminderTime: Long?,
    val tags: String, // JSON string
    val createdAt: Long,
    val completedAt: Long?,
    val subTasks: String, // JSON string
    val isSynced: Boolean = false // For offline sync tracking
)

fun TaskEntity.toDomain(): Task {
    return Task(
        id = id,
        listId = listId,
        userId = userId,
        title = title,
        description = description,
        isCompleted = isCompleted,
        priority = Priority.entries.find { it.value == priority } ?: Priority.MEDIUM,
        dueDate = dueDate,
        reminderTime = reminderTime,
        tags = if (tags.isNotEmpty()) tags.split(",") else emptyList(),
        createdAt = createdAt,
        completedAt = completedAt,
        subTasks = emptyList() // Will parse from JSON if needed
    )
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        listId = listId,
        userId = userId,
        title = title,
        description = description,
        isCompleted = isCompleted,
        priority = priority.value,
        dueDate = dueDate,
        reminderTime = reminderTime,
        tags = tags.joinToString(","),
        createdAt = createdAt,
        completedAt = completedAt,
        subTasks = "", // Will serialize to JSON if needed
        isSynced = false
    )
}


