package com.wem.snoozy.data.remote.dto

data class AlarmResponseDto(
    val id: Int,
    val ownerId: Int,
    val title: String,
    val alarmTime: String,
    val enabled: Boolean,
    val repeatDays: List<String>,
    val soundName: String,
    val difficultyLevel: Int,
    val isOverslept: Boolean
)
