package com.wem.snoozy.domain.repository

import com.wem.snoozy.domain.entity.AlarmItem
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {

    suspend fun addNewAlarm(alarmItem: AlarmItem)

    suspend fun editAlarm(alarmItem: AlarmItem)

    fun getAllAlarms(): Flow<List<AlarmItem>>

    suspend fun toggleAlarmState(alarmItem: AlarmItem)

    suspend fun deleteAlarm(alarmId: Int)

    suspend fun syncRemoteAlarms()

    suspend fun updateOversleptStatus(alarmId: Int, isOverslept: Boolean)

    suspend fun updateAlarmAfterRing(alarmId: Int)

    suspend fun grantPermission(targetUserId: Long, permissionType: String): Boolean
}
