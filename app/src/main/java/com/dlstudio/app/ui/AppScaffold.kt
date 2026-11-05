package com.dlstudio.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.dlstudio.app.ui.components.BottomTabs
import com.dlstudio.app.ui.components.Tabs
import kotlinx.coroutines.launch

/** Четыре экрана в `HorizontalPager` (свайп) + нижние вкладки; обе стороны управляют одним состоянием. */
@Composable
fun AppScaffold(state: AppState, vm: AppViewModel, onFolder: () -> Unit) {
    val pager = rememberPagerState { Tabs.all.size }
    val scope = rememberCoroutineScope()
    PagerBack(pager)
    Scaffold(bottomBar = {
        BottomTabs(pager.currentPage) { scope.launch { pager.animateScrollToPage(it) } }
    }) { padding ->
        HorizontalPager(pager, Modifier.fillMaxSize().padding(padding)) { index ->
            AppPage(index, state, vm, onFolder)
        }
    }
}
