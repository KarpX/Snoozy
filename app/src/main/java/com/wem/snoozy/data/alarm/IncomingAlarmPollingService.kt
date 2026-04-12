package com.wem.snoozy.data.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.wem.snoozy.R
import com.wem.snoozy.data.receiver.AlarmReceiver
import com.wem.snoozy.domain.repository.AlarmRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class IncomingAlarmPollingService : Service() {

    @Inject
    lateinit var alarmRepository: AlarmRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isPolling = false

    // Храним ID уже обработанных уведомлений, чтобы не звонить дважды
    private val processedActionIds = mutableSetOf<Long>()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        startPolling()
    }

    private fun startPolling() {
        if (isPolling) return
        isPolling = true
        serviceScope.launch {
            while (isActive) {
                try {
                    val actions = alarmRepository.getIncomingActions()
                    
                    // Фильтруем только новые действия, которые мы еще не видели
                    val newActions = actions.filter { action ->
                        action.id != null && !processedActionIds.contains(action.id)
                    }

                    newActions.forEach { action ->
                        if (action.actionType == "TRIGGER_NOW" && action.status == "EXECUTED") {
                            Log.d("PollingService", "New trigger for alarm: ${action.alarmId}")
                            
                            // Запоминаем ID, чтобы не обрабатывать его в следующем цикле
                            action.id?.let { processedActionIds.add(it) }
                            
                            triggerLocalAlarm(action.alarmId.toInt())
                        }
                    }
                    
                    // Чтобы set не рос бесконечно, можно очищать старые ID (опционально)
                    if (processedActionIds.size > 200) {
                        val toRemove = processedActionIds.take(100)
                        processedActionIds.removeAll(toRemove.toSet())
                    }

                } catch (e: Exception) {
                    Log.e("PollingService", "Error during polling", e)
                }
                delay(5000)
            }
        }
    }

    private fun triggerLocalAlarm(alarmId: Int) {
        val intent = Intent(this, AlarmService::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Snoozy Background Sync",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Snoozy работает в фоне")
            .setContentText("Проверка входящих будильников...")
            .setSmallIcon(R.drawable.ic_snoozy_logo)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isPolling = false
    }

    companion object {
        private const val CHANNEL_ID = "polling_service_channel"
        private const val NOTIFICATION_ID = 1003
    }
}
