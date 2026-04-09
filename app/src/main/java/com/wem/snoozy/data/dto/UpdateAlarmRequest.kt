package com.wem.snoozy.data.dto

data class UpdateAlarmRequest(
    val title: String? = null,
    val alarmTime: String? = null,
    val enabled: Boolean? = null,
    val repeatDays: List<String>? = null,
    val soundName: String? = null,
    val difficultyLevel: Int? = null,
    val isOverslept: Boolean? = null
)