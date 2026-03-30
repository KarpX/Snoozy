package com.wem.snoozy.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

/** Navigation object
 *
 * @param screen Navigation object provide a unique screen
 * @param icon Navigation object's icon
 * @param relatedRoutes Routes that also belong to this tab section
 */
sealed class MyNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val relatedRoutes: List<String> = emptyList()
) {

    data object Home : MyNavItem(
        screen = Screen.Home,
        icon = Icons.Outlined.Alarm,
        relatedRoutes = listOf(Screen.AddAlarm.route)
    )

    data object Groups : MyNavItem(
        screen = Screen.Groups,
        icon = Icons.Outlined.Group,
        relatedRoutes = listOf(Screen.AddMembers.route, Screen.NewGroup.route)
    )

    data object Profile : MyNavItem(
        screen = Screen.Profile,
        icon = Icons.Outlined.AccountCircle
    )

    data object Settings : MyNavItem(
        screen = Screen.Settings,
        icon = Icons.Outlined.Settings
    )
}


@Composable
fun BottomBarTabs(
    navController: NavController
) {

    // list of all navigation objects
    val tabs = listOf(
        MyNavItem.Home,
        MyNavItem.Groups,
        MyNavItem.Profile,
        MyNavItem.Settings,
    )

    // navigation BackStack and current route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // bottom bar
    Row(
        modifier = Modifier
            .shadow(2.dp, RoundedCornerShape(30))
            .clip(RoundedCornerShape(30))
            .background(MaterialTheme.colorScheme.onSurface)
            .fillMaxSize(),
    ) {
        tabs.forEach { tab ->
            // Check if current route matches main screen or any related sub-screens
            val isSelected = currentRoute == tab.screen.route || tab.relatedRoutes.contains(currentRoute)

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(26))
                    .weight(1f)
                    .background(if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface)
                    .clickable {
                        navController.navigate(tab.screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = "tab",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
