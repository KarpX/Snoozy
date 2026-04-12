package com.wem.snoozy.data.dto

data class GrantPermissionRequest(
    val targetUserId: Long,
    val permissionType: String
)