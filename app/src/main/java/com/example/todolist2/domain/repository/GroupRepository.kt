package com.example.todolist2.domain.repository

import com.example.todolist2.domain.model.Group
import com.example.todolist2.domain.model.GroupMember
import com.example.todolist2.domain.model.GroupTask
import com.example.todolist2.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface cho Group management
 */
interface GroupRepository {
    // Groups
    fun observeUserGroups(userId: String): Flow<List<Group>>
    fun observeGroup(groupId: String): Flow<Group?>
    suspend fun getGroupByCode(code: String): Group?
    suspend fun createGroup(userId: String, group: Group): Resource<Group>
    suspend fun updateGroup(userId: String, group: Group): Resource<Unit>
    suspend fun deleteGroup(userId: String, groupId: String): Resource<Unit>
    
    // Group Members
    fun observeGroupMembers(groupId: String): Flow<List<GroupMember>>
    suspend fun getGroupMember(groupId: String, userId: String): GroupMember?
    suspend fun joinGroup(userId: String, groupCode: String, userName: String, userEmail: String, userAvatarUrl: String): Resource<GroupMember>
    suspend fun leaveGroup(userId: String, groupId: String): Resource<Unit>
    suspend fun removeMember(ownerId: String, groupId: String, memberId: String): Resource<Unit>
    suspend fun updateMemberRole(ownerId: String, groupId: String, memberId: String, role: com.example.todolist2.domain.model.GroupRole): Resource<Unit>
    
    // Group Tasks
    fun observeGroupTasks(groupId: String): Flow<List<GroupTask>>
    suspend fun shareTaskToGroup(groupId: String, taskId: String, userId: String): Resource<GroupTask>
    suspend fun assignTaskToMember(groupId: String, taskId: String, memberId: String): Resource<Unit>
    suspend fun unshareTaskFromGroup(groupId: String, taskId: String): Resource<Unit>
    
    // Utilities
    suspend fun generateGroupCode(): String // Tạo mã nhóm unique (6-8 ký tự)
    suspend fun validateGroupCode(code: String): Boolean // Kiểm tra mã nhóm có tồn tại không
}

