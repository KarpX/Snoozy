package com.wem.snoozy.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
