package com.example.todolist2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.todolist2.domain.model.TodoList

@Entity(tableName = "todo_lists")
data class TodoListEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val color: String,
    val createdAt: Long,
    val taskCount: Int = 0,
    val completedTaskCount: Int = 0,
    val isSynced: Boolean = false
)

fun TodoListEntity.toDomain(): TodoList {
    return TodoList(
        id = id,
        userId = userId,
        name = name,
        color = color,
        createdAt = createdAt,
        taskCount = taskCount,
        completedTaskCount = completedTaskCount
    )
}

fun TodoList.toEntity(): TodoListEntity {
    return TodoListEntity(
        id = id,
        userId = userId,
        name = name,
        color = color,
        createdAt = createdAt,
        taskCount = taskCount,
        completedTaskCount = completedTaskCount,
        isSynced = false
    )
}


















