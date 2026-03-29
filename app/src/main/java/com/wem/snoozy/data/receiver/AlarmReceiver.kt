package com.wem.snoozy.data.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.wem.snoozy.R

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(EXTRA_TYPE)
        if (type == TYPE_BEDTIME) {
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
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Replace with app icon if available
            .setContentTitle("Пора спать!")
            .setContentText("Чтобы выспаться, вам пора ложиться в постель.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val EXTRA_TYPE = "extra_type"
        const val TYPE_BEDTIME = "type_bedtime"
        const val NOTIFICATION_ID = 1001
    }
}
