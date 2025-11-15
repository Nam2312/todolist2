package com.example.todolist2.presentation.auth.signup

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist2.domain.repository.AuthRepository
import com.example.todolist2.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpState(
    val username: String = "",  // Changed from displayName
    val email: String = "",
    val password: String = "",  // Only used for Firebase Auth, NOT saved to Firestore
    val isLoading: Boolean = false,
    val isSignUpSuccessful: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "SignUpViewModel"
    }
    
    private val _state = MutableStateFlow(SignUpState())
    val state = _state.asStateFlow()
    
    fun onUsernameChange(name: String) {
        _state.update { it.copy(username = name, error = null) }
    }
    
    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, error = null) }
    }
    
    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, error = null) }
    }
    
    fun signUp() {
        // Prevent multiple clicks
        if (_state.value.isLoading) {
            return
        }
        
        Log.d(TAG, "📝 Bắt đầu đăng ký...")
        
        // Validate input
        val username = _state.value.username.trim()
        val email = _state.value.email.trim()
        val password = _state.value.password
        
        if (username.isEmpty()) {
            _state.update { it.copy(error = "Vui lòng nhập tên người dùng") }
            Log.w(TAG, "❌ Username trống")
            return
        }
        
        if (username.length < 2) {
            _state.update { it.copy(error = "Tên người dùng phải có ít nhất 2 ký tự") }
            Log.w(TAG, "❌ Username quá ngắn")
            return
        }
        
        // Validate username format (only letters, numbers, underscore)
        if (!username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
            _state.update { it.copy(error = "Tên người dùng chỉ được chứa chữ, số và dấu gạch dưới") }
            Log.w(TAG, "❌ Username không hợp lệ")
            return
        }
        
        if (email.isEmpty()) {
            _state.update { it.copy(error = "Vui lòng nhập email") }
            Log.w(TAG, "❌ Email trống")
            return
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _state.update { it.copy(error = "Email không hợp lệ") }
            Log.w(TAG, "❌ Email không hợp lệ: $email")
            return
        }
        
        if (password.isEmpty()) {
            _state.update { it.copy(error = "Vui lòng nhập mật khẩu") }
            Log.w(TAG, "❌ Mật khẩu trống")
            return
        }
        
        if (password.length < 6) {
            _state.update { it.copy(error = "Mật khẩu phải có ít nhất 6 ký tự") }
            Log.w(TAG, "❌ Mật khẩu quá ngắn")
            return
        }
        
        // Check password strength
        if (!password.any { it.isDigit() }) {
            Log.w(TAG, "⚠️ Mật khẩu không có số (vẫn cho phép)")
        }
        
        // Set loading state immediately for instant UI feedback (on main thread)
        _state.value = _state.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            Log.d(TAG, "⏳ Đang tạo tài khoản trên Firebase...")
            Log.d(TAG, "📧 Email: $email")
            Log.d(TAG, "👤 Username: $username")
            Log.d(TAG, "🔐 Password: *** (sẽ được mã hóa bởi Firebase Auth)")
            
            try {
                val result = authRepository.signUpWithEmail(
                    email = email,
                    password = password,
                    displayName = username  // Pass username as displayName to repository
                )
                
                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Đăng ký thành công!")
                        Log.d(TAG, "🆔 User ID: ${result.data.id}")
                        Log.d(TAG, "📧 Email: ${result.data.email}")
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                isSignUpSuccessful = true
                            )
                        }
                    }
                    is Resource.Error -> {
                        val errorMessage = parseFirebaseError(result.message)
                        Log.e(TAG, "❌ Lỗi đăng ký: ${result.message}")
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = errorMessage
                            )
                        }
                    }
                    else -> {
                        Log.w(TAG, "⚠️ Kết quả không xác định")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Exception: ${e.message}", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Lỗi không xác định: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Parse Firebase error messages to Vietnamese
     */
    private fun parseFirebaseError(error: String?): String {
        Log.d(TAG, "🔍 Phân tích lỗi: $error")
        
        return when {
            error == null -> "Lỗi không xác định"
            error.contains("API key not valid", ignoreCase = true) -> {
                Log.e(TAG, "🚨 FIREBASE CHƯA SETUP!")
                "⚠️ Chưa cấu hình Firebase!\n\nVui lòng:\n1. Tạo Firebase project\n2. Tải file google-services.json\n3. Copy vào thư mục app/\n4. Bật Email/Password Authentication\n\nXem hướng dẫn: FIX_LOGIN_ERROR.md"
            }
            error.contains("network", ignoreCase = true) || 
            error.contains("connection", ignoreCase = true) -> 
                "Không có kết nối mạng"
            error.contains("email address is already", ignoreCase = true) || 
            error.contains("already in use", ignoreCase = true) -> 
                "Email đã được đăng ký"
            error.contains("email address is badly formatted", ignoreCase = true) -> 
                "Email không hợp lệ"
            error.contains("weak password", ignoreCase = true) -> 
                "Mật khẩu quá yếu. Vui lòng dùng mật khẩu mạnh hơn"
            error.contains("too many requests", ignoreCase = true) -> 
                "Quá nhiều lần thử. Vui lòng đợi"
            else -> error
        }
    }
}

