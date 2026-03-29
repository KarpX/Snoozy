package com.wem.snoozy.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.wem.snoozy.data.alarm.AlarmScheduler
import com.wem.snoozy.data.alarm.AlarmService
import com.wem.snoozy.data.local.Dao
import com.wem.snoozy.data.mapper.toAlarmItem
import com.wem.snoozy.data.mapper.toAlarmItemModel
import com.wem.snoozy.presentation.utils.formatStringToDate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dao: Dao

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(EXTRA_ALARM_ID, -1)
        val action = intent.action
        val type = intent.getStringExtra(EXTRA_TYPE)
        
        Log.d("AlarmReceiver", "onReceive: action=$action, alarmId=$alarmId, type=$type")

        if (action == ACTION_DISMISS_ALARM) {
            val serviceIntent = Intent(context, AlarmService::class.java)
            context.stopService(serviceIntent)
            
            if (alarmId != -1) {
                updateAlarmAfterRing(alarmId)
            }
            return
        }

        if (action == ACTION_ALARM || type == TYPE_ALARM) {
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra(EXTRA_ALARM_ID, alarmId)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    private fun updateAlarmAfterRing(alarmId: Int) {
        scope.launch {
            val alarmModel = dao.getAlarmById(alarmId) ?: return@launch
            val alarmItem = alarmModel.toAlarmItem()

            if (alarmItem.repeatDays.isEmpty()) {
                // Разовый будильник — просто выключаем
                dao.updateCheckedStatus(alarmId, false)
            } else {
                // Повторяющийся — вычисляем следующую дату
                val alarmTime = LocalTime.parse(alarmItem.ringHours, DateTimeFormatter.ofPattern("HH:mm"))
                val currentRingDate = alarmItem.ringDay.formatStringToDate()
                val currentDateTime = LocalDateTime.of(currentRingDate, alarmTime)
                
                val nextDateTime = getNextOccurrence(currentDateTime, alarmItem.repeatDays)
                
                val nextAlarmItem = alarmItem.copy(
                    ringDay = formatDateForDisplay(nextDateTime.toLocalDate())
                )
                
                // КРИТИЧЕСКИ ВАЖНО: сохраняем новую дату в БД
                dao.addAlarm(nextAlarmItem.toAlarmItemModel())
                
                // И планируем следующий звонок
                alarmScheduler.schedule(nextAlarmItem)
            }
        }
    }

    private fun getNextOccurrence(startDateTime: LocalDateTime, repeatDays: String): LocalDateTime {
        val days = repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }.sorted()
        if (days.isEmpty()) return startDateTime.plusDays(1)

        val currentDayOfWeek = startDateTime.dayOfWeek.value
        val nextDay = days.firstOrNull { it > currentDayOfWeek } ?: days.first()

        val daysToAdd = if (nextDay > currentDayOfWeek) {
            nextDay - currentDayOfWeek
        } else {
            7 - currentDayOfWeek + nextDay
        }
        
        return startDateTime.plusDays(daysToAdd.toLong())
    }

    private fun formatDateForDisplay(date: java.time.LocalDate): String {
        val today = java.time.LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy").withLocale(Locale.ENGLISH)
        return when (date) {
            today -> "Сегодня"
            today.plusDays(1) -> "Завтра"
            today.plusDays(2) -> "Послезавтра"
            else -> date.format(formatter)
        }
    }

    companion object {
        const val EXTRA_TYPE = "extra_type"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val TYPE_BEDTIME = "type_bedtime"
        const val TYPE_ALARM = "type_alarm"
        
        const val ACTION_ALARM = "com.wem.snoozy.ALARM_ACTION"
        const val ACTION_DISMISS_ALARM = "com.wem.snoozy.DISMISS_ALARM"
    }
}
