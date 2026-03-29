package com.wem.snoozy.data.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.wem.snoozy.data.alarm.AlarmService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra(EXTRA_ALARM_ID, -1)
        val action = intent.action
        val type = intent.getStringExtra(EXTRA_TYPE)
        
        Log.d("AlarmReceiver", "onReceive: action=$action, alarmId=$alarmId, type=$type")

        if (action == ACTION_DISMISS_ALARM) {
            val serviceIntent = Intent(context, AlarmService::class.java)
            context.stopService(serviceIntent)
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
        } else if (action == ACTION_BEDTIME || type == TYPE_BEDTIME) {
            showBedtimeNotification(context)
        }
    }

    private fun showBedtimeNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "bedtime_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Bedtime Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications to remind you to go to bed"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Пора спать!")
            .setContentText("Чтобы выспаться, вам пора ложиться в постель.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
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

        const val NOTIFICATION_ID = 1001
    }
}
