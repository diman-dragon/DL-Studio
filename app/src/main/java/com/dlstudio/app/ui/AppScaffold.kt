package com.dlstudio.app.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.components.BottomTabs
import com.dlstudio.app.ui.components.Confetti
import com.dlstudio.app.ui.components.GainToast
import com.dlstudio.app.ui.components.Tabs
import com.dlstudio.app.ui.screens.MapScreen
import com.dlstudio.app.ui.screens.ProgressScreen
import com.dlstudio.app.ui.screens.SettingsScreen
import com.dlstudio.app.ui.screens.StartScreen
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.topic.TopicScreen
import kotlinx.coroutines.launch

/**
 * Каркас: четыре вкладки со свайпом (самая левая — стартовый экран, карта правее); открытая тема плавно занимает весь экран. «Назад» закрывает тему, затем листает вкладки.
 * Сверху лежит мягкое свечение акцентного цвета, а когда пройдена тема, поверх всего запускается салют.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppScaffold(state: AppState, vm: AppViewModel) {
    val pager = rememberPagerState { Tabs.all.size }
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current as? Activity
    var openId by rememberSaveable { mutableStateOf<String?>(null) }
    val topic = state.topics.firstOrNull { it.id == openId }
    val toSettings = { scope.launch { pager.animateScrollToPage(Tabs.all.lastIndex); Unit } }

    BackHandler {
        when {
            topic != null -> openId = null
            pager.currentPage == 0 -> activity?.moveTaskToBack(true)
            else -> scope.launch { pager.animateScrollToPage(pager.currentPage - 1) }
        }
    }
    val glow = Color(LocalAccent.current.main)
    Box(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).drawBehind {
            drawRect(Brush.radialGradient(listOf(glow.copy(alpha = 0.2f), Color.Transparent), Offset(size.width / 2, 0f), size.width * 1.1f))
        }
    ) {
        Crossfade(topic, label = "topic") { shown ->
            if (shown != null) {
                TopicScreen(shown, state, vm) { openId = null }
            } else {
                Scaffold(
                    containerColor = Color.Transparent,
                    bottomBar = { BottomTabs(pager.currentPage) { target -> scope.launch { pager.animateScrollToPage(target) } } }
                ) { padding ->
                    HorizontalPager(pager, Modifier.fillMaxSize().padding(padding)) { page ->
                        when (page) {
                            0 -> StartScreen(state, { openId = it }, { toSettings() })
                            1 -> MapScreen(state, { openId = it }, { toSettings() })
                            2 -> ProgressScreen(state)
                            else -> SettingsScreen(state, vm)
                        }
                    }
                }
            }
        }
        GainToast(state.gain, vm::clearGain, Modifier.align(Alignment.TopCenter).padding(top = 12.dp))
        state.gain?.takeIf { it.finished }?.let { Confetti(it.id) }
    }
}
