package com.example.todolist2.data.local.dao

import androidx.room.*
import com.example.todolist2.data.local.entity.GroupEntity
import com.example.todolist2.data.local.entity.GroupMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    
    // Groups
    @Query("SELECT * FROM groups WHERE isActive = 1")
    fun getAllGroups(): Flow<List<GroupEntity>>
    
    @Query("SELECT * FROM groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: String): GroupEntity?
    
    @Query("SELECT * FROM groups WHERE code = :code")
    suspend fun getGroupByCode(code: String): GroupEntity?
    
    @Query("SELECT * FROM groups WHERE ownerId = :userId OR id IN (SELECT groupId FROM group_members WHERE userId = :userId AND isActive = 1)")
    fun getUserGroups(userId: String): Flow<List<GroupEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)
    
    @Update
    suspend fun updateGroup(group: GroupEntity)
    
    @Delete
    suspend fun deleteGroup(group: GroupEntity)
    
    // Group Members
    @Query("SELECT * FROM group_members WHERE groupId = :groupId AND isActive = 1")
    fun getGroupMembers(groupId: String): Flow<List<GroupMemberEntity>>
    
    @Query("SELECT * FROM group_members WHERE groupId = :groupId AND userId = :userId")
    suspend fun getGroupMember(groupId: String, userId: String): GroupMemberEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: GroupMemberEntity)
    
    @Update
    suspend fun updateMember(member: GroupMemberEntity)
    
    @Delete
    suspend fun deleteMember(member: GroupMemberEntity)
    
    @Query("DELETE FROM group_members WHERE groupId = :groupId")
    suspend fun deleteMembersByGroup(groupId: String)
}



