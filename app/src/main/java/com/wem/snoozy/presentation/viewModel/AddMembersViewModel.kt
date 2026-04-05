package com.wem.snoozy.presentation.viewModel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wem.snoozy.data.local.UserPreferencesManager
import com.wem.snoozy.data.repository.GroupsRepositoryImpl
import com.wem.snoozy.domain.entity.ContactItem
import com.wem.snoozy.domain.repository.AlarmRepository
import com.wem.snoozy.domain.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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
        viewModelScope.launch {
            contactRepository.fetchContacts().collect { contacts ->
                _allContacts.value = contacts
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
            
            // Собираем список ID участников. 
            // Обязательно добавляем текущего пользователя, если его еще нет в списке.
            val memberIds = selectedContacts.mapNotNull { it.id.toIntOrNull() }.toMutableList()
            if (currentUserId != null && !memberIds.contains(currentUserId)) {
                memberIds.add(currentUserId)
            }

            val createdGroup = groupsRepository.createGroup(name, memberIds)
            
            if (createdGroup != null && avatarUriString != null) {
                val localFile = saveAvatarToInternalStorage(Uri.parse(avatarUriString))
                if (localFile != null) {
                    val file = File(localFile)
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                    
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

    private suspend fun saveAvatarToInternalStorage(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val fileName = "group_avatar_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, "avatars")
            if (!file.exists()) file.mkdirs()
            
            val outputFile = File(file, fileName)
            FileOutputStream(outputFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
