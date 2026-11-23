package com.dlstudio.app.ui.topic

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.content.TheoryPage
import com.dlstudio.app.speech.Speak
import com.dlstudio.app.ui.components.GradientButton
import com.dlstudio.app.ui.components.OutlineButton
import com.dlstudio.app.ui.theme.LocalStrings
import kotlinx.coroutines.launch

/**
 * Теория: страницы-карточки со свайпом и точками-индикатором; на последней странице кнопка «Готово» отмечает этап пройденным.
 * Если у темы есть язык озвучки (`speak`), у фраз и плиток-букв появляется озвучка.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TheoryStage(pages: List<TheoryPage>, speak: Speak?, onFinish: () -> Unit, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    val pager = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val last = pager.currentPage == pages.lastIndex
    Column(modifier.fillMaxSize()) {
        PageDots(pages.size, pager.currentPage, Modifier.fillMaxWidth().padding(vertical = 10.dp))
        HorizontalPager(pager, Modifier.weight(1f), pageSpacing = 8.dp) { index ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp)) {
                TheoryCard(pages[index], speak)
            }
        }
        Row(Modifier.padding(16.dp), Arrangement.spacedBy(12.dp)) {
            if (pager.currentPage > 0) OutlineButton(s.previous, { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } }, Modifier.weight(1f))
            GradientButton(if (last) s.finish else s.next, { if (last) onFinish() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } }, Modifier.weight(1f))
        }
    }
}
