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
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.content.TheoryPage
import com.dlstudio.app.ui.components.GradientButton
import com.dlstudio.app.ui.components.OutlineButton
import com.dlstudio.app.ui.theme.LocalStrings
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

private const val PLACE_DELAY_MS = 400L

/**
 * Теория: страницы-карточки со свайпом и точками-индикатором; на последней странице кнопка «Готово» отмечает этап пройденным.
 * `talk` — голоса персонажей: если у темы есть язык озвучки, у фраз и плиток-букв появляется озвучка, реплики диалога звучат голосами своих персонажей;
 * `startPage` и `startScroll` — страница и прокрутка, где остановился прошлый раз, `onPlace` — запомнить страницу и прокрутку, `mediaDir` — папка картинок, звука и видео темы; `onPage` — просмотрена страница (для статистики).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TheoryStage(pages: List<TheoryPage>, startPage: Int, startScroll: Int, talk: Talk, mediaDir: File?, onPage: () -> Unit, onPlace: (Int, Int) -> Unit, onFinish: () -> Unit, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    val pager = rememberPagerState(startPage.coerceIn(0, pages.lastIndex)) { pages.size }
    val scope = rememberCoroutineScope()
    val last = pager.currentPage == pages.lastIndex
    LaunchedEffect(pager.currentPage) { talk.stop(); onPage() }
    val scrolls = remember { mutableMapOf<Int, ScrollState>() }
    LaunchedEffect(pager) {
        snapshotFlow { pager.currentPage to (scrolls[pager.currentPage]?.value ?: 0) }.collectLatest { (page, at) -> delay(PLACE_DELAY_MS); onPlace(page, at) }
    }
    Column(modifier.fillMaxSize()) {
        PageDots(pages.size, pager.currentPage, Modifier.fillMaxWidth().padding(vertical = 10.dp))
        HorizontalPager(pager, Modifier.weight(1f), pageSpacing = 8.dp) { index ->
            val scroll = remember(index) { ScrollState(if (index == startPage) startScroll else 0).also { scrolls[index] = it } }
            Column(Modifier.fillMaxSize().verticalScroll(scroll).padding(horizontal = 16.dp, vertical = 8.dp)) {
                TheoryCard(pages[index], index, talk, mediaDir)
            }
        }
        Row(Modifier.padding(16.dp), Arrangement.spacedBy(12.dp)) {
            if (pager.currentPage > 0) OutlineButton(s.previous, { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } }, Modifier.weight(1f))
            GradientButton(if (last) s.finish else s.next, { if (last) onFinish() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } }, Modifier.weight(1f))
        }
    }
}
