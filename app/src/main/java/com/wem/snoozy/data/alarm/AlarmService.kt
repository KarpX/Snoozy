package com.wem.snoozy.data.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.wem.snoozy.data.receiver.AlarmReceiver
import com.wem.snoozy.presentation.activity.AlarmActivity

class AlarmService : Service() {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("AlarmService", "onCreate")
        val ringtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneUri)
        
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getIntExtra(AlarmReceiver.EXTRA_ALARM_ID, -1) ?: -1
        Log.d("AlarmService", "onStartCommand: alarmId=$alarmId")

        createNotificationChannel()

        // 1. Создаем интент для запуска Активити
        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            action = "com.wem.snoozy.ALARM_DISPLAY"
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 
            alarmId, 
            fullScreenIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Кнопка "Выключить" в уведомлении
        val dismissIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_DISMISS_ALARM
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this, alarmId + 1000, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Строим уведомление (ЭТО И ЕСТЬ ВЫЗОВ АКТИВИТИ)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Будильник")
            .setContentText("Пора просыпаться!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true) // Ключевая строка
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Выключить", dismissPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            // НЕ ставим setSilent(true), иначе fullScreenIntent может не сработать!
            .build()

        // 4. Запускаем сервис как Foreground
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e("AlarmService", "Error startForeground", e)
        }

        // 5. Запускаем звук и вибрацию
        ringtone?.let {
            it.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            if (!it.isPlaying) it.play()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 500), 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 500, 500), 0)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("AlarmService", "onDestroy")
        ringtone?.stop()
        vibrator?.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            // Используем v4 для гарантии сброса настроек в системе
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alarms"
                setSound(null, null) 
                enableVibration(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "alarm_service_channel_v4"
        const val NOTIFICATION_ID = 1002
    }
}
