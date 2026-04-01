package com.wem.snoozy.presentation.navigation

/**
 * Screen object
 *
 * @param route Screen's route for navigation
 */
sealed class Screen(
    val route: String
) {

    object Login: Screen(LOGIN_ROUTE)

    object Home: Screen(ROUTE_HOME)

    object Settings: Screen(ROUTE_SETTINGS)
    object Groups: Screen(ROUTE_GROUPS)
    object Profile: Screen(ROUTE_PROFILE)

    object AddAlarm: Screen(ROUTE_ADD_ALARM)
    object AddMembers: Screen(ROUTE_ADD_MEMBERS)
    object NewGroup: Screen(ROUTE_NEW_GROUP)

    private companion object {
        const val ROUTE_HOME = "home"
        const val ROUTE_SETTINGS = "settings"
        const val ROUTE_GROUPS = "groups"
        const val ROUTE_PROFILE = "profile"
        const val ROUTE_ADD_ALARM = "add_alarm"
        const val ROUTE_ADD_MEMBERS = "add_members"
        const val ROUTE_NEW_GROUP = "new_group"

        const val LOGIN_ROUTE = "login"
    }
}