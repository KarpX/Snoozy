package com.wem.snoozy.presentation.viewModel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
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
            
            val file = saveCompressedImage(uri)
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

    private suspend fun saveCompressedImage(uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            var originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return@withContext null
            
            // Исправляем ориентацию на основе EXIF данных
            originalBitmap = rotateImageIfRequired(originalBitmap, uri)
            
            val maxSize = 1024
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scale = Math.min(maxSize.toFloat() / width, maxSize.toFloat() / height).coerceAtMost(1f)
            
            val scaledBitmap = if (scale < 1f) {
                Bitmap.createScaledBitmap(originalBitmap, (width * scale).toInt(), (height * scale).toInt(), true)
            } else {
                originalBitmap
            }

            val file = File(context.cacheDir, "compressed_avatar_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            
            if (scaledBitmap != originalBitmap) {
                scaledBitmap.recycle()
            }
            originalBitmap.recycle()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun rotateImageIfRequired(bitmap: Bitmap, uri: Uri): Bitmap {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return bitmap
        val ei = try {
            ExifInterface(inputStream)
        } catch (e: Exception) {
            return bitmap
        }
        
        val orientation = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            else -> bitmap
        }
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        val rotatedBitmap = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        if (rotatedBitmap != source) {
            source.recycle()
        }
        return rotatedBitmap
    }
}
