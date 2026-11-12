package com.dlstudio.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.study.AutoGrade
import com.dlstudio.app.study.CardMode
import com.dlstudio.app.study.CardPlan
import com.dlstudio.app.study.Grade
import com.dlstudio.app.study.Speak
import com.dlstudio.app.study.SpeechLang
import com.dlstudio.app.study.StudySession
import com.dlstudio.app.ui.theme.LocalSpeechTag
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.LocalStudyOptions
import kotlin.math.abs
import kotlinx.coroutines.delay

/**
 * Лицевая сторона: слово по центру максимально крупно, внизу подсказка, микрофон проверки и озвучка.
 * В режимах голоса микрофон стоит под карточкой, на лицевой стороне только подсказка («Произношение») или озвучка («Повтор»).
 */
@Composable
fun FlashFront(card: LibNode, mode: CardMode, target: String, hint: Boolean, hintReady: Boolean, listenRevealed: Boolean, onHint: () -> Unit, onSpeak: Speak, pronounce: PronounceState) {
    val s = LocalStrings.current
    val word = when { mode == CardMode.REPEAT -> target; mode.reverse -> card.back.ifBlank { card.name }; else -> card.name }
    val listening = mode == CardMode.LISTEN
    val note = card.hintText
    val canHint = if (listening) hintReady else !mode.reverse && mode != CardMode.REPEAT && note.isNotBlank()
    val mic = (!mode.reverse || mode == CardMode.PRONOUNCE) && LocalStudyOptions.current.pronounce
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().weight(1f), Alignment.Center) {
            if (listening && !listenRevealed) CardWord(s.listenPrompt, MaterialTheme.colorScheme.onSurfaceVariant)
            else CardWord(word, MaterialTheme.colorScheme.onSurface, heard = if (mode == CardMode.REPEAT || (mic && mode != CardMode.PRONOUNCE)) pronounce.heard else "")
        }
        if (hint && note.isNotBlank() && !listening) CardNote(note, Modifier.padding(bottom = 8.dp))
        if (mode.reverse && card.example.isNotBlank()) SpeakNote(card.example, onSpeak, Modifier.padding(bottom = 8.dp), pronounce = if (mic) pronounce else null)
        if (mic) PronounceFeedback(pronounce)
        if (!mode.reverse) CardActions(
            speakText = if (mode == CardMode.REPEAT) target else card.name, onSpeak = onSpeak,
            pronounce = pronounce, onHint = onHint,
            hintLit = listening && hintReady && !listenRevealed, hintOn = canHint, pronounceOn = mic
        )
    }
}

/** Обратная сторона: перевод (или изучаемое слово) максимально крупно, пояснение (сколько угодно строк, блок под словом прокручивается) и пример с озвучкой; у изучаемого слова — озвучка и микрофон. */
@Composable
fun FlashBack(card: LibNode, mode: CardMode, onSpeak: Speak, pronounce: PronounceState) {
    val text = if (mode.reverse) card.name else card.back.ifBlank { card.name }
    val mic = mode.reverse && !mode.speaks && LocalStudyOptions.current.pronounce
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().weight(1f), Alignment.Center) { CardWord(text, MaterialTheme.colorScheme.onSurface, heard = if (mode.reverse) pronounce.heard else "") }
        Column(Modifier.heightIn(max = 260.dp).verticalScroll(rememberScrollState())) {
            if (card.explanation.isNotBlank()) ExpandableNote(card.explanation, Modifier.padding(bottom = 8.dp))
            if (card.example.isNotBlank()) SpeakNote(card.example, onSpeak, Modifier.padding(bottom = 8.dp), pronounce = if (!mode.reverse && LocalStudyOptions.current.pronounce) pronounce else null)
        }
        if (mic) PronounceFeedback(pronounce)
        if (mode.reverse) CardActions(card.name, onSpeak, pronounce, {}, false, true, mic)
    }
}

/** Карточка с переворотом; для просмотра (`FLIP_FWD`) — новое лицо и оборот со значениями, для остальных режимов прежние. Переворот заблокирован, пока нет ответа (`canFlip`). */
@Composable
fun StudyFlashCard(
    card: LibNode, plan: CardPlan, flipped: Boolean, hint: Boolean, hintReady: Boolean, listenRevealed: Boolean, canFlip: Boolean,
    onFlip: () -> Unit, onHint: () -> Unit, onSpeak: Speak, pronounce: PronounceState, modifier: Modifier = Modifier
) {
    val flipLabel = LocalStrings.current.tapToFlip
    val turn by animateFloatAsState(if (flipped) 1f else 0f, tween(260, easing = FastOutSlowInEasing), label = "flip")
    val shape = CardStyle.shape
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = modifier.fillMaxWidth()
            .clickable(remember { MutableInteractionSource() }, null, enabled = canFlip, role = Role.Button, onClick = onFlip)
            .semantics { contentDescription = flipLabel }
            .graphicsLayer { scaleX = abs(1f - 2f * turn).coerceAtLeast(0.02f) }
            .clip(shape).background(cs.surfaceVariant).border(1.dp, cs.outlineVariant, shape).padding(16.dp)
    ) {
        when {
            turn < 0.5f && plan.mode.flip -> FlipFront(card, hint, onHint, onSpeak, pronounce)
            turn < 0.5f -> FlashFront(card, plan.mode, plan.target, hint, hintReady, listenRevealed, onHint, onSpeak, pronounce)
            plan.mode.flip && card.meanings.isNotEmpty() -> FlipBack(card, onSpeak)
            else -> FlashBack(card, plan.mode, onSpeak, pronounce)
        }
    }
}

@Composable
fun StudyCardPage(
    card: LibNode, page: Int, session: StudySession, isCurrent: Boolean,
    onFlip: (String, Grade?) -> Unit, onHint: (String) -> Unit, onSpeak: Speak
) {
    val study = LocalStudyOptions.current
    val interactive = !session.random && card.back.isNotBlank()
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
        if (mode != CardMode.LISTEN || !interactive) onHint(card.id)
        else if (hintReady) { listenRevealed = true; onHint(card.id) }
    }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize(), Arrangement.spacedBy(14.dp), Alignment.CenterHorizontally) {
            StudyFlashCard(
                card, plan, flipped, card.id in session.hinted, hintReady, listenRevealed, canFlip,
                { onFlip(card.id, grade) }, ::handleHint, onSpeak, pronounce, Modifier.weight(1f)
            )
            if (interactive) CardAnswers(plan, selected, submitted, { selected = it; flipOnce() }, { submitted = true; flipOnce() }, onSpeak)
        }
    }
}

/**
 * Нижний ряд кнопок карточки, расположение всегда одинаковое: подсказка слева, динамик по центру (в 1,5 раза крупнее), микрофон справа.
 * Подсказка без текста (`hintOn = false`) остаётся на месте приглушённой.
 */
@Composable
fun CardActions(speakText: String, onSpeak: Speak, pronounce: PronounceState, onHint: () -> Unit, hintLit: Boolean, hintOn: Boolean, pronounceOn: Boolean) {
    val cs = androidx.compose.material3.MaterialTheme.colorScheme
    val hintColors = listOf(cs.tertiary, cs.tertiary.copy(alpha = .75f))
    val soundColors = listOf(cs.primary, cs.primary.copy(alpha = .75f))
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val d = minOf(64.dp, maxWidth / 4)
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            CardActionIcon(R.drawable.ic_lightbulb, onHint, colors = hintColors, highlight = hintLit, enabled = hintOn, size = d, iconSize = d * 0.6f, contentDescription = LocalStrings.current.hintButton)
            Box {
                CardActionIcon(R.drawable.ic_volume, { onSpeak(speakText) }, colors = soundColors, size = d * 1.5f, iconSize = d * 0.9f, contentDescription = LocalStrings.current.speak)
                SlowButton(speakText, Modifier.align(Alignment.BottomEnd).offset(x = 8.dp, y = 6.dp))
            }
            PronounceButton(pronounce, size = d, enabled = pronounceOn)
        }
    }
}

/**
 * Область ответа под карточкой: у карточки с переворотом её нет; в режимах голоса — микрофон; в режимах «письмо» —
 * ввод с клавиатуры, иначе четыре варианта. Голосовой ответ завершается тапом по карточке.
 */
@Composable
fun CardAnswers(
    plan: CardPlan, selected: String?, submitted: Boolean,
    onPick: (String) -> Unit, onSubmit: () -> Unit, onSpeak: Speak
) {
    val mode = plan.mode
    when {
        mode.flip -> Unit
        mode.speaks -> Unit
        mode.typed -> CardWriteInput(plan.correct, submitted, { onSubmit() }, onSpeak = if (mode.reverse) onSpeak else null)
        else -> ChoiceOptions(plan.options, plan.correct, selected, onPick)
    }
}
