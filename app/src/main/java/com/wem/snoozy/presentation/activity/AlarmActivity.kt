package com.wem.snoozy.presentation.activity

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wem.snoozy.data.receiver.AlarmReceiver
import com.wem.snoozy.ui.theme.SnoozyTheme

class AlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val alarmId = intent.getIntExtra(AlarmReceiver.EXTRA_ALARM_ID, -1)

        turnScreenOnAndKeyguardOff()

        setContent {
            SnoozyTheme {
                AlarmScreen(
                    onDismiss = {
                        // Отправляем бродкаст для остановки сервиса и обновления даты в БД
                        val dismissIntent = Intent(this, AlarmReceiver::class.java).apply {
                            action = AlarmReceiver.ACTION_DISMISS_ALARM
                            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
                        }
                        sendBroadcast(dismissIntent)
                        finish()
                    }
                )
            }
        }
    }

    private fun turnScreenOnAndKeyguardOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null)
        }
    }
}

@Composable
fun AlarmScreen(onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Будильник!",
                fontSize = 32.sp,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(64.dp)
            ) {
                Text(text = "Выключить", fontSize = 20.sp)
            }
        }
    }
}
