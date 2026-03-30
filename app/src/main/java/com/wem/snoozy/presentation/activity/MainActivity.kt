package com.wem.snoozy.presentation.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.wem.snoozy.data.local.UserPreferencesManager
import com.wem.snoozy.presentation.navigation.AppNavGraph
import com.wem.snoozy.presentation.navigation.BottomBarTabs
import com.wem.snoozy.presentation.viewModel.SettingsViewModel
import com.wem.snoozy.ui.theme.SnoozyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var userPreferencesManager: UserPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        userPreferencesManager = UserPreferencesManager(this)

        setContent {
            val context = LocalContext.current
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeState by settingsViewModel.themeState.collectAsState()

            var showBatteryDialog by remember { mutableStateOf(false) }

            // Запрос разрешения на уведомления для Android 13+
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { _ -> }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                
                // Проверка оптимизации батареи
                val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
                    showBatteryDialog = true
                }
            }

            if (showBatteryDialog) {
                AlertDialog(
                    onDismissRequest = { showBatteryDialog = false },
                    title = { Text("Внимание") },
                    text = { Text("Для корректной работы будильника в фоновом режиме необходимо отключить оптимизацию заряда батареи для этого приложения.") },
                    confirmButton = {
                        TextButton(onClick = {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                            showBatteryDialog = false
                        }) {
                            Text("Настроить")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showBatteryDialog = false }) {
                            Text("Отмена")
                        }
                    }
                )
            }

            SnoozyTheme(darkTheme = themeState) {
                val window = (LocalView.current.context as Activity).window
                SideEffect {
                    val controller = WindowInsetsControllerCompat(window, window.decorView)
                    controller.isAppearanceLightStatusBars = !themeState
                }

                val navController = rememberNavController()

                Scaffold(
                    bottomBar = {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 60.dp)
                                .fillMaxWidth()
                                .height(64.dp)
                        ) {
                            BottomBarTabs(navController)
                        }
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        AppNavGraph(navController)
                    }
                }
            }
        }
    }
}
