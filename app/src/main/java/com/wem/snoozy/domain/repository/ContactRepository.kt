package com.wem.snoozy.domain.repository

import com.wem.snoozy.domain.entity.ContactItem
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun fetchContacts(): Flow<List<ContactItem>>
}
