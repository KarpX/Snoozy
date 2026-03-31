package com.wem.snoozy.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wem.snoozy.domain.entity.ContactItem
import com.wem.snoozy.domain.entity.GroupItem
import com.wem.snoozy.domain.repository.AlarmRepository
import com.wem.snoozy.domain.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    private val alarmRepository: AlarmRepository
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

    fun createGroup(name: String, onComplete: () -> Unit) {
        val selectedContacts = getSelectedContacts()
        if (name.isBlank() || selectedContacts.isEmpty()) return

        viewModelScope.launch {
            val group = GroupItem(
                name = name,
                membersCount = selectedContacts.size,
                contactIds = selectedContacts.joinToString(",") { it.id }
            )
            alarmRepository.addGroup(group)
            onComplete()
        }
    }
}
