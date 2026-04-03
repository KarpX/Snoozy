package com.wem.snoozy.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wem.snoozy.data.remote.dto.GoogleAuthRequest
import com.wem.snoozy.data.remote.dto.LoginRequest
import com.wem.snoozy.data.remote.dto.RegisterRequest
import com.wem.snoozy.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    data class NeedPhone(val idToken: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(phone: String, pass: String) {
        if (phone.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Заполните все поля")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.login(LoginRequest(phone, pass))
            result.onSuccess {
                _uiState.value = AuthUiState.Success
            }.onFailure {
                _uiState.value = AuthUiState.Error(it.message ?: "Ошибка входа")
            }
        }
    }

    fun register(username: String, phone: String, pass: String, confirm: String) {
        if (username.isBlank() || phone.isBlank() || pass.isBlank() || confirm.isBlank()) {
            _uiState.value = AuthUiState.Error("Заполните все поля")
            return
        }
        if (pass != confirm) {
            _uiState.value = AuthUiState.Error("Пароли не совпадают")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.register(RegisterRequest(username, phone, pass, confirm))
            result.onSuccess {
                _uiState.value = AuthUiState.Success
            }.onFailure {
                _uiState.value = AuthUiState.Error(it.message ?: "Ошибка регистрации")
            }
        }
    }

    fun googleAuth(idToken: String, phoneNumber: String? = null) {
        if (idToken.isBlank()) {
            _uiState.value = AuthUiState.Error("Google ID Token is empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.googleAuth(GoogleAuthRequest(idToken, phoneNumber))
            result.onSuccess {
                _uiState.value = AuthUiState.Success
            }.onFailure { exception ->
                val message = exception.message ?: ""
                if (message.contains("401") || message.contains("phone") || message.contains("UNAUTHORIZED")) {
                    // Если сервер вернул 401 или ошибку отсутствия телефона
                    _uiState.value = AuthUiState.NeedPhone(idToken)
                } else {
                    _uiState.value = AuthUiState.Error(message.ifBlank { "Google auth failed" })
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
