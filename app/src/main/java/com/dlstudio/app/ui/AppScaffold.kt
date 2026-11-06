package com.dlstudio.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.dlstudio.app.ui.components.BottomTabs

/** Четыре экрана и нижние вкладки. Экран строится только при входе; состояние лежит в моделях, поэтому ничего не теряется. */
@Composable
fun AppScaffold(state: AppState, vm: AppViewModel, onFolder: () -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    BackHandler(enabled = tab != 0) { tab = 0 }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, bottomBar = { BottomTabs(tab) { tab = it } }) { padding ->
        AnimatedContent(
            targetState = tab,
            modifier = Modifier.fillMaxSize().padding(padding),
            transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(90)) },
            label = "screen"
        ) { index -> AppPage(index, state, vm, onFolder) }
    }
}
