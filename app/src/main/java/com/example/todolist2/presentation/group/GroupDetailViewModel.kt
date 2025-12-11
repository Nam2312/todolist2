package com.example.todolist2.presentation.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist2.domain.model.Group
import com.example.todolist2.domain.model.GroupMember
import com.example.todolist2.domain.model.GroupRole
import com.example.todolist2.domain.repository.AuthRepository
import com.example.todolist2.domain.repository.GroupRepository
import com.example.todolist2.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupDetailState(
    val group: Group? = null,
    val members: List<GroupMember> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteDialog: Boolean = false,
    val isOwner: Boolean = false,
    val currentUserId: String? = null
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(GroupDetailState())
    val state = _state.asStateFlow()
    
    private val userId: String?
        get() = authRepository.getCurrentUser()?.uid
    
    fun loadGroup(groupId: String) {
        val uid = userId ?: return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, currentUserId = uid) }
            
            // Load group
            groupRepository.observeGroup(groupId)
                .catch { e ->
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { group ->
                    val isOwner = group?.ownerId == uid
                    _state.update { 
                        it.copy(
                            group = group,
                            isOwner = isOwner,
                            isLoading = false
                        )
                    }
                }
            
            // Load members
            groupRepository.observeGroupMembers(groupId)
                .catch { e ->
                    _state.update { it.copy(error = e.message) }
                }
                .collect { members ->
                    _state.update { it.copy(members = members) }
                }
        }
    }
    
    fun showDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = true) }
    }
    
    fun hideDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = false) }
    }
    
    fun deleteGroup() {
        val uid = userId ?: return
        val groupId = _state.value.group?.id ?: return
        
        viewModelScope.launch {
            groupRepository.deleteGroup(uid, groupId)
        }
    }
    
    fun leaveGroup() {
        val uid = userId ?: return
        val groupId = _state.value.group?.id ?: return
        
        viewModelScope.launch {
            groupRepository.leaveGroup(uid, groupId)
        }
    }
    
    fun removeMember(memberId: String) {
        val uid = userId ?: return
        val groupId = _state.value.group?.id ?: return
        
        viewModelScope.launch {
            groupRepository.removeMember(uid, groupId, memberId)
        }
    }
    
    fun updateMemberRole(memberId: String, role: GroupRole) {
        val uid = userId ?: return
        val groupId = _state.value.group?.id ?: return
        
        viewModelScope.launch {
            groupRepository.updateMemberRole(uid, groupId, memberId, role)
        }
    }
}




