package com.pelodeck.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object Profiles : Screen("profiles")
    data object History : Screen("history")
    data object Diagnostics : Screen("diagnostics")
}
