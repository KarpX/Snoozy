package com.wem.snoozy.data.repository

import com.wem.snoozy.data.alarm.AlarmScheduler
import com.wem.snoozy.data.dto.AlarmDto
import com.wem.snoozy.data.dto.CreateAlarmRequest
import com.wem.snoozy.data.dto.TriggerRequest
import com.wem.snoozy.data.dto.UpdateAlarmRequest
import com.wem.snoozy.data.local.AlarmItemModel
import com.wem.snoozy.data.local.Dao
import com.wem.snoozy.data.mapper.toAlarmItem
import com.wem.snoozy.data.mapper.toAlarmItemModel
import com.wem.snoozy.data.mapper.toAlarmItemsFlow
import com.wem.snoozy.domain.entity.AlarmItem
import com.wem.snoozy.data.remote.ApiService
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
        if (savedAlarm.checked) {
            alarmScheduler.schedule(savedAlarm)
        }
    }

    override suspend fun editAlarm(alarmItem: AlarmItem) {
        var remoteId = alarmItem.remoteId
        try {
            if (remoteId != null) {
                apiService.updateAlarm(remoteId, alarmItem.toUpdateAlarmRequest())
            } else {
                val response = apiService.createAlarm(alarmItem.toCreateAlarmRequest())
                if (response.isSuccessful) {
                    remoteId = response.body()?.id
                }
            }
        } catch (e: Exception) {
            // Ignore remote sync failure and keep local update
        }

        val updatedModel = alarmItem.toAlarmItemModel().copy(remoteId = remoteId)
        dao.addAlarm(updatedModel)
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
        val alarmModel = dao.getAlarmById(alarmId)
        try {
            val remoteId = alarmModel?.remoteId
            if (remoteId != null) {
                apiService.deleteAlarm(remoteId)
            }
        } catch (e: Exception) {
            // If remote delete fails, still remove locally to keep user experience consistent.
        }

        dao.deleteAlarm(alarmId)
        alarmScheduler.cancelAlarm(alarmId)
        alarmScheduler.cancelBedtimeNotification(alarmId)
    }

    override suspend fun syncRemoteAlarms() {
        try {
            val response = apiService.getMyAlarms()
            if (response.isSuccessful) {
                response.body()?.forEach { alarmDto ->
                    val remoteModel = alarmDto.toAlarmItemModel()
                    val existingModel = dao.getAlarmByRemoteId(alarmDto.id)
                    if (existingModel != null) {
                        dao.addAlarm(remoteModel.copy(id = existingModel.id))
                    } else {
                        dao.addAlarm(remoteModel)
                    }
                }
            }
        } catch (e: Exception) {
            // ignore sync errors
        }
    }

    private fun AlarmItem.toCreateAlarmRequest(): CreateAlarmRequest {
        return CreateAlarmRequest(
            title = "Будильник ${this.ringHours} ${this.ringDay}",
            alarmTime = this.toIsoDateTime(),
            enabled = this.checked,
            repeatDays = this.repeatDays.toApiRepeatDays(),
            soundName = "classic",
            difficultyLevel = 1
        )
    }

    private fun AlarmItem.toUpdateAlarmRequest(): UpdateAlarmRequest {
        return UpdateAlarmRequest(
            title = "Будильник ${this.ringHours} ${this.ringDay}",
            alarmTime = this.toIsoDateTime(),
            enabled = this.checked,
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
            else -> LocalDate.parse(this, formatter)
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
            checked = this.enabled,
            repeatDays = this.repeatDays.toLocalRepeatDays(),
            remoteId = this.id,
            isOverslept = this.isOverslept
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
}
