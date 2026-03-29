package com.wem.snoozy.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.wem.snoozy.data.receiver.AlarmReceiver
import com.wem.snoozy.domain.entity.AlarmItem
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

    fun scheduleBedtimeNotification(alarmItem: AlarmItem) {
        if (alarmItem.timeToBed.isEmpty() || !alarmItem.checked) {
            cancelBedtimeNotification(alarmItem.id)
            return
        }

        try {
            val bedtime = LocalTime.parse(alarmItem.timeToBed, DateTimeFormatter.ofPattern("H:mm"))
            val now = LocalDateTime.now()
            var scheduleTime = LocalDateTime.of(LocalDate.now(), bedtime)

            if (scheduleTime.isBefore(now)) {
                scheduleTime = scheduleTime.plusDays(1)
            }

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra(AlarmReceiver.EXTRA_TYPE, AlarmReceiver.TYPE_BEDTIME)
                putExtra("ALARM_ID", alarmItem.id)
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
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
            }
            Log.d("AlarmScheduler", "Scheduled bedtime for alarm ${alarmItem.id} at $scheduleTime")
        } catch (e: Exception) {
            Log.e("AlarmScheduler", "Error scheduling bedtime", e)
        }
    }

    fun cancelBedtimeNotification(alarmId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId + BEDTIME_OFFSET,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    companion object {
        private const val BEDTIME_OFFSET = 10000 // To avoid ID collisions with main alarms
    }
}
