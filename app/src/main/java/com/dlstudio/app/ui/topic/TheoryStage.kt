package com.dlstudio.app.ui.topic

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.content.TheoryPage
import com.dlstudio.app.ui.components.GradientButton
import com.dlstudio.app.ui.components.Markup
import com.dlstudio.app.ui.components.OutlineButton
import com.dlstudio.app.ui.components.ProgressBar
import com.dlstudio.app.ui.components.SettingCard
import com.dlstudio.app.ui.theme.LocalStrings
import kotlinx.coroutines.launch

/** Теория: страницы со свайпом и полосой прогресса; на последней странице кнопка «Готово» отмечает этап пройденным. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TheoryStage(pages: List<TheoryPage>, onFinish: () -> Unit, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    val pager = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val last = pager.currentPage == pages.lastIndex
    Column(modifier.fillMaxSize()) {
        ProgressBar((pager.currentPage + 1f) / pages.size, Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
        HorizontalPager(pager, Modifier.weight(1f)) { index -> PageView(pages[index]) }
        Row(Modifier.padding(16.dp), Arrangement.spacedBy(12.dp)) {
            if (pager.currentPage > 0) OutlineButton(s.previous, { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } }, Modifier.weight(1f))
            GradientButton(if (last) s.finish else s.next, { if (last) onFinish() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun PageView(page: TheoryPage) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp)) {
        SettingCard(gap = 14.dp) {
            if (page.heading.isNotBlank()) Text(page.heading, style = MaterialTheme.typography.headlineSmall)
            page.paragraphs.forEach { Text(Markup.annotate(it), style = MaterialTheme.typography.bodyLarge) }
        }
    }
}
