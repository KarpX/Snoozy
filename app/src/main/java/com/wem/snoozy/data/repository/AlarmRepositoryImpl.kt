package com.wem.snoozy.data.repository

import com.wem.snoozy.data.alarm.AlarmScheduler
import com.wem.snoozy.data.local.Dao
import com.wem.snoozy.data.mapper.toAlarmItem
import com.wem.snoozy.data.mapper.toAlarmItemModel
import com.wem.snoozy.data.mapper.toAlarmItemsFlow
import com.wem.snoozy.domain.entity.AlarmItem
import com.wem.snoozy.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    private val dao: Dao,
    private val alarmScheduler: AlarmScheduler
) : AlarmRepository {

    override suspend fun addNewAlarm(alarmItem: AlarmItem) {
        val id = dao.addAlarm(alarmItem.toAlarmItemModel()).toInt()
        val savedAlarm = alarmItem.copy(id = id)
        if (savedAlarm.checked) {
            alarmScheduler.scheduleBedtimeNotification(savedAlarm)
        }
    }

    override suspend fun editAlarm(alarmItem: AlarmItem) {
        dao.addAlarm(alarmItem.toAlarmItemModel())
        if (alarmItem.checked) {
            alarmScheduler.scheduleBedtimeNotification(alarmItem)
        } else {
            alarmScheduler.cancelBedtimeNotification(alarmItem.id)
        }
    }

    override fun getAllAlarms(): Flow<List<AlarmItem>> {
        return dao.getAlarms().toAlarmItemsFlow()
    }

    override suspend fun toggleAlarmState(alarmItem: AlarmItem) {
        val newCheckedState = !alarmItem.checked
        dao.updateCheckedStatus(alarmItem.id, newCheckedState)
        val updatedAlarm = alarmItem.copy(checked = newCheckedState)
        if (newCheckedState) {
            alarmScheduler.scheduleBedtimeNotification(updatedAlarm)
        } else {
            alarmScheduler.cancelBedtimeNotification(updatedAlarm.id)
        }
    }

    override suspend fun deleteAlarm(alarmId: Int) {
        dao.deleteAlarm(alarmId)
        alarmScheduler.cancelBedtimeNotification(alarmId)
    }
}