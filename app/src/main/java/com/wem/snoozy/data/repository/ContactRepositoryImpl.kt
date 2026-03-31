package com.wem.snoozy.data.repository

import android.content.Context
import android.provider.ContactsContract
import com.wem.snoozy.domain.entity.ContactItem
import com.wem.snoozy.domain.repository.ContactRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ContactRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ContactRepository {

    override fun fetchContacts(): Flow<List<ContactItem>> = flow {
        val contacts = mutableListOf<ContactItem>()
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            val idColumn = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameColumn = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberColumn = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoColumn = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)

            while (it.moveToNext()) {
                val id = it.getString(idColumn)
                val name = it.getString(nameColumn)
                val number = it.getString(numberColumn)
                val photoUri = it.getString(photoColumn)?.let { uri -> android.net.Uri.parse(uri) }

                // Avoid duplicates if a contact has multiple numbers (simplified for now)
                if (contacts.none { c -> c.phoneNumber == number }) {
                    contacts.add(ContactItem(id, name, number, photoUri))
                }
            }
        }
        emit(contacts)
    }.flowOn(Dispatchers.IO)
}
