package com.wem.snoozy.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.wem.snoozy.data.alarm.AlarmScheduler
import com.wem.snoozy.data.dto.AlarmDto
import com.wem.snoozy.data.dto.CreateAlarmRequest
import com.wem.snoozy.data.dto.UpdateAlarmRequest
import com.wem.snoozy.data.dto.GrantPermissionRequest
import com.wem.snoozy.data.local.AlarmItemModel
import com.wem.snoozy.data.local.Dao
import com.wem.snoozy.data.mapper.toAlarmItem
import com.wem.snoozy.data.mapper.toAlarmItemModel
import com.wem.snoozy.data.mapper.toAlarmItemsFlow
import com.wem.snoozy.data.remote.ApiService
import com.wem.snoozy.domain.entity.AlarmItem
import com.wem.snoozy.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(
    private val dao: Dao,
    private val alarmScheduler: AlarmScheduler,
    private val apiService: ApiService
) : AlarmRepository {

    override suspend fun addNewAlarm(alarmItem: AlarmItem) {
        val remoteId = try {
            val response = apiService.createAlarm(alarmItem.toCreateAlarmRequest())
            if (response.isSuccessful) {
                response.body()?.id
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

        val alarmModel = alarmItem.toAlarmItemModel().copy(remoteId = remoteId)
        val id = dao.addAlarm(alarmModel).toInt()
        val savedAlarm = alarmModel.toAlarmItem().copy(id = id)
        if (savedAlarm.enabled) {
            alarmScheduler.schedule(savedAlarm)
        }
    }

    override suspend fun editAlarm(alarmItem: AlarmItem) {
        var remoteId = alarmItem.remoteId
        try {
            if (remoteId != null) {
                val response = apiService.updateAlarm(remoteId, alarmItem.toUpdateAlarmRequest())
                if (!response.isSuccessful) {
                    Log.e("AlarmRepo", "Failed to update remote alarm: ${response.code()}")
                }
            } else {
                val response = apiService.createAlarm(alarmItem.toCreateAlarmRequest())
                if (response.isSuccessful) {
                    remoteId = response.body()?.id
                }
            }
        } catch (e: Exception) {
            Log.e("AlarmRepo", "Error syncing edit to remote", e)
        }

        val updatedModel = alarmItem.toAlarmItemModel().copy(remoteId = remoteId)
        dao.addAlarm(updatedModel)
        
        alarmScheduler.cancelAlarm(alarmItem.id)
        alarmScheduler.cancelBedtimeNotification(alarmItem.id)
        if (alarmItem.enabled) {
            alarmScheduler.schedule(alarmItem)
        }
    }

    override fun getAllAlarms(): Flow<List<AlarmItem>> {
        return dao.getAlarms().toAlarmItemsFlow()
    }

    override suspend fun toggleAlarmState(alarmItem: AlarmItem) {
        val newEnabledState = !alarmItem.enabled
        val updatedAlarm = alarmItem.copy(enabled = newEnabledState)
        
        val remoteId = alarmItem.remoteId
        if (remoteId != null) {
            try {
                // Используем именно метод обновления (PATCH), как вы просили
                apiService.updateAlarm(remoteId, updatedAlarm.toUpdateAlarmRequest())
            } catch (e: Exception) {
                Log.e("AlarmRepo", "Failed to toggle remote alarm state via update", e)
            }
        }

        dao.updateEnabledStatus(alarmItem.id, newEnabledState)
        if (newEnabledState) {
            alarmScheduler.schedule(updatedAlarm)
        } else {
            alarmScheduler.cancelAlarm(updatedAlarm.id)
            alarmScheduler.cancelBedtimeNotification(updatedAlarm.id)
        }
    }

    override suspend fun deleteAlarm(alarmId: Int) {
        val alarmModel = dao.getAlarmById(alarmId)
        val remoteId = alarmModel?.remoteId
        if (remoteId != null) {
            try {
                apiService.deleteAlarm(remoteId)
            } catch (e: Exception) {
                Log.e("AlarmRepo", "Failed to delete remote alarm", e)
            }
        }

        dao.deleteAlarm(alarmId)
        alarmScheduler.cancelAlarm(alarmId)
        alarmScheduler.cancelBedtimeNotification(alarmId)
    }

    override suspend fun syncRemoteAlarms() {
        try {
            val unsyncedAlarms = dao.getAlarmsToSync()
            unsyncedAlarms.forEach { model ->
                try {
                    val response = apiService.createAlarm(model.toAlarmItem().toCreateAlarmRequest())
                    if (response.isSuccessful) {
                        response.body()?.id?.let { remoteId ->
                            dao.addAlarm(model.copy(remoteId = remoteId))
                        }
                    }
                } catch (e: Exception) { /* skip */ }
            }

            val response = apiService.getMyAlarms()
            if (response.isSuccessful) {
                val remoteAlarms = response.body() ?: emptyList()
                
                val localAlarms = dao.getAlarmsOnce()
                val remoteIds = remoteAlarms.map { it.id }
                
                localAlarms.filter { it.remoteId != null && it.remoteId !in remoteIds }.forEach {
                    dao.deleteAlarm(it.id)
                    alarmScheduler.cancelAlarm(it.id)
                }

                remoteAlarms.forEach { alarmDto ->
                    val existingModel = dao.getAlarmByRemoteId(alarmDto.id)
                    val newModel = alarmDto.toAlarmItemModel()
                    if (existingModel != null) {
                        dao.addAlarm(newModel.copy(id = existingModel.id))
                        
                        val updatedAlarm = newModel.copy(id = existingModel.id).toAlarmItem()
                        if (updatedAlarm.enabled) {
                            alarmScheduler.schedule(updatedAlarm)
                        } else {
                            alarmScheduler.cancelAlarm(updatedAlarm.id)
                        }
                    } else {
                        val id = dao.addAlarm(newModel).toInt()
                        val newAlarm = newModel.toAlarmItem().copy(id = id)
                        if (newAlarm.enabled) {
                            alarmScheduler.schedule(newAlarm)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AlarmRepo", "Sync failed", e)
        }
    }

    override suspend fun updateOversleptStatus(alarmId: Int, isOverslept: Boolean) {
        dao.updateOversleptStatus(alarmId, isOverslept)
        val alarmModel = dao.getAlarmById(alarmId)
        val remoteId = alarmModel?.remoteId
        if (remoteId != null) {
            try {
                val alarmItem = alarmModel.toAlarmItem()
                apiService.updateAlarm(remoteId, alarmItem.copy(isOverslept = isOverslept).toUpdateAlarmRequest())
            } catch (e: Exception) {
                Log.e("AlarmRepo", "Failed to sync overslept status", e)
            }
        }
    }

    override suspend fun updateAlarmAfterRing(alarmId: Int) {
        val alarmModel = dao.getAlarmById(alarmId) ?: return
        val alarmItem = alarmModel.toAlarmItem()

        updateOversleptStatus(alarmId, false)

        if (alarmItem.repeatDays.isEmpty()) {
            toggleAlarmState(alarmItem) 
        } else {
            val alarmTime = LocalTime.parse(alarmItem.ringHours, DateTimeFormatter.ofPattern("H:mm"))
            val currentRingDate = try {
                alarmItem.ringDay.toLocalDateFromRelative()
            } catch (e: Exception) {
                LocalDate.now()
            }
            val currentDateTime = LocalDateTime.of(currentRingDate, alarmTime)
            
            val nextDateTime = getNextOccurrence(currentDateTime, alarmItem.repeatDays)
            
            val nextAlarmItem = alarmItem.copy(
                ringDay = formatDateForDisplay(nextDateTime.toLocalDate())
            )

            val updatedModel = nextAlarmItem.toAlarmItemModel()
            dao.addAlarm(updatedModel)
            
            val remoteId = alarmItem.remoteId
            if (remoteId != null) {
                try {
                    apiService.updateAlarm(remoteId, nextAlarmItem.toUpdateAlarmRequest())
                } catch (e: Exception) {
                    Log.e("AlarmRepo", "Failed to sync next ring date", e)
                }
            }
            
            alarmScheduler.schedule(nextAlarmItem)
        }
    }

    override suspend fun grantPermission(targetUserId: Long, permissionType: String): Boolean {
        return try {
            val response = apiService.grantPermission(GrantPermissionRequest(targetUserId, permissionType))
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("AlarmRepo", "Failed to grant permission to user $targetUserId", e)
            false
        }
    }

    private fun AlarmItem.toCreateAlarmRequest(): CreateAlarmRequest {
        return CreateAlarmRequest(
            title = "Будильник ${this.ringHours} ${this.ringDay}",
            alarmTime = this.toIsoDateTime(),
            enabled = this.enabled,
            repeatDays = this.repeatDays.toApiRepeatDays(),
            soundName = "classic",
            difficultyLevel = 1
        )
    }

    private fun AlarmItem.toUpdateAlarmRequest(): UpdateAlarmRequest {
        return UpdateAlarmRequest(
            title = "Будильник ${this.ringHours} ${this.ringDay}",
            alarmTime = this.toIsoDateTime(),
            enabled = this.enabled,
            repeatDays = this.repeatDays.toApiRepeatDays(),
            soundName = "classic",
            difficultyLevel = 1,
            isOverslept = this.isOverslept
        )
    }

    private fun AlarmItem.toIsoDateTime(): String {
        return try {
            val date = this.ringDay.toLocalDateFromRelative()
            val time = LocalTime.parse(this.ringHours, DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH))
            LocalDateTime.of(date, time).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        }
    }

    private fun String.toApiRepeatDays(): List<String> {
        if (this.isEmpty()) return emptyList()
        return this.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .mapNotNull { id ->
                when (id) {
                    1 -> "MON"
                    2 -> "TUE"
                    3 -> "WED"
                    4 -> "THU"
                    5 -> "FRI"
                    6 -> "SAT"
                    7 -> "SUN"
                    else -> null
                }
            }
    }

    private fun String.toLocalDateFromRelative(): LocalDate {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ENGLISH)

        return when (this) {
            "Сегодня" -> today
            "Завтра" -> today.plusDays(1)
            "Послезавтра" -> today.plusDays(2)
            else -> try { LocalDate.parse(this, formatter) } catch (e: Exception) { today }
        }
    }

    private fun AlarmDto.toAlarmItemModel(): AlarmItemModel {
        val dateTime = try {
            LocalDateTime.parse(this.alarmTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
        val ringDay = dateTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ENGLISH))
        val ringHours = dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH))
        return AlarmItemModel(
            id = 0,
            ringDay = ringDay,
            ringHours = ringHours,
            ringHoursMillis = timeToMilli(ringHours),
            timeToBed = "",
            enabled = this.enabled,
            repeatDays = this.repeatDays.toLocalRepeatDays(),
            remoteId = this.id,
            isOverslept = this.overslept
        )
    }

    private fun List<String>.toLocalRepeatDays(): String {
        return this.mapNotNull { day ->
            when (day) {
                "MON" -> 1
                "TUE" -> 2
                "WED" -> 3
                "THU" -> 4
                "FRI" -> 5
                "SAT" -> 6
                "SUN" -> 7
                else -> null
            }
        }.joinToString(",")
    }

    private fun timeToMilli(ringHours: String): Int {
        return try {
            val parts = ringHours.split(":")
            val hours = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val minutes = parts.getOrNull(1)?.toIntOrNull() ?: 0
            hours * 60 + minutes
        } catch (e: Exception) {
            0
        }
    }

    private fun getNextOccurrence(current: LocalDateTime, repeatDays: String): LocalDateTime {
        val days = repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }.sorted()
        if (days.isEmpty()) return current.plusDays(1)

        val currentDayOfWeek = current.dayOfWeek.value
        val nextDay = days.firstOrNull { it > currentDayOfWeek } ?: days.first()

        val daysToAdd = if (nextDay > currentDayOfWeek) {
            nextDay - currentDayOfWeek
        } else {
            7 - currentDayOfWeek + nextDay
        }
        
        return current.plusDays(daysToAdd.toLong())
    }

    private fun formatDateForDisplay(date: LocalDate): String {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy").withLocale(Locale.ENGLISH)
        return when (date) {
            today -> "Сегодня"
            today.plusDays(1) -> "Завтра"
            today.plusDays(2) -> "Послезавтра"
            else -> date.format(formatter)
        }
    }
}
