package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.Features
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.study.AutoGrade
import com.dlstudio.app.study.CardMode
import com.dlstudio.app.study.CardPlan
import com.dlstudio.app.study.Grade
import com.dlstudio.app.study.Speak
import com.dlstudio.app.study.SpeechLang
import com.dlstudio.app.study.StudySession
import com.dlstudio.app.ui.theme.LocalSpeechTag
import com.dlstudio.app.ui.theme.LocalStudyOptions
import kotlinx.coroutines.delay

/** Страница сессии: в библиотеке — простой просмотр; в плановой учёбе — режим по весам (`CardPlan`), ответ под карточкой. */
@Composable
fun StudyCardPage(
    card: LibNode, page: Int, session: StudySession, isCurrent: Boolean,
    onFlip: (String, Grade?) -> Unit, onHint: () -> Unit, onSpeak: Speak
) {
    val study = LocalStudyOptions.current
    val interactive = !Features.FLIP_ONLY && !session.random && card.back.isNotBlank()
    val plan = remember(card.id, page, interactive, study) { CardPlan.of(card, page, session.queue, interactive, study) }
    val mode = plan.mode
    val pronounce = rememberPronounce(plan.target, SpeechLang.forText(plan.target, LocalSpeechTag.current))
    var hintReady by remember(card.id) { mutableStateOf(false) }
    var listenRevealed by remember(card.id) { mutableStateOf(false) }
    var selected by remember(card.id) { mutableStateOf<String?>(null) }
    var submitted by remember(card.id) { mutableStateOf(false) }
    val flipped = card.id in session.flipped
    val canFlip = !interactive || mode.flip || selected != null || submitted || flipped || (mode.speaks && pronounce.phase.finished)
    val grade = if (mode.speaks) AutoGrade.ofSpeech(pronounce.phase.verdict, card.id in session.hinted) else null

    LaunchedEffect(isCurrent, card.id, plan) {
        if (!isCurrent || !interactive) return@LaunchedEffect
        if (mode == CardMode.REPEAT) onSpeak(plan.target)
        if (mode == CardMode.LISTEN) { onSpeak(card.name); delay(4000L); hintReady = true }
    }
    fun flipOnce() { if (!flipped) onFlip(card.id, grade) }
    fun handleHint() {
        if (mode != CardMode.LISTEN || !interactive) onHint()
        else if (hintReady) { listenRevealed = true; onHint() }
    }
    Column(Modifier.fillMaxSize(), Arrangement.spacedBy(14.dp), Alignment.CenterHorizontally) {
        StudyFlashCard(
            card, plan, flipped, card.id in session.hinted, hintReady, listenRevealed, canFlip,
            { onFlip(card.id, grade) }, ::handleHint, onSpeak, pronounce, Modifier.weight(1f)
        )
        if (interactive) CardAnswers(plan, selected, submitted, pronounce, { selected = it; flipOnce() }, { submitted = true; flipOnce() }, ::flipOnce, onSpeak)
    }
}
