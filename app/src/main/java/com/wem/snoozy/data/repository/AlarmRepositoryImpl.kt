package com.wem.snoozy.data.repository

import com.wem.snoozy.data.alarm.AlarmScheduler
import com.wem.snoozy.data.local.Dao
import com.wem.snoozy.data.mapper.toAlarmItem
import com.wem.snoozy.data.mapper.toAlarmItemModel
import com.wem.snoozy.data.mapper.toAlarmItemsFlow
import com.wem.snoozy.data.mapper.toGroupItemModel
import com.wem.snoozy.data.mapper.toGroupItemsFlow
import com.wem.snoozy.domain.entity.AlarmItem
import com.wem.snoozy.domain.entity.GroupItem
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
            alarmScheduler.schedule(savedAlarm)
        }
    }

    override suspend fun editAlarm(alarmItem: AlarmItem) {
        dao.addAlarm(alarmItem.toAlarmItemModel())
        if (alarmItem.checked) {
            alarmScheduler.schedule(alarmItem)
        } else {
            alarmScheduler.cancelAlarm(alarmItem.id)
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
            alarmScheduler.schedule(updatedAlarm)
        } else {
            alarmScheduler.cancelAlarm(updatedAlarm.id)
            alarmScheduler.cancelBedtimeNotification(updatedAlarm.id)
        }
    }

    override suspend fun deleteAlarm(alarmId: Int) {
        dao.deleteAlarm(alarmId)
        alarmScheduler.cancelAlarm(alarmId)
        alarmScheduler.cancelBedtimeNotification(alarmId)
    }

    override suspend fun addGroup(groupItem: GroupItem) {
        dao.addGroup(groupItem.toGroupItemModel())
    }

    override fun getGroups(): Flow<List<GroupItem>> {
        return dao.getGroups().toGroupItemsFlow()
    }

    override suspend fun deleteGroup(groupId: Int) {
        dao.deleteGroup(groupId)
    }
}
