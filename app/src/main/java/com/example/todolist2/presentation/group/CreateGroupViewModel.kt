package com.example.todolist2.presentation.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist2.domain.model.Group
import com.example.todolist2.domain.repository.AuthRepository
import com.example.todolist2.domain.repository.GroupRepository
import com.example.todolist2.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CreateGroupState(
    val groupName: String = "",
    val description: String = "",
    val groupCode: String = "",
    val selectedColor: String = "#6200EE",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(CreateGroupState())
    val state = _state.asStateFlow()
    
    private val userId: String?
        get() = authRepository.getCurrentUser()?.uid
    
    init {
        generateNewCode()
    }
    
    fun updateGroupName(name: String) {
        _state.update { it.copy(groupName = name, error = null) }
    }
    
    fun updateDescription(description: String) {
        _state.update { it.copy(description = description) }
    }
    
    fun selectColor(color: String) {
        _state.update { it.copy(selectedColor = color) }
    }
    
    fun generateNewCode() {
        viewModelScope.launch {
            _state.update { it.copy(groupCode = "", isLoading = true) }
            val code = groupRepository.generateGroupCode()
            _state.update { it.copy(groupCode = code, isLoading = false) }
        }
    }
    
    fun createGroup() {
        val uid = userId ?: return
        val state = _state.value
        
        // Prevent double creation
        if (state.isLoading || state.isSuccess) {
            return
        }
        
        if (state.groupName.isBlank()) {
            _state.update { it.copy(error = "Vui lòng nhập tên nhóm") }
            return
        }
        
        if (state.groupCode.isBlank()) {
            _state.update { it.copy(error = "Mã nhóm chưa được tạo") }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val group = Group(
                id = UUID.randomUUID().toString(),
                name = state.groupName.trim(),
                description = state.description.trim(),
                code = state.groupCode.uppercase().trim(),
                ownerId = uid,
                color = state.selectedColor,
                isActive = true,
                memberCount = 1
            )
            
            when (val result = groupRepository.createGroup(uid, group)) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Resource.Error -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Không thể tạo nhóm"
                        )
                    }
                }
                is Resource.Loading -> {
                    // Already loading
                }
            }
        }
    }
}

