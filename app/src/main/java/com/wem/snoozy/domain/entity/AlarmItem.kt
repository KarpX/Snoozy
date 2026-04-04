package com.wem.snoozy.domain.entity

data class AlarmItem(
    val id: Int,
    val ringDay: String,
    val ringHours: String,
    val timeToBed: String,
    val checked: Boolean,
    val repeatDays: String,
    val isOverslept: Boolean = false
)