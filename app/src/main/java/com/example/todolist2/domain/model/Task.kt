package com.example.todolist2.domain.model

/**
 * Module 2: Task domain model
 */
data class Task(
    val id: String = "",
    val listId: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val dueDate: Long? = null,
    val reminderTime: Long? = null,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    
    // Sub-tasks
    val subTasks: List<SubTask> = emptyList()
)

data class SubTask(
    val id: String = "",
    val title: String = "",
    val isCompleted: Boolean = false
)

enum class Priority(val value: Int) {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    URGENT(4)
}


