package com.pelodeck.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pelodeck.domain.model.ThemeSkin
import com.pelodeck.ui.dashboard.DashboardScreen
import com.pelodeck.ui.diagnostics.DiagnosticsScreen
import com.pelodeck.ui.history.HistoryScreen
import com.pelodeck.ui.login.LoginScreen
import com.pelodeck.ui.launcher.LauncherScreen

@Composable
fun PeloDeckNavHost(
    currentSkin: ThemeSkin,
    onSkinSelected: (ThemeSkin) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            LauncherScreen(onOpenPeloDeck = { navController.navigate(Screen.Login.route) { launchSingleTop = true } })
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateHome = { navController.popBackStack(Screen.Home.route, false) },
                onStartRideWithProfile = {
                    navController.navigate(Screen.Dashboard.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToDiagnostics = { navController.navigate(Screen.Diagnostics.route) }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                currentSkin = currentSkin,
                onSkinSelected = onSkinSelected,
                onNavigateToProfiles = { navController.navigate(Screen.Login.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToDiagnostics = { navController.navigate(Screen.Diagnostics.route) }
            )
        }

        composable(Screen.Profiles.route) {
            LoginScreen(
                onNavigateHome = { navController.popBackStack(Screen.Home.route, false) },
                onStartRideWithProfile = {
                    navController.navigate(Screen.Dashboard.route) {
                        launchSingleTop = true
                    }
                },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToDiagnostics = { navController.navigate(Screen.Diagnostics.route) }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Diagnostics.route) {
            DiagnosticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
