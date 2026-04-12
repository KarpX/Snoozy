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
        if (!alarmItem.enabled) {
            cancelAlarm(alarmItem.id)
            cancelBedtimeNotification(alarmItem.id)
            return
        }

        scheduleAlarm(alarmItem)
        scheduleBedtimeNotification(alarmItem)
    }

    private fun scheduleAlarm(alarmItem: AlarmItem) {
        try {
            val alarmTime = LocalTime.parse(alarmItem.ringHours, DateTimeFormatter.ofPattern("H:mm"))
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
        if (alarmItem.timeToBed.isEmpty() || !alarmItem.enabled) {
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
                -alarmItem.id, // Use negative ID to distinguish from alarm
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerAt = scheduleTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
            
            Log.d("AlarmScheduler", "Scheduled Bedtime Notification for ${alarmItem.id} at $scheduleTime")
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Error scheduling bedtime", e)
        }
    }

    fun scheduleWakeupCheck(alarmId: Int) {
        val triggerAt = System.currentTimeMillis() + 5 * 60 * 1000 // 5 minutes
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_CHECK_WAKEUP
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarmId + WAKEUP_CHECK_OFFSET, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExactAlarm(triggerAt, pendingIntent)
        Log.d("AlarmScheduler", "Scheduled WakeupCheck for $alarmId in 5 minutes")
    }

    fun scheduleWakeupExpiry(alarmId: Int) {
        val triggerAt = System.currentTimeMillis() + 60 * 1000 // 1 minute
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_EXPIRE_WAKEUP
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarmId + WAKEUP_EXPIRY_OFFSET, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        setExactAlarm(triggerAt, pendingIntent)
        Log.d("AlarmScheduler", "Scheduled WakeupExpiry for $alarmId in 1 minute")
    }

    fun cancelWakeupExpiry(alarmId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_EXPIRE_WAKEUP
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, alarmId + WAKEUP_EXPIRY_OFFSET, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun setExactAlarm(triggerAt: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancelAlarm(alarmId: Int) {
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
    }

    fun cancelBedtimeNotification(alarmId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            -alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun getNextOccurrence(current: LocalDateTime, repeatDays: String): LocalDateTime {
        val days = repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }.sorted()
        if (days.isEmpty()) return current.plusDays(1)

        val currentDayOfWeek = current.dayOfWeek.value // 1 (Mon) to 7 (Sun)
        
        // Find the next day in the list
        val nextDay = days.firstOrNull { it > currentDayOfWeek } ?: days.first()
            
        val daysToAdd = if (nextDay > currentDayOfWeek) {
            nextDay - currentDayOfWeek
        } else {
            7 - currentDayOfWeek + nextDay
        }
        
        return current.plusDays(daysToAdd.toLong())
    }

    companion object {
        private const val WAKEUP_CHECK_OFFSET = 3000
        private const val WAKEUP_EXPIRY_OFFSET = 4000
    }
}
