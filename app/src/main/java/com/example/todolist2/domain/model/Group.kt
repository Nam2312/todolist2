package com.example.todolist2.domain.model

/**
 * Group/Team domain model
 * Cho phép nhiều người dùng làm việc cùng nhau
 */
data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val code: String = "", // Mã nhóm để tham gia (6-8 ký tự)
    val ownerId: String = "", // User ID của người tạo nhóm
    val color: String = "#6200EE", // Màu sắc nhóm
    val createdAt: Long = System.currentTimeMillis(),
    val memberCount: Int = 0,
    val taskCount: Int = 0,
    val isActive: Boolean = true
)

data class GroupMember(
    val id: String = "",
    val groupId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userAvatarUrl: String = "",
    val role: GroupRole = GroupRole.MEMBER, // OWNER, ADMIN, MEMBER
    val joinedAt: Long = System.currentTimeMillis(),
    val tasksCompleted: Int = 0, // Số tasks đã hoàn thành trong nhóm
    val isActive: Boolean = true
)

enum class GroupRole {
    OWNER,    // Người tạo nhóm - có quyền cao nhất
    ADMIN,    // Quản trị viên - có thể quản lý members
    MEMBER    // Thành viên thường
}

data class GroupTask(
    val id: String = "",
    val groupId: String = "",
    val taskId: String = "", // Reference đến Task
    val assignedToUserId: String? = null, // Task được assign cho ai
    val createdByUserId: String = "", // Ai tạo task này
    val sharedAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val completedByUserId: String? = null
)




