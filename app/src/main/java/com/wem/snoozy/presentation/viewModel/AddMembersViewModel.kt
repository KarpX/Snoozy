package com.wem.snoozy.presentation.viewModel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wem.snoozy.data.local.UserPreferencesManager
import com.wem.snoozy.data.repository.GroupsRepositoryImpl
import com.wem.snoozy.domain.entity.ContactItem
import com.wem.snoozy.domain.repository.AlarmRepository
import com.wem.snoozy.domain.repository.ContactRepository
import com.wem.snoozy.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class AddMembersState(
    val contacts: List<ContactItem> = emptyList(),
    val searchText: String = "",
    val isLoading: Boolean = false,
    val selectedContactIds: Set<String> = emptySet()
)

@HiltViewModel
class AddMembersViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val alarmRepository: AlarmRepository,
    private val groupsRepository: GroupsRepositoryImpl,
    private val userRepository: UserRepository,
    private val userPreferencesManager: UserPreferencesManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _searchText = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _selectedContactIds = MutableStateFlow<Set<String>>(emptySet())
    private val _allContacts = MutableStateFlow<List<ContactItem>>(emptyList())

    val state: StateFlow<AddMembersState> = combine(
        _allContacts,
        _searchText,
        _isLoading,
        _selectedContactIds
    ) { contacts, searchText, isLoading, selectedIds ->
        val filteredContacts = if (searchText.isBlank()) {
            contacts
        } else {
            contacts.filter { it.name.contains(searchText, ignoreCase = true) || it.phoneNumber.contains(searchText) }
        }
        
        AddMembersState(
            contacts = filteredContacts.map { it.copy(isSelected = selectedIds.contains(it.id)) },
            searchText = searchText,
            isLoading = isLoading,
            selectedContactIds = selectedIds
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AddMembersState())

    fun loadContacts() {
        if (_isLoading.value) return
        
        _isLoading.value = true
        _allContacts.value = emptyList()
        viewModelScope.launch {
            try {
                contactRepository.fetchContacts().collect { localContacts ->
                    localContacts.chunked(10).forEach { chunk ->
                        val registeredBatch = withContext(Dispatchers.IO) {
                            chunk.map { contact ->
                                async {
                                    try {
                                        val user = userRepository.checkUserByPhone(contact.phoneNumber)
                                        if (user != null) {
                                            contact.copy(
                                                id = user.id.toString(),
                                                photoUri = user.avatarLink?.let { Uri.parse(it) } ?: contact.photoUri
                                            )
                                        } else {
                                            null
                                        }
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                            }.awaitAll().filterNotNull()
                        }
                        
                        if (registeredBatch.isNotEmpty()) {
                            _allContacts.update { current -> current + registeredBatch }
                        }
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchText.value = query
    }

    fun toggleSelection(contactId: String) {
        _selectedContactIds.update { current ->
            if (current.contains(contactId)) current - contactId else current + contactId
        }
    }
    
    fun getSelectedContacts(): List<ContactItem> {
        return _allContacts.value.filter { _selectedContactIds.value.contains(it.id) }
    }

    fun clearSelection() {
        _selectedContactIds.value = emptySet()
        _searchText.value = ""
    }

    fun createGroup(name: String, avatarUriString: String?, onComplete: () -> Unit) {
        val selectedContacts = getSelectedContacts()
        if (name.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            
            val currentUserId = userPreferencesManager.userIdFlow.first()
            val memberIds = selectedContacts.mapNotNull { it.id.toIntOrNull() }.toMutableList()
            if (currentUserId != null && !memberIds.contains(currentUserId)) {
                memberIds.add(currentUserId)
            }

            val createdGroup = groupsRepository.createGroup(name, memberIds)
            
            if (createdGroup != null && avatarUriString != null) {
                val compressedFile = saveCompressedImage(Uri.parse(avatarUriString))
                if (compressedFile != null) {
                    val requestFile = compressedFile.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", compressedFile.name, requestFile)
                    groupsRepository.uploadAvatar(createdGroup.id, body)
                }
            }
            
            _isLoading.value = false
            if (createdGroup != null) {
                clearSelection()
                onComplete()
            }
        }
    }

    private suspend fun saveCompressedImage(uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            var originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return@withContext null
            
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

            val file = File(context.cacheDir, "compressed_group_${System.currentTimeMillis()}.jpg")
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
