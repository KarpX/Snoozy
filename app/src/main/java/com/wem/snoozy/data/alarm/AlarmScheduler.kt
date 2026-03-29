package com.wem.snoozy.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.wem.snoozy.data.receiver.AlarmReceiver
import com.wem.snoozy.domain.entity.AlarmItem
import com.wem.snoozy.presentation.activity.MainActivity
import com.wem.snoozy.presentation.utils.formatStringToDate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class AlarmScheduler @Inject constructor(
    private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarmItem: AlarmItem) {
        if (!alarmItem.checked) {
            cancelAlarm(alarmItem.id)
            cancelBedtimeNotification(alarmItem.id)
            return
        }

        scheduleAlarm(alarmItem)
        scheduleBedtimeNotification(alarmItem)
    }

    private fun scheduleAlarm(alarmItem: AlarmItem) {
        try {
            val alarmTime = LocalTime.parse(alarmItem.ringHours, DateTimeFormatter.ofPattern("HH:mm"))
            val ringDate = alarmItem.ringDay.formatStringToDate()
            
            var scheduleTime = LocalDateTime.of(ringDate, alarmTime)
            val now = LocalDateTime.now()

            if (scheduleTime.isBefore(now)) {
                if (alarmItem.repeatDays.isEmpty()) {
                    if (ringDate == LocalDate.now()) {
                        scheduleTime = scheduleTime.plusDays(1)
                    }
                } else {
                    scheduleTime = getNextOccurrence(scheduleTime, alarmItem.repeatDays)
                }
            }

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                action = AlarmReceiver.ACTION_ALARM
                putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmItem.id)
                putExtra(AlarmReceiver.EXTRA_TYPE, AlarmReceiver.TYPE_ALARM)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmItem.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerAt = scheduleTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val showIntent = Intent(context, MainActivity::class.java)
            val showPendingIntent = PendingIntent.getActivity(
                context, alarmItem.id, showIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAt, showPendingIntent)
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            
            Log.d("AlarmScheduler", "Scheduled AlarmClock for ${alarmItem.id} at $scheduleTime")
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Error scheduling alarm", e)
        }
    }

    fun scheduleBedtimeNotification(alarmItem: AlarmItem) {
        if (alarmItem.timeToBed.isEmpty() || !alarmItem.checked) {
            cancelBedtimeNotification(alarmItem.id)
            return
        }

        try {
            val bedtime = LocalTime.parse(alarmItem.timeToBed, DateTimeFormatter.ofPattern("H:mm"))
            val ringDate = alarmItem.ringDay.formatStringToDate()
            val now = LocalDateTime.now()
            var scheduleTime = LocalDateTime.of(ringDate, bedtime)

            if (scheduleTime.isBefore(now)) {
                scheduleTime = scheduleTime.plusDays(1)
            }

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra(AlarmReceiver.EXTRA_TYPE, AlarmReceiver.TYPE_BEDTIME)
                putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmItem.id)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmItem.id + BEDTIME_OFFSET,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerAt = scheduleTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Error scheduling bedtime", e)
        }
    }

    fun cancelAlarm(alarmId: Int) {
        // Чтобы иконка исчезла, нужно создать Intent с ТЕМ ЖЕ ACTION и ТЕМ ЖЕ REQUEST_CODE
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d("AlarmScheduler", "Canceled alarm $alarmId and removed from system")
    }

    fun cancelBedtimeNotification(alarmId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId + BEDTIME_OFFSET,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
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

    companion object {
        private const val BEDTIME_OFFSET = 10000
    }
}
