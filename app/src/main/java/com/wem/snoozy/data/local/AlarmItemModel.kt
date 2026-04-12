package com.wem.snoozy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("alarms")
data class AlarmItemModel(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val ringDay: String,
    val ringHours: String,
    val ringHoursMillis: Int,
    val timeToBed: String,
    val enabled: Boolean,
    val repeatDays: String,
    @androidx.room.ColumnInfo(index = true) val remoteId: Long? = null,
    val isOverslept: Boolean = false
)