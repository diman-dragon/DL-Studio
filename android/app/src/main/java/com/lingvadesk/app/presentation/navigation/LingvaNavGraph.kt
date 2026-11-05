package com.lingvadesk.app.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

sealed class Screen(val route: String) {
    object Library : Screen("library")
    object StudyHub : Screen("study_hub")
    object Session : Screen("session/{deckId}") {
        fun createRoute(deckId: Long) = "session/$deckId"
    }
    object Stats : Screen("stats")
    object Settings : Screen("settings")
}

@Composable
fun LingvaNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.StudyHub.route) {
        composable(Screen.StudyHub.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Study Hub Screen (Native Compose)")
            }
        }
        composable(Screen.Library.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Library Screen (Native Compose)")
            }
        }
        composable(Screen.Stats.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Stats Screen (Native Compose)")
            }
        }
        composable(Screen.Settings.route) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Settings Screen (Native Compose)")
            }
        }
    }
}
