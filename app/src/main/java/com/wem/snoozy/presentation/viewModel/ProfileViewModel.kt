package com.wem.snoozy.presentation.viewModel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wem.snoozy.data.remote.dto.UserResponse
import com.wem.snoozy.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val user: UserResponse, val isUploading: Boolean = false) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            userRepository.getCurrentUser()
                .onSuccess { user ->
                    _uiState.value = ProfileUiState.Success(user)
                }
                .onFailure { error ->
                    _uiState.value = ProfileUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun uploadAvatar(uri: Uri) {
        val currentState = _uiState.value
        if (currentState !is ProfileUiState.Success) return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isUploading = true)
            
            val file = saveUriToFile(uri)
            if (file != null) {
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                
                userRepository.uploadAvatar(body)
                    .onSuccess { newUrl ->
                        _uiState.value = ProfileUiState.Success(
                            currentState.user.copy(avatarLink = newUrl),
                            isUploading = false
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = ProfileUiState.Error(error.message ?: "Upload failed")
                    }
            } else {
                _uiState.value = currentState.copy(isUploading = false)
            }
        }
    }

    private suspend fun saveUriToFile(uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val file = File(context.cacheDir, "temp_avatar_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
