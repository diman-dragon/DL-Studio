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
import com.dlstudio.app.study.Grade
import com.dlstudio.app.study.CardPlan
import com.dlstudio.app.study.Speak
import com.dlstudio.app.study.StudySession
import com.dlstudio.app.ui.theme.LocalStudyOptions
import com.dlstudio.app.ui.theme.LocalStrings
import kotlinx.coroutines.delay

/** Сессия: карточки листаются свайпом, ниже карточки варианты ответа. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudySessionView(
    session: StudySession,
    onFlip: (String, Grade?) -> Unit,
    onHint: (String) -> Unit,
    onPage: (Int) -> Unit,
    onNextRound: () -> Unit,
    onClose: () -> Unit,
    onSpeak: Speak
) {
    key(session.id) {
        val pager = rememberPagerState(initialPage = session.index) { session.total + 1 }
        val card = session.current
        val plan = card?.let { CardPlan.of(it, session.index, session.queue, !session.random && it.back.isNotBlank(), LocalStudyOptions.current) }
        val task = plan?.mode?.takeIf { !it.flip }?.let { LocalStrings.current.taskText(it) }
        LaunchedEffect(pager) { snapshotFlow { pager.currentPage }.collect { onPage(it) } }
        LaunchedEffect(pager.currentPage) { if (session.random && pager.currentPage == session.total) { delay(1800); onNextRound() } }
        Column(Modifier.fillMaxSize().padding(16.dp), Arrangement.spacedBy(16.dp)) {
            SessionHeader(session.finished, session.total, onClose, task, "${card?.id}:$task")
            HorizontalPager(pager, Modifier.weight(1f), pageSpacing = 16.dp, key = { it }) { page ->
                val pageCard = session.queue.getOrNull(page)
                if (pageCard == null) SessionDone(session, onClose)
                else StudyCardPage(pageCard, page, session, pager.currentPage == page, onFlip, onHint, onSpeak)
            }
        }
    }
}
