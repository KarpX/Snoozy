package com.wem.snoozy.data.remote

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import com.wem.snoozy.data.dto.AlarmActionDto
import com.wem.snoozy.data.local.UserPreferencesManager
import com.wem.snoozy.data.receiver.AlarmReceiver
import com.wem.snoozy.data.alarm.AlarmService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmSocketManager @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val userPreferencesManager: UserPreferencesManager,
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun connect() {
        scope.launch {
            val token = userPreferencesManager.accessTokenFlow.first() ?: return@launch
            val request = Request.Builder()
                .url("ws://45.156.22.247:8081/ws/alarms") // Предполагаемый эндпоинт для WS
                .addHeader("Authorization", "Bearer $token")
                .build()

            webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
                override fun onMessage(webSocket: WebSocket, text: String) {
                    try {
                        val action = gson.fromJson(text, AlarmActionDto::class.java)
                        if (action.actionType == "TRIGGER_NOW") {
                            triggerLocalAlarm(action.alarmId.toInt())
                        }
                    } catch (e: Exception) {
                        Log.e("SocketManager", "Error parsing message", e)
                    }
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d("SocketManager", "Closing: $reason")
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e("SocketManager", "Failure: ${t.message}")
                    // Реконнект через некоторое время
                    scope.launch {
                        kotlinx.coroutines.delay(5000)
                        connect()
                    }
                }
            })
        }
    }

    private fun triggerLocalAlarm(alarmId: Int) {
        val intent = Intent(context, AlarmService::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "App closed")
    }
}
