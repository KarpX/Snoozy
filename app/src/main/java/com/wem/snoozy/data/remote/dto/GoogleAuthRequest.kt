package com.wem.snoozy.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GoogleAuthRequest(
    @SerializedName("idToken")
    val idToken: String,
    @SerializedName("phoneNumber")
    val phoneNumber: String? = null
)
