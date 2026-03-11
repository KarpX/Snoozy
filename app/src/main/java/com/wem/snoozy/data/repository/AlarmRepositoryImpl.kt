package com.wem.snoozy.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.wem.snoozy.SnoozyApp
import com.wem.snoozy.data.local.AlarmDatabase
import com.wem.snoozy.data.local.Dao
import com.wem.snoozy.data.mapper.toAlarmItemModel
import com.wem.snoozy.data.mapper.toAlarmItemsFlow
import com.wem.snoozy.domain.entity.AlarmItem
import com.wem.snoozy.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    private val dao: Dao
) : AlarmRepository {

    override suspend fun addNewAlarm(alarmItem: AlarmItem) {
        dao.addAlarm(alarmItem.toAlarmItemModel())
    }

    override suspend fun editAlarm(alarmItem: AlarmItem) {
        dao.addAlarm(alarmItem.toAlarmItemModel())
    }

    override fun getAllAlarms(): Flow<List<AlarmItem>> {
        return dao.getAlarms().toAlarmItemsFlow()
    }

    override suspend fun toggleAlarmState(alarmItem: AlarmItem) {
        dao.updateCheckedStatus(alarmItem.id, !alarmItem.checked)
    }

    override suspend fun deleteAlarm(alarmId: Int) {
        dao.deleteAlarm(alarmId)
    }
}