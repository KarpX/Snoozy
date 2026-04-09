package com.wem.snoozy.data.dto

data class CreateAlarmRequest(
    val title: String,
    val alarmTime: String,
    val enabled: Boolean = true,
    val repeatDays: List<String> = emptyList(),
    val soundName: String? = null,
    val difficultyLevel: Int? = null
)