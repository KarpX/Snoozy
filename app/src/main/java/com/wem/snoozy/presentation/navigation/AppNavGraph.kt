package com.wem.snoozy.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.wem.snoozy.presentation.screen.AddMembersScreen
import com.wem.snoozy.presentation.screen.BottomSheetContentAdd
import com.wem.snoozy.presentation.screen.GroupsScreen
import com.wem.snoozy.presentation.screen.MainScreen
import com.wem.snoozy.presentation.screen.NewGroupScreen
import com.wem.snoozy.presentation.screen.ProfileScreen
import com.wem.snoozy.presentation.screen.SettingsScreen
import com.wem.snoozy.presentation.viewModel.AddMembersViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = { fadeIn(animationSpec = tween(durationMillis = 0)) },
        exitTransition = { fadeOut(animationSpec = tween(durationMillis = 0)) }
    ) {
        composable(Screen.Settings.route) { SettingsScreen() }
        composable(Screen.Home.route) { MainScreen() }
        
        navigation(
            startDestination = Screen.Groups.route,
            route = "groups_flow"
        ) {
            composable(Screen.Groups.route) {
                GroupsScreen(onAddGroupClick = { navController.navigate(Screen.AddMembers.route) })
            }
            
            composable(Screen.AddMembers.route) { entry ->
                val parentEntry = remember(entry) { navController.getBackStackEntry("groups_flow") }
                val viewModel: AddMembersViewModel = hiltViewModel(parentEntry)
                
                AddMembersScreen(
                    onBackClick = { navController.popBackStack() },
                    onNextClick = { navController.navigate(Screen.NewGroup.route) },
                    viewModel = viewModel
                )
            }
            
            composable(Screen.NewGroup.route) { entry ->
                val parentEntry = remember(entry) { navController.getBackStackEntry("groups_flow") }
                val viewModel: AddMembersViewModel = hiltViewModel(parentEntry)
                
                NewGroupScreen(
                    onBackClick = { 
                        // Возвращаемся сразу к списку групп
                        navController.popBackStack(Screen.Groups.route, inclusive = false) 
                    },
                    viewModel = viewModel
                )
            }
        }

        composable(Screen.Profile.route) { ProfileScreen() }
        composable(Screen.AddAlarm.route) { BottomSheetContentAdd { } }
    }
}
