package com.wem.snoozy.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.ContextCompat
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
        // Проверяем разрешение прямо перед запросом
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            Log.e("ContactRepository", "Permission READ_CONTACTS not granted")
            emit(emptyList())
            return@flow
        }

        val contacts = mutableListOf<ContactItem>()
        try {
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

                if (idColumn == -1 || nameColumn == -1 || numberColumn == -1) {
                    Log.e("ContactRepository", "One of the columns was not found")
                    return@use
                }

                while (it.moveToNext()) {
                    val id = it.getString(idColumn)
                    val name = it.getString(nameColumn) ?: "No Name"
                    val number = it.getString(numberColumn) ?: ""
                    val photoUri = it.getString(photoColumn)?.let { uri -> android.net.Uri.parse(uri) }

                    if (contacts.none { c -> c.phoneNumber == number }) {
                        contacts.add(ContactItem(id, name, number, photoUri))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ContactRepository", "Error fetching contacts", e)
        }
        emit(contacts)
    }.flowOn(Dispatchers.IO)
}
