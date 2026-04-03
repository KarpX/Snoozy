package com.wem.snoozy.presentation.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wem.snoozy.data.local.UserPreferencesManager
import com.wem.snoozy.presentation.navigation.Screen
import com.wem.snoozy.presentation.screen.LoginScreen
import com.wem.snoozy.presentation.screen.RegistrationScreen
import com.wem.snoozy.presentation.viewModel.SettingsViewModel
import com.wem.snoozy.ui.theme.SnoozyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val themeState by settingsViewModel.themeState.collectAsState()
            val context = LocalContext.current

            // Проверка наличия токена при запуске
            LaunchedEffect(Unit) {
                val userPrefs = UserPreferencesManager(context)
                if (userPrefs.hasToken()) {
                    navigateToMain()
                }
            }

            SnoozyTheme(darkTheme = themeState) {
                val window = (LocalView.current.context as Activity).window
                SideEffect {
                    val controller = WindowInsetsControllerCompat(window, window.decorView)
                    controller.isAppearanceLightStatusBars = !themeState
                }

                val authNavController = rememberNavController()
                NavHost(
                    navController = authNavController,
                    startDestination = Screen.Login.route,
                    enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
                    exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }
                ) {
                    composable(Screen.Login.route) {
                        LoginScreen(
                            onLoginSuccess = { navigateToMain() },
                            onRegisterClick = { authNavController.navigate(Screen.Registration.route) }
                        )
                    }
                    composable(Screen.Registration.route) {
                        RegistrationScreen(
                            onBackClick = { authNavController.popBackStack() },
                            onRegistrationSuccess = { navigateToMain() }
                        )
                    }
                }
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
