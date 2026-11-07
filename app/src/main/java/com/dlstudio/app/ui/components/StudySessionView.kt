package com.dlstudio.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.study.StudySession

/**
 * Сессия: карточки листаются свайпом вперёд и назад, порядок не меняется; последняя страница — итог.
 * Тап по карточке показывает перевод. Кнопок оценки нет: оценка выводится из поведения.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudySessionView(session: StudySession, onFlip: () -> Unit, onHint: () -> Unit, onPage: (Int) -> Unit, onClose: () -> Unit, onSpeak: (String) -> Unit) {
    key(session.id) {
        val pager = rememberPagerState(initialPage = session.index) { session.total + 1 }
        LaunchedEffect(pager) { snapshotFlow { pager.currentPage }.collect { onPage(it) } }
        Column(Modifier.fillMaxSize().padding(16.dp), Arrangement.spacedBy(16.dp)) {
            SessionHeader(session.finished, session.total, onClose)
            HorizontalPager(pager, Modifier.weight(1f), pageSpacing = 16.dp, key = { it }) { page ->
                val card = session.queue.getOrNull(page)
                if (card == null) SessionDone(session, onClose)
                else StudyFlashCard(card, card.id in session.flipped, card.id in session.hinted, onFlip, onHint, onSpeak, Modifier.fillMaxSize())
            }
        }
    }
}
