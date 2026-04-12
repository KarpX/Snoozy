package com.wem.snoozy.data.dto

data class AlarmActionDto(
    val id: Long?,
    val alarmId: Long,
    val actorUserId: Long,
    val targetUserId: Long,
    val actionType: String,
    val status: String,
    val executedAt: String?,
    val messageText: String?
)