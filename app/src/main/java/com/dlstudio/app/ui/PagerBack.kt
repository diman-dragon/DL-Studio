package com.dlstudio.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

/** Системная «Назад»: с любого экрана — на первый (Учёба); на первом — выход из приложения. */
@Composable
fun PagerBack(pager: PagerState) {
    val scope = rememberCoroutineScope()
    BackHandler(enabled = pager.currentPage != 0) { scope.launch { pager.animateScrollToPage(0) } }
}
