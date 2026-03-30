package com.wem.snoozy.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.wem.snoozy.presentation.screen.BottomSheetContentAdd
import com.wem.snoozy.presentation.screen.GroupsScreen
import com.wem.snoozy.presentation.screen.MainScreen
import com.wem.snoozy.presentation.screen.ProfileScreen
import com.wem.snoozy.presentation.screen.SettingsScreen

/**
 * Application NavGraph to navigate user
 *
 * @param navController Controller for navigation
 */
@Composable
fun AppNavGraph(
    navController: NavHostController
) {

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            fadeIn(animationSpec = tween(durationMillis = 0))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(durationMillis = 0))
        }
    ) {

        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        composable(Screen.Home.route) {
            MainScreen()
        }
        composable(Screen.Groups.route) {
            GroupsScreen()
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
        }
        composable(Screen.AddAlarm.route) {
            BottomSheetContentAdd {  }
        }
    }

}