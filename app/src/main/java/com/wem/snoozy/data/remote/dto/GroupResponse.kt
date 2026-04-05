package com.wem.snoozy.data.remote.dto

data class GroupResponse(
    val id: Int,
    val name: String,
    val ownerId: Int,
    val url: String?,
    val avatarUrl: String?,
    val members: List<MemberDto>,
)
