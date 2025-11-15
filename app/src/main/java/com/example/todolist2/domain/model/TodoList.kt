package com.example.todolist2.domain.model

/**
 * Module 2: Task List (Project) domain model
 */
data class TodoList(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val color: String = "#6200EE", // Material Purple
    val createdAt: Long = System.currentTimeMillis(),
    val taskCount: Int = 0,
    val completedTaskCount: Int = 0
)







