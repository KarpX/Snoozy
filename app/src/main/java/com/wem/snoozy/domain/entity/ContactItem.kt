package com.wem.snoozy.domain.entity

import android.net.Uri

data class ContactItem(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val photoUri: Uri? = null,
    val isSelected: Boolean = false
)
