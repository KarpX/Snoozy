package com.wem.snoozy.data.remote.dto

data class UserResponse(
    val id: Int,
    val username: String,
    val email: String?,
    val phoneNumber: String,
    val avatarLink: String?
)
