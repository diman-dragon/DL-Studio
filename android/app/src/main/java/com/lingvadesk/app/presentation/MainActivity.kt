package com.lingvadesk.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.lingvadesk.app.domain.study.PrefKeys
import com.lingvadesk.app.presentation.navigation.LingvaNavGraph
import com.lingvadesk.app.presentation.theme.LingvaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val app: AppViewModel = hiltViewModel()
            val settings by app.settings.collectAsStateWithLifecycle()
            LingvaTheme(theme = settings[PrefKeys.THEME] ?: "auto", font = settings[PrefKeys.FONT] ?: "m") {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    LingvaNavGraph(navController = rememberNavController(), app = app, settings = settings)
                }
            }
        }
    }
}
