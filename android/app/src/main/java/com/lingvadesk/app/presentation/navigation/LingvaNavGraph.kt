package com.lingvadesk.app.presentation.navigation

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.lingvadesk.app.domain.study.PrefKeys
import com.lingvadesk.app.presentation.AppViewModel
import com.lingvadesk.app.presentation.library.LibraryScreen
import com.lingvadesk.app.presentation.settings.SettingsScreen
import com.lingvadesk.app.presentation.stats.StatsScreen
import com.lingvadesk.app.presentation.study.SessionScreen
import com.lingvadesk.app.presentation.study.StudyHubScreen

sealed class Screen(val route: String) {
    object Library : Screen("library")
    object StudyHub : Screen("study_hub")
    object Session : Screen("session/{scope}") {
        fun createRoute(scope: String) = "session/${Uri.encode(scope)}"
    }
    object Stats : Screen("stats")
    object Settings : Screen("settings")
}

private class Tab(val screen: Screen, val label: String, val icon: ImageVector)

/** Порядок вкладок единый: Учёба → Статистика → Библиотека → Настройки. */
private val tabs = listOf(
    Tab(Screen.StudyHub, "Учёба", Icons.Filled.PlayArrow),
    Tab(Screen.Stats, "Статистика", Icons.Filled.DateRange),
    Tab(Screen.Library, "Библиотека", Icons.AutoMirrored.Filled.List),
    Tab(Screen.Settings, "Настройки", Icons.Filled.Settings)
)

@Composable
fun LingvaNavGraph(navController: NavHostController, app: AppViewModel, settings: Map<String, String>) {
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val showBar = tabs.any { it.screen.route == route }

    // «Назад» с вкладки ведёт в библиотеку и дальше не выходит из приложения
    BackHandler(enabled = showBar) {
        if (route != Screen.Library.route) navigateTab(navController, Screen.Library.route)
    }

    Scaffold(bottomBar = {
        if (showBar) NavigationBar {
            tabs.forEach { t ->
                NavigationBarItem(
                    selected = route == t.screen.route, onClick = { navigateTab(navController, t.screen.route) },
                    icon = { Icon(t.icon, contentDescription = t.label) }, label = { Text(t.label, maxLines = 1) }
                )
            }
        }
    }) { padding ->
        NavHost(navController = navController, startDestination = Screen.StudyHub.route, modifier = Modifier.padding(padding)) {
            composable(Screen.StudyHub.route) {
                StudyHubScreen(grid = settings[PrefKeys.LAYOUT] == "grid", onStart = { scope -> navController.navigate(Screen.Session.createRoute(scope)) })
            }
            composable(Screen.Stats.route) { StatsScreen() }
            composable(Screen.Library.route) { LibraryScreen(grid = settings[PrefKeys.LAYOUT] == "grid") }
            composable(Screen.Settings.route) { SettingsScreen(app = app, settings = settings) }
            composable(Screen.Session.route, arguments = listOf(navArgument("scope") { type = NavType.StringType })) { e ->
                SessionScreen(
                    scope = Uri.decode(e.arguments?.getString("scope") ?: "all"), settings = settings,
                    onExit = { navController.popBackStack() }
                )
            }
        }
    }
}

private fun navigateTab(nav: NavHostController, route: String) {
    nav.navigate(route) {
        popUpTo(Screen.StudyHub.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
