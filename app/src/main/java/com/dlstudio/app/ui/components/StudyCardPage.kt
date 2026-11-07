package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.study.CardMode
import com.dlstudio.app.study.ModeWeights
import com.dlstudio.app.study.StudySession
import com.dlstudio.app.ui.theme.LocalStudyOptions
import kotlinx.coroutines.delay

/** Страница сессии: в библиотеке — простой просмотр; в плановой учёбе — интерактивные режимы. */
@Composable
fun StudyCardPage(
    card: LibNode,
    page: Int,
    session: StudySession,
    isCurrent: Boolean,
    onFlip: (String) -> Unit,
    onHint: () -> Unit,
    onSpeak: (String) -> Unit
) {
    val enabled = LocalStudyOptions.current.modes
    val interactive = !session.random && card.back.isNotBlank()
    val mode = remember(card.id, page, interactive, enabled) {
        if (interactive) ModeWeights.modeFor(card, page, enabled) else CardMode.CHOICE_FWD
    }
    var hintReady by remember(card.id) { mutableStateOf(false) }
    var listenRevealed by remember(card.id) { mutableStateOf(false) }
    var selected by remember(card.id) { mutableStateOf<String?>(null) }
    var submitted by remember(card.id) { mutableStateOf(false) }
    val isReverse = mode == CardMode.CHOICE_REV || mode == CardMode.WRITE_REV
    val correct = if (isReverse) card.name else card.back
    val options = remember(card.id, page) { ModeWeights.optionsFor(card, session.queue, isReverse, page) }
    val canFlip = !interactive || selected != null || submitted || card.id in session.flipped

    LaunchedEffect(isCurrent, card.id) {
        if (isCurrent && mode == CardMode.LISTEN && interactive) {
            onSpeak(card.name); delay(4000L); hintReady = true
        }
    }
    fun flipOnce() { if (card.id !in session.flipped) onFlip(card.id) }
    fun handleHint() {
        if (mode == CardMode.LISTEN && interactive) {
            if (hintReady) { listenRevealed = true; onHint() }
        } else onHint()
    }
    Column(Modifier.fillMaxSize(), Arrangement.spacedBy(14.dp), Alignment.CenterHorizontally) {
        StudyFlashCard(
            card, mode, card.id in session.flipped, card.id in session.hinted,
            hintReady, listenRevealed, canFlip, { onFlip(card.id) }, ::handleHint, onSpeak, Modifier.weight(1f)
        )
        if (interactive) CardAnswers(mode, correct, options, selected, submitted, { selected = it; flipOnce() }, { submitted = true; flipOnce() }, if (isReverse) onSpeak else null)
    }
}
