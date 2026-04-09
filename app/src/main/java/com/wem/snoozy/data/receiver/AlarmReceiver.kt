package com.wem.snoozy.data.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.wem.snoozy.data.alarm.AlarmScheduler
import com.wem.snoozy.data.alarm.AlarmService
import com.wem.snoozy.data.local.Dao
import com.wem.snoozy.data.mapper.toAlarmItem
import com.wem.snoozy.data.mapper.toAlarmItemModel
import com.wem.snoozy.domain.repository.AlarmRepository
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
    lateinit var alarmRepository: AlarmRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(EXTRA_ALARM_ID, -1)
        val action = intent.action
        val type = intent.getStringExtra(EXTRA_TYPE)
        val ringHours = intent.getStringExtra("RING_HOURS")
        
        Log.d("AlarmReceiver", "onReceive: action=$action, alarmId=$alarmId, type=$type, ringHours=$ringHours")

        when (action) {
            ACTION_DISMISS_ALARM -> {
                val serviceIntent = Intent(context, AlarmService::class.java)
                context.stopService(serviceIntent)
                
                if (alarmId != -1) {
                    scope.launch {
                        alarmRepository.updateAlarmAfterRing(alarmId)
                        // Планируем проверку через 5 минут
                        alarmScheduler.scheduleWakeupCheck(alarmId)
                    }
                }
            }
            ACTION_CHECK_WAKEUP -> {
                if (alarmId != -1) {
                    showWakeupCheckNotification(context, alarmId)
                    // Планируем отметку "проспал" через 1 минуту, если не подтвердит
                    alarmScheduler.scheduleWakeupExpiry(alarmId)
                }
            }
            ACTION_CONFIRM_WAKEUP -> {
                if (alarmId != -1) {
                    cancelWakeupCheck(context, alarmId)
                    scope.launch {
                        alarmRepository.updateOversleptStatus(alarmId, false)
                    }
                }
            }
            ACTION_EXPIRE_WAKEUP -> {
                if (alarmId != -1) {
                    cancelWakeupCheck(context, alarmId)
                    scope.launch {
                        alarmRepository.updateOversleptStatus(alarmId, true)
                    }
                }
            }
            else -> {
                if (action == ACTION_ALARM || type == TYPE_ALARM) {
                    val serviceIntent = Intent(context, AlarmService::class.java).apply {
                        putExtra(EXTRA_ALARM_ID, alarmId)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                } else if (action == ACTION_BEDTIME || type == TYPE_BEDTIME) {
                    showBedtimeNotification(context, ringHours)
                }
            }
        }
    }

    private fun showWakeupCheckNotification(context: Context, alarmId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "wakeup_check_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Wakeup Check",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Check if user is awake"
                setSound(null, null)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val confirmIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_CONFIRM_WAKEUP
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        val confirmPendingIntent = PendingIntent.getBroadcast(
            context, alarmId + 2000, confirmIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Вы проснулись?")
            .setContentText("Нажмите, если вы не спите")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .addAction(android.R.drawable.checkbox_on_background, "Я не сплю", confirmPendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)
            .build()

        notificationManager.notify(WAKEUP_NOTIFICATION_ID_OFFSET + alarmId, notification)
    }

    private fun cancelWakeupCheck(context: Context, alarmId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(WAKEUP_NOTIFICATION_ID_OFFSET + alarmId)
        alarmScheduler.cancelWakeupExpiry(alarmId)
    }

    private fun showBedtimeNotification(context: Context, ringHours: String?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "bedtime_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Bedtime Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications to remind you to go to bed"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val contentText = if (ringHours != null) {
            "Чтобы выспаться к $ringHours, вам пора ложиться в постель."
        } else {
            "Чтобы выспаться, вам пора ложиться в постель."
        }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Пора спать!")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val EXTRA_TYPE = "extra_type"
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val TYPE_BEDTIME = "type_bedtime"
        const val TYPE_ALARM = "type_alarm"
        
        const val ACTION_ALARM = "com.wem.snoozy.ALARM_ACTION"
        const val ACTION_BEDTIME = "com.wem.snoozy.BEDTIME_ACTION"
        const val ACTION_DISMISS_ALARM = "com.wem.snoozy.DISMISS_ALARM"
        const val ACTION_CHECK_WAKEUP = "com.wem.snoozy.CHECK_WAKEUP"
        const val ACTION_CONFIRM_WAKEUP = "com.wem.snoozy.CONFIRM_WAKEUP"
        const val ACTION_EXPIRE_WAKEUP = "com.wem.snoozy.EXPIRE_WAKEUP"

        const val NOTIFICATION_ID = 1001
        const val WAKEUP_NOTIFICATION_ID_OFFSET = 2000
    }
}
