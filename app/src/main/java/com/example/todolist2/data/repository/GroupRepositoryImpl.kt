package com.example.todolist2.data.repository

import com.example.todolist2.data.local.dao.GroupDao
import com.example.todolist2.data.local.entity.toDomain
import com.example.todolist2.data.local.entity.toEntity
import com.example.todolist2.data.remote.firebase.FirestoreGroupDataSource
import com.example.todolist2.domain.model.Group
import com.example.todolist2.domain.model.GroupMember
import com.example.todolist2.domain.model.GroupRole
import com.example.todolist2.domain.repository.GroupRepository
import com.example.todolist2.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val groupDao: GroupDao,
    private val firestoreDataSource: FirestoreGroupDataSource
) : GroupRepository {
    
    // Groups
    override fun observeUserGroups(userId: String): Flow<List<Group>> {
        // Use local database for immediate updates
        return groupDao.getUserGroups(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun observeGroup(groupId: String): Flow<Group?> {
        // First check local, then observe from Firestore
        return flow {
            // Emit local first if exists
            val localGroup = groupDao.getGroupById(groupId)?.toDomain()
            if (localGroup != null) {
                emit(localGroup)
            }
            
            // Then observe from Firestore and update local
            firestoreDataSource.observeGroup(groupId)
                .collect { firestoreGroup ->
                    if (firestoreGroup != null) {
                        // Update local with Firestore data
                        groupDao.insertGroup(firestoreGroup.toEntity().copy(isSynced = true))
                        emit(firestoreGroup)
                    } else {
                        // If Firestore returns null, check if we have local data
                        // If we have local data, keep emitting it (might be offline)
                        // If no local data, emit null
                        if (localGroup == null) {
                            emit(null)
                        }
                        // If we have local data, don't emit null - keep the local data
                    }
                }
        }
    }
    
    override suspend fun getGroupByCode(code: String): Group? {
        // Normalize code to uppercase
        val normalizedCode = code.uppercase().trim()
        
        // Try local first (case-insensitive)
        val localGroups = groupDao.getAllGroups().firstOrNull() ?: emptyList()
        val localGroup = localGroups
            .map { it.toDomain() }
            .firstOrNull { it.code.uppercase() == normalizedCode && it.isActive }
        
        if (localGroup != null) {
            return localGroup
        }
        
        // Then try Firestore
        val firestoreGroup = firestoreDataSource.getGroupByCode(normalizedCode)
        if (firestoreGroup != null) {
            // Save to local for future use
            groupDao.insertGroup(firestoreGroup.toEntity().copy(isSynced = true))
            return firestoreGroup
        }
        
        return null
    }
    
    // Helper extension to get first item from Flow safely
    private suspend fun <T> Flow<T>.firstOrNull(): T? {
        var result: T? = null
        collect { value ->
            result = value
            return@collect
        }
        return result
    }
    
    override suspend fun createGroup(userId: String, group: Group): Resource<Group> {
        // Check if group with same code already exists
        val existingGroup = groupDao.getGroupByCode(group.code.uppercase())
        if (existingGroup != null && existingGroup.isSynced) {
            return Resource.Error("Mã nhóm đã tồn tại")
        }
        
        // Check in Firestore too
        val firestoreGroup = firestoreDataSource.getGroupByCode(group.code.uppercase())
        if (firestoreGroup != null) {
            return Resource.Error("Mã nhóm đã tồn tại")
        }
        
        // Save to local first (with unique ID)
        val localEntity = group.toEntity().copy(isSynced = false)
        groupDao.insertGroup(localEntity)
        
        // Sync to Firestore (this will generate a new ID from Firestore)
        val result = firestoreDataSource.createGroup(userId, group)
        
        if (result is Resource.Success) {
            // Delete old local entry if ID changed
            if (result.data.id != group.id) {
                groupDao.getGroupById(group.id)?.let {
                    groupDao.deleteGroup(it)
                }
            }
            
            // Update local with server data (with Firestore ID)
            val updatedGroup = result.data.toEntity().copy(isSynced = true)
            groupDao.insertGroup(updatedGroup)
            
            // Create owner member locally (it was created in Firestore by createGroup)
            val ownerMember = GroupMember(
                id = "${result.data.id}_$userId",
                groupId = result.data.id,
                userId = userId,
                userName = "",
                userEmail = "",
                userAvatarUrl = "",
                role = GroupRole.OWNER,
                joinedAt = System.currentTimeMillis()
            )
            groupDao.insertMember(ownerMember.toEntity().copy(isSynced = true))
        } else {
            // If Firestore fails, keep local but mark as unsynced
            // Don't delete local data - it will be retried later
        }
        
        return result
    }
    
    override suspend fun updateGroup(userId: String, group: Group): Resource<Unit> {
        // Update local first
        groupDao.updateGroup(group.toEntity())
        
        // Sync to Firestore
        return firestoreDataSource.updateGroup(userId, group)
    }
    
    override suspend fun deleteGroup(userId: String, groupId: String): Resource<Unit> {
        // Delete from local
        groupDao.getGroupById(groupId)?.let {
            groupDao.deleteGroup(it)
            groupDao.deleteMembersByGroup(groupId)
        }
        
        // Sync to Firestore
        return firestoreDataSource.deleteGroup(userId, groupId)
    }
    
    // Group Members
    override fun observeGroupMembers(groupId: String): Flow<List<GroupMember>> {
        return firestoreDataSource.observeGroupMembers(groupId)
    }
    
    override suspend fun getGroupMember(groupId: String, userId: String): GroupMember? {
        // Try local first
        val localMember = groupDao.getGroupMember(groupId, userId)?.toDomain()
        if (localMember != null) return localMember
        
        // Then try Firestore
        return firestoreDataSource.getGroupMember(groupId, userId)
    }
    
    override suspend fun joinGroup(userId: String, groupCode: String, userName: String, userEmail: String, userAvatarUrl: String): Resource<GroupMember> {
        // Join via Firestore
        val result = firestoreDataSource.joinGroup(userId, groupCode, userName, userEmail, userAvatarUrl)
        
        if (result is Resource.Success) {
            // Save member to local
            groupDao.insertMember(result.data.toEntity())
            
            // Also fetch and save the group to local
            val group = firestoreDataSource.getGroupByCode(groupCode)
            if (group != null) {
                groupDao.insertGroup(group.toEntity().copy(isSynced = true))
            }
        }
        
        return result
    }
    
    override suspend fun leaveGroup(userId: String, groupId: String): Resource<Unit> {
        // Update local
        groupDao.getGroupMember(groupId, userId)?.let {
            groupDao.deleteMember(it)
        }
        
        // Sync to Firestore
        return firestoreDataSource.leaveGroup(userId, groupId)
    }
    
    override suspend fun removeMember(ownerId: String, groupId: String, memberId: String): Resource<Unit> {
        // Update local
        groupDao.getGroupMember(groupId, memberId)?.let {
            groupDao.deleteMember(it)
        }
        
        // Sync to Firestore
        return firestoreDataSource.removeMember(ownerId, groupId, memberId)
    }
    
    override suspend fun updateMemberRole(ownerId: String, groupId: String, memberId: String, role: GroupRole): Resource<Unit> {
        // Update local
        groupDao.getGroupMember(groupId, memberId)?.let { member ->
            groupDao.updateMember(member.copy(role = role.name))
        }
        
        // Sync to Firestore
        return firestoreDataSource.updateMemberRole(ownerId, groupId, memberId, role)
    }
    
    // Group Tasks (simplified - can be expanded later)
    override fun observeGroupTasks(groupId: String): Flow<List<com.example.todolist2.domain.model.GroupTask>> {
        // TODO: Implement group tasks observation
        return kotlinx.coroutines.flow.flowOf(emptyList())
    }
    
    override suspend fun shareTaskToGroup(groupId: String, taskId: String, userId: String): Resource<com.example.todolist2.domain.model.GroupTask> {
        // TODO: Implement share task to group
        return Resource.Error("Not implemented yet")
    }
    
    override suspend fun assignTaskToMember(groupId: String, taskId: String, memberId: String): Resource<Unit> {
        // TODO: Implement assign task to member
        return Resource.Error("Not implemented yet")
    }
    
    override suspend fun unshareTaskFromGroup(groupId: String, taskId: String): Resource<Unit> {
        // TODO: Implement unshare task
        return Resource.Error("Not implemented yet")
    }
    
    // Utilities
    override suspend fun generateGroupCode(): String {
        return firestoreDataSource.generateGroupCode()
    }
    
    override suspend fun validateGroupCode(code: String): Boolean {
        return firestoreDataSource.validateGroupCode(code)
    }
}


