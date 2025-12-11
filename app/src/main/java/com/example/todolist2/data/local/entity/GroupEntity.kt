package com.example.todolist2.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.todolist2.domain.model.Group
import com.example.todolist2.domain.model.GroupMember
import com.example.todolist2.domain.model.GroupRole

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val code: String,
    val ownerId: String,
    val color: String,
    val createdAt: Long,
    val memberCount: Int,
    val taskCount: Int,
    val isActive: Boolean,
    val isSynced: Boolean = false
)

@Entity(tableName = "group_members")
data class GroupMemberEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val userAvatarUrl: String,
    val role: String, // "OWNER", "ADMIN", "MEMBER"
    val joinedAt: Long,
    val tasksCompleted: Int,
    val isActive: Boolean,
    val isSynced: Boolean = false
)

// Conversion functions
fun GroupEntity.toDomain(): Group {
    return Group(
        id = id,
        name = name,
        description = description,
        code = code,
        ownerId = ownerId,
        color = color,
        createdAt = createdAt,
        memberCount = memberCount,
        taskCount = taskCount,
        isActive = isActive
    )
}

fun Group.toEntity(): GroupEntity {
    return GroupEntity(
        id = id,
        name = name,
        description = description,
        code = code,
        ownerId = ownerId,
        color = color,
        createdAt = createdAt,
        memberCount = memberCount,
        taskCount = taskCount,
        isActive = isActive,
        isSynced = false
    )
}

fun GroupMemberEntity.toDomain(): GroupMember {
    return GroupMember(
        id = id,
        groupId = groupId,
        userId = userId,
        userName = userName,
        userEmail = userEmail,
        userAvatarUrl = userAvatarUrl,
        role = try {
            GroupRole.valueOf(role)
        } catch (e: Exception) {
            GroupRole.MEMBER
        },
        joinedAt = joinedAt,
        tasksCompleted = tasksCompleted,
        isActive = isActive
    )
}

fun GroupMember.toEntity(): GroupMemberEntity {
    return GroupMemberEntity(
        id = id,
        groupId = groupId,
        userId = userId,
        userName = userName,
        userEmail = userEmail,
        userAvatarUrl = userAvatarUrl,
        role = role.name,
        joinedAt = joinedAt,
        tasksCompleted = tasksCompleted,
        isActive = isActive,
        isSynced = false
    )
}




