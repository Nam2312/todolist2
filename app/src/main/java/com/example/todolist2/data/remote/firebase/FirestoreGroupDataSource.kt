package com.example.todolist2.data.remote.firebase

import com.example.todolist2.domain.model.Group
import com.example.todolist2.domain.model.GroupMember
import com.example.todolist2.domain.model.GroupRole
import com.example.todolist2.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore data source cho Groups
 */
@Singleton
class FirestoreGroupDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    // Groups
    
    fun observeUserGroups(userId: String): Flow<List<Group>> = callbackFlow {
        // Get groups where user is owner
        val ownerGroupsRef = firestore.collection("groups")
            .whereEqualTo("ownerId", userId)
            .whereEqualTo("isActive", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
        
        val subscription = ownerGroupsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            // Get groups where user is owner
            val ownerGroups = snapshot?.documents?.mapNotNull {
                it.toObject(Group::class.java)?.copy(id = it.id)
            } ?: emptyList()
            
            // For groups where user is member, we'll rely on local database
            // which is updated when user joins a group
            // This is a simplified approach - for production, consider maintaining memberIds array in Group
            trySend(ownerGroups)
        }
        awaitClose { subscription.remove() }
    }
    
    fun observeGroup(groupId: String): Flow<Group?> = callbackFlow {
        val subscription = firestore.collection("groups")
            .document(groupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val group = snapshot?.toObject(Group::class.java)?.copy(id = snapshot.id)
                trySend(group)
            }
        awaitClose { subscription.remove() }
    }
    
    suspend fun getGroupByCode(code: String): Group? {
        return try {
            // Try exact match first
            var snapshot = firestore.collection("groups")
                .whereEqualTo("code", code.uppercase())
                .whereEqualTo("isActive", true)
                .limit(1)
                .get()
                .await()
            
            var group = snapshot.documents.firstOrNull()?.let {
                it.toObject(Group::class.java)?.copy(id = it.id)
            }
            
            // If not found, try case-insensitive search
            if (group == null) {
                snapshot = firestore.collection("groups")
                    .whereEqualTo("isActive", true)
                    .limit(50) // Get more to filter client-side
                    .get()
                    .await()
                
                group = snapshot.documents
                    .mapNotNull { it.toObject(Group::class.java)?.copy(id = it.id) }
                    .firstOrNull { it.code.uppercase() == code.uppercase() }
            }
            
            group
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun createGroup(userId: String, group: Group): Resource<Group> {
        return try {
            // Check if code already exists
            val normalizedCode = group.code.uppercase().trim()
            val existingGroup = getGroupByCode(normalizedCode)
            if (existingGroup != null) {
                return Resource.Error("Mã nhóm đã tồn tại")
            }
            
            val docRef = firestore.collection("groups").document()
            // Ensure code is uppercase and isActive is true
            val newGroup = group.copy(
                id = docRef.id,
                code = normalizedCode,
                isActive = true,
                memberCount = 1 // Owner is first member
            )
            docRef.set(newGroup).await()
            
            // Add owner as member
            val ownerMember = GroupMember(
                id = "${docRef.id}_$userId",
                groupId = docRef.id,
                userId = userId,
                userName = "", // Will be filled from user profile
                userEmail = "",
                userAvatarUrl = "",
                role = GroupRole.OWNER,
                joinedAt = System.currentTimeMillis(),
                isActive = true
            )
            firestore.collection("groups")
                .document(docRef.id)
                .collection("members")
                .document(ownerMember.id)
                .set(ownerMember)
                .await()
            
            Resource.Success(newGroup)
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error(e.message ?: "Failed to create group")
        }
    }
    
    suspend fun updateGroup(userId: String, group: Group): Resource<Unit> {
        return try {
            // Verify user is owner or admin
            val member = getGroupMember(group.id, userId)
            if (member?.role != GroupRole.OWNER && member?.role != GroupRole.ADMIN) {
                return Resource.Error("Only owner or admin can update group")
            }
            
            firestore.collection("groups")
                .document(group.id)
                .set(group)
                .await()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update group")
        }
    }
    
    suspend fun deleteGroup(userId: String, groupId: String): Resource<Unit> {
        return try {
            // Verify user is owner
            val member = getGroupMember(groupId, userId)
            if (member?.role != GroupRole.OWNER) {
                return Resource.Error("Only owner can delete group")
            }
            
            firestore.collection("groups")
                .document(groupId)
                .update("isActive", false)
                .await()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete group")
        }
    }
    
    // Group Members
    
    fun observeGroupMembers(groupId: String): Flow<List<GroupMember>> = callbackFlow {
        val subscription = firestore.collection("groups")
            .document(groupId)
            .collection("members")
            .whereEqualTo("isActive", true)
            .orderBy("joinedAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val members = snapshot?.documents?.mapNotNull {
                    it.toObject(GroupMember::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(members)
            }
        awaitClose { subscription.remove() }
    }
    
    suspend fun getGroupMember(groupId: String, userId: String): GroupMember? {
        return try {
            val snapshot = firestore.collection("groups")
                .document(groupId)
                .collection("members")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .limit(1)
                .get()
                .await()
            
            snapshot.documents.firstOrNull()?.let {
                it.toObject(GroupMember::class.java)?.copy(id = it.id)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun joinGroup(userId: String, groupCode: String, userName: String, userEmail: String, userAvatarUrl: String): Resource<GroupMember> {
        return try {
            val group = getGroupByCode(groupCode)
                ?: return Resource.Error("Group not found")
            
            // Check if already a member
            val existingMember = getGroupMember(group.id, userId)
            if (existingMember != null) {
                return Resource.Error("Already a member of this group")
            }
            
            val member = GroupMember(
                id = "${group.id}_$userId",
                groupId = group.id,
                userId = userId,
                userName = userName,
                userEmail = userEmail,
                userAvatarUrl = userAvatarUrl,
                role = GroupRole.MEMBER,
                joinedAt = System.currentTimeMillis()
            )
            
            firestore.collection("groups")
                .document(group.id)
                .collection("members")
                .document(member.id)
                .set(member)
                .await()
            
            // Update member count
            firestore.collection("groups")
                .document(group.id)
                .update("memberCount", group.memberCount + 1)
                .await()
            
            Resource.Success(member)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to join group")
        }
    }
    
    suspend fun leaveGroup(userId: String, groupId: String): Resource<Unit> {
        return try {
            val member = getGroupMember(groupId, userId)
                ?: return Resource.Error("Not a member of this group")
            
            if (member.role == GroupRole.OWNER) {
                return Resource.Error("Owner cannot leave group. Transfer ownership or delete group instead.")
            }
            
            firestore.collection("groups")
                .document(groupId)
                .collection("members")
                .document(member.id)
                .update("isActive", false)
                .await()
            
            // Update member count
            val group = firestore.collection("groups")
                .document(groupId)
                .get()
                .await()
                .toObject(Group::class.java)
            
            if (group != null) {
                firestore.collection("groups")
                    .document(groupId)
                    .update("memberCount", (group.memberCount - 1).coerceAtLeast(0))
                    .await()
            }
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to leave group")
        }
    }
    
    suspend fun removeMember(ownerId: String, groupId: String, memberId: String): Resource<Unit> {
        return try {
            // Verify requester is owner or admin
            val requester = getGroupMember(groupId, ownerId)
            if (requester?.role != GroupRole.OWNER && requester?.role != GroupRole.ADMIN) {
                return Resource.Error("Only owner or admin can remove members")
            }
            
            val member = getGroupMember(groupId, memberId)
                ?: return Resource.Error("Member not found")
            
            if (member.role == GroupRole.OWNER) {
                return Resource.Error("Cannot remove owner")
            }
            
            firestore.collection("groups")
                .document(groupId)
                .collection("members")
                .document(member.id)
                .update("isActive", false)
                .await()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to remove member")
        }
    }
    
    suspend fun updateMemberRole(ownerId: String, groupId: String, memberId: String, role: GroupRole): Resource<Unit> {
        return try {
            // Verify requester is owner
            val requester = getGroupMember(groupId, ownerId)
            if (requester?.role != GroupRole.OWNER) {
                return Resource.Error("Only owner can change member roles")
            }
            
            val member = getGroupMember(groupId, memberId)
                ?: return Resource.Error("Member not found")
            
            firestore.collection("groups")
                .document(groupId)
                .collection("members")
                .document(member.id)
                .update("role", role.name)
                .await()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update member role")
        }
    }
    
    suspend fun generateGroupCode(): String {
        // Generate unique 6-character code (letters and numbers, uppercase only)
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        var code: String
        var attempts = 0
        
        do {
            code = (1..6)
                .map { chars.random() }
                .joinToString("")
                .uppercase()
            attempts++
        } while (getGroupByCode(code) != null && attempts < 10)
        
        return code
    }
    
    suspend fun validateGroupCode(code: String): Boolean {
        return getGroupByCode(code) != null
    }
}

