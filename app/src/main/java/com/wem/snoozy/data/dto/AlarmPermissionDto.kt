package com.wem.snoozy.data.dto

data class AlarmPermissionDto(
    val id: Long?,
    val ownerId: Long?,
    val targetUserId: Long,
    val permissionType: String,
    val active: Boolean,
    val createdAt: String?
)