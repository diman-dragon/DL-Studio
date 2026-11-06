package com.dlstudio.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.study.CardState
import com.dlstudio.app.study.DayMath
import com.dlstudio.app.study.Grade
import com.dlstudio.app.study.StudySession

/** Идущая сессия: прогресс, карточка со сменой слайдом и, после переворота, кнопки оценки. */
@Composable
fun StudySessionView(session: StudySession, states: Map<String, CardState>, onFlip: () -> Unit, onGrade: (Grade) -> Unit, onClose: () -> Unit, onSpeak: (String) -> Unit) {
    if (session.done) { SessionDone(session, onClose); return }
    val today = remember(session.index) { DayMath.dayNumber(System.currentTimeMillis()) }
    Column(Modifier.fillMaxSize().padding(16.dp), Arrangement.spacedBy(16.dp)) {
        SessionHeader(session.finished, session.total, onClose)
        AnimatedContent(
            targetState = session.index, modifier = Modifier.weight(1f),
            transitionSpec = { (slideInHorizontally { it / 3 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut()) },
            label = "card"
        ) { index ->
            session.queue.getOrNull(index)?.let { card ->
                StudyFlashCard(card, if (index == session.index) session.flipped else true, onFlip, onSpeak, Modifier.fillMaxSize())
            }
        }
        Box(Modifier.height(80.dp)) { if (session.flipped) GradeBar(states[session.current?.id], today, onGrade) }
    }
}
