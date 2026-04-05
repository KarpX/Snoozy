package com.wem.snoozy.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MemberDto(
    val id: Int,
    val username: String,
    val avatarUrl: String?
)
