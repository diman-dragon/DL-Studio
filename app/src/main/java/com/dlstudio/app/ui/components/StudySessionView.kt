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
import kotlinx.coroutines.delay

/** Сессия: карточки листаются свайпом, ниже карточки варианты ответа. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudySessionView(
    session: StudySession,
    onFlip: (String) -> Unit,
    onHint: () -> Unit,
    onPage: (Int) -> Unit,
    onNextRound: () -> Unit,
    onClose: () -> Unit,
    onSpeak: (String) -> Unit
) {
    key(session.id) {
        val pager = rememberPagerState(initialPage = session.index) { session.total + 1 }
        LaunchedEffect(pager) { snapshotFlow { pager.currentPage }.collect { onPage(it) } }
        LaunchedEffect(pager.currentPage) { if (session.random && pager.currentPage == session.total) { delay(1800); onNextRound() } }
        Column(Modifier.fillMaxSize().padding(16.dp), Arrangement.spacedBy(16.dp)) {
            SessionHeader(session.finished, session.total, onClose)
            HorizontalPager(pager, Modifier.weight(1f), pageSpacing = 16.dp, key = { it }) { page ->
                val card = session.queue.getOrNull(page)
                if (card == null) SessionDone(session, onClose)
                else StudyCardPage(card, page, session, pager.currentPage == page, onFlip, onHint, onSpeak)
            }
        }
    }
}
