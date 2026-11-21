package com.dlstudio.app.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.dlstudio.app.ui.components.BottomTabs
import com.dlstudio.app.ui.components.Tabs
import kotlinx.coroutines.launch

/** Каркас вкладок; внутренний Back обрабатывается экранами, корневой возвращает вкладку или сворачивает app. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppScaffold(state: AppState, vm: AppViewModel, onFolder: () -> Unit) {
    val pager = rememberPagerState { Tabs.all.size }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    BackHandler {
        if (pager.currentPage == 0) (context as? Activity)?.moveTaskToBack(true)
        else scope.launch { pager.animateScrollToPage(pager.currentPage - 1) }
    }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, bottomBar = {
        BottomTabs(pager.currentPage) { target -> scope.launch { pager.animateScrollToPage(target) } }
    }) { padding ->
        HorizontalPager(pager, Modifier.fillMaxSize().padding(padding)) { index -> AppPage(index, state, vm, onFolder) }
    }
}
