package com.example.todolist2.presentation.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist2.domain.repository.AuthRepository
import com.example.todolist2.domain.repository.GroupRepository
import com.example.todolist2.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class JoinGroupState(
    val groupCode: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class JoinGroupViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(JoinGroupState())
    val state = _state.asStateFlow()
    
    private val userId: String?
        get() = authRepository.getCurrentUser()?.uid
    
    fun updateGroupCode(code: String) {
        _state.update { it.copy(groupCode = code, error = null) }
    }
    
    fun joinGroup() {
        val uid = userId ?: return
        val code = _state.value.groupCode.trim().uppercase()
        
        if (code.length != 6) {
            _state.update { it.copy(error = "Mã nhóm phải có 6 ký tự") }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            // Get user info
            val userResult = authRepository.getUserProfile(uid)
            if (userResult !is Resource.Success) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Không thể lấy thông tin người dùng"
                    )
                }
                return@launch
            }
            
            val user = userResult.data
            
            // Join group
            when (val result = groupRepository.joinGroup(
                userId = uid,
                groupCode = code,
                userName = user.username,
                userEmail = user.email,
                userAvatarUrl = user.avatarUrl
            )) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is Resource.Error -> {
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Không thể tham gia nhóm"
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

