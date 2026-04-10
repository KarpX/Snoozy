package com.wem.snoozy.data.dto

data class AlarmDto(
    val id: Long,
    val ownerId: Long,
    val title: String,
    val alarmTime: String,
    val enabled: Boolean,
    val repeatDays: List<String>,
    val soundName: String?,
    val difficultyLevel: Int?,
    val overslept: Boolean,
    val createdAt: String?,
    val updatedAt: String?
)