package com.example.todolist2.presentation.auth.login

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

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    companion object {
        private const val TAG = "LoginViewModel"
    }
    
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()
    
    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, error = null) }
    }
    
    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, error = null) }
    }
    
    fun login() {
        // Prevent multiple clicks
        if (_state.value.isLoading) {
            return
        }
        
        Log.d(TAG, "🔐 Bắt đầu đăng nhập...")
        
        // Validate input
        val email = _state.value.email.trim()
        val password = _state.value.password
        
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
        
        // Set loading state immediately for instant UI feedback (on main thread)
        _state.value = _state.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            Log.d(TAG, "⏳ Đang gửi request đến Firebase...")
            
            try {
                val result = authRepository.signInWithEmail(
                    email = email,
                    password = password
                )
                
                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Đăng nhập thành công: ${result.data.email}")
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                isLoginSuccessful = true
                            )
                        }
                    }
                    is Resource.Error -> {
                        val errorMessage = parseFirebaseError(result.message)
                        Log.e(TAG, "❌ Lỗi đăng nhập: ${result.message}")
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
        return when {
            error == null -> "Lỗi không xác định"
            error.contains("API key not valid", ignoreCase = true) -> 
                "⚠️ Chưa cấu hình Firebase!\n\nVui lòng:\n1. Tạo Firebase project\n2. Tải file google-services.json\n3. Copy vào thư mục app/\n\nXem hướng dẫn: FIX_LOGIN_ERROR.md"
            error.contains("network", ignoreCase = true) || 
            error.contains("connection", ignoreCase = true) -> 
                "Không có kết nối mạng"
            error.contains("password is invalid", ignoreCase = true) || 
            error.contains("wrong password", ignoreCase = true) -> 
                "Mật khẩu không đúng"
            error.contains("no user record", ignoreCase = true) || 
            error.contains("user not found", ignoreCase = true) -> 
                "Email chưa được đăng ký"
            error.contains("too many requests", ignoreCase = true) -> 
                "Quá nhiều lần thử. Vui lòng đợi"
            error.contains("email address is badly formatted", ignoreCase = true) -> 
                "Email không hợp lệ"
            error.contains("disabled", ignoreCase = true) -> 
                "Tài khoản đã bị khóa"
            else -> error
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    fun handleGoogleSignIn() {
        // Placeholder for Google Sign-In
        // TODO: Implement Google Sign-In flow
        // This requires:
        // 1. Add dependency: implementation("com.google.android.gms:play-services-auth:20.7.0")
        // 2. Configure OAuth 2.0 client ID in Firebase Console
        // 3. Implement GoogleSignInClient and Activity Result API
        // For now, showing error message
        _state.update { 
            it.copy(
                error = "Google Sign-In chưa được cấu hình. Vui lòng sử dụng đăng nhập bằng email."
            )
        }
    }
    
    fun signInWithGoogle(idToken: String) {
        if (_state.value.isLoading) {
            return
        }
        
        Log.d(TAG, "🔐 Bắt đầu đăng nhập với Google...")
        _state.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                val result = authRepository.signInWithGoogle(idToken)
                
                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Đăng nhập Google thành công: ${result.data.email}")
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                isLoginSuccessful = true
                            )
                        }
                    }
                    is Resource.Error -> {
                        val errorMessage = parseFirebaseError(result.message)
                        Log.e(TAG, "❌ Lỗi đăng nhập Google: ${result.message}")
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
}

