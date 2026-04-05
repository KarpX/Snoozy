package com.wem.snoozy.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String?,
    @SerializedName("phoneNumber")
    val phoneNumber: String?,
    @SerializedName("avatarLink")
    val avatarLink: String?
)
