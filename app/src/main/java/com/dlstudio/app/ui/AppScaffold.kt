package com.dlstudio.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.library.LibraryViewModel
import com.dlstudio.app.study.StudyViewModel
import com.dlstudio.app.ui.components.BottomTabs
import com.dlstudio.app.ui.components.Tabs
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Четыре экрана в пейджере: листаются свайпом и иконками внизу. «Назад» идёт на шаг к первому экрану
 * (внутренние экраны — библиотека, учёба — обрабатывают «Назад» раньше), на первом — выход из приложения.
 * Во время учебной сессии свайп экранов выключен: горизонтальный свайп принадлежит карточкам.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppScaffold(state: AppState, vm: AppViewModel, onFolder: () -> Unit, study: StudyViewModel = viewModel(), library: LibraryViewModel = viewModel()) {
    val pager = rememberPagerState { Tabs.all.size }
    val scope = rememberCoroutineScope()
    val studying by remember(study) { study.ui.map { it.session != null }.distinctUntilChanged() }.collectAsStateWithLifecycle(false)
    val openStudy = { cards: List<LibNode> ->
        study.beginRandom(cards)?.let { library.startSession(it.id, it.queue.map(LibNode::id), true); scope.launch { pager.animateScrollToPage(0) } }
        Unit
    }
    BackHandler(enabled = pager.currentPage != 0) { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, bottomBar = {
        BottomTabs(pager.currentPage) { target -> scope.launch { if (abs(target - pager.currentPage) == 1) pager.animateScrollToPage(target) else pager.scrollToPage(target) } }
    }) { padding ->
        HorizontalPager(pager, Modifier.fillMaxSize().padding(padding), userScrollEnabled = !studying) { index -> AppPage(index, state, vm, onFolder, openStudy) }
    }
}
