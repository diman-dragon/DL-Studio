package com.dlstudio.app.ui.topic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import com.dlstudio.app.ui.components.Pill
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.content.QuizItem
import com.dlstudio.app.content.QuizKind
import com.dlstudio.app.i18n.Strings
import com.dlstudio.app.speech.Characters
import com.dlstudio.app.speech.Check
import com.dlstudio.app.speech.Speak
import com.dlstudio.app.speech.SpeakAs
import com.dlstudio.app.ui.components.FeedbackPanel
import com.dlstudio.app.ui.components.QuizHeader
import com.dlstudio.app.ui.components.QuizResult
import com.dlstudio.app.ui.components.Reveal
import com.dlstudio.app.ui.theme.LocalStrings
import kotlinx.coroutines.delay

private const val PASS_PERCENT = 60
private const val SCROLL_DELAY_MS = 250L

/**
 * Практика в стиле приложения: сверху номер вопроса, прогресс и счёт, затем карточка вопроса с яркой шапкой, внизу реакция персонажа. Вопросы по порядку, виды — выбор из четырёх, «на слух», произнести вслух, мешок слов, вписать, соединить пары.
 * Верный ответ даёт XP (один раз за вопрос), неверный показывает правильный. Вопросы, которым нужны озвучка и микрофон,
 * показываются только в темах с языком озвучки. Персонаж радуется верному ответу и огорчается неверному: тот, чей голос звучал в вопросе, а у неозвученных — преподаватели по очереди. В конце — результат с мисс Грейс и «Пройти снова». Этап считается пройденным, когда верно решено 60% вопросов (`done` приходит снаружи).
 */
@Composable
fun QuizStage(items: List<QuizItem>, done: Boolean, speak: SpeakAs?, check: Check?, onAnswer: (Int, Boolean) -> Unit, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    var quiet by rememberSaveable { mutableStateOf(false) }
    val sound = speak != null && !quiet
    val order = remember(items, sound) { playable(items, sound) }
    var pos by rememberSaveable { mutableStateOf(0) }
    var correct by rememberSaveable { mutableStateOf(0) }
    var result by remember(pos) { mutableStateOf<Boolean?>(null) }
    val scroll = rememberScrollState()
    LaunchedEffect(result) { if (result != null) { delay(SCROLL_DELAY_MS); scroll.animateScrollTo(scroll.maxValue) } }
    Column(modifier.fillMaxSize().verticalScroll(scroll).padding(16.dp), Arrangement.spacedBy(16.dp)) {
        if (speak != null && items.any { it.needsSound }) QuietSwitch(quiet) {
            val next = playable(items, quiet)
            pos = order.getOrNull(pos)?.let { here -> next.indexOfFirst { it >= here }.let { if (it < 0) next.size else it } } ?: next.size
            quiet = !quiet
        }
        if (pos >= order.size) {
            QuizResult(correct, order.size, correct * 100 >= order.size * PASS_PERCENT, done) { pos = 0; correct = 0 }
        } else {
            val index = order[pos]
            val item = items[index]
            QuizHeader(pos, order.size, correct)
            key(pos) {
                Question(item, speak.takeIf { sound }, check) { ok ->
                    if (result == null) {
                        result = ok
                        if (ok) correct += 1
                        onAnswer(index, ok)
                    }
                }
            }
            result?.let { ok ->
                Reveal(0) { FeedbackPanel(ok, reactor(item, pos), verdict(s, item, ok), if (pos == order.lastIndex) s.seeResult else s.next) { pos += 1 } }
            }
        }
    }
}

/** Номера вопросов, которые можно решать: без звука не показываются те, где он нужен. */
private fun playable(items: List<QuizItem>, sound: Boolean): List<Int> = items.indices.filter { sound || !items[it].needsSound }

/** Переключатель «Без звука» на этот раз (в автобусе, в тишине): прячет вопросы «на слух» и «произнеси»; не запоминается. */
@Composable
private fun QuietSwitch(quiet: Boolean, onToggle: () -> Unit) {
    val s = LocalStrings.current
    Row(Modifier.fillMaxWidth(), Arrangement.End) {
        Pill(if (quiet) s.quietOff else s.quietOn, Modifier.clip(RoundedCornerShape(16.dp)).clickable(role = Role.Switch, onClick = onToggle), accent = quiet)
    }
}

/** Кто реагирует на ответ: если вопрос озвучен, то тот, чей голос звучал (портрет и голос совпадают); иначе преподаватели по очереди. */
private fun reactor(item: QuizItem, pos: Int): String = when {
    item.who != null -> item.who
    item.say.isNotBlank() -> Characters.DEFAULT
    else -> Characters.teachers[pos % Characters.teachers.size].id
}

@Composable
private fun Question(item: QuizItem, speakAs: SpeakAs?, check: Check?, onDone: (Boolean) -> Unit) {
    val speak: Speak? = speakAs?.let { f -> { text: String -> f(text, item.who, QUIZ_LINE) } }
    when (item.kind) {
        QuizKind.CHOICE, QuizKind.LISTEN -> ChoiceQuestion(item, speak, onDone)
        QuizKind.SPEAK -> if (speak != null) SpeakQuestion(item, speak, check, onDone)
        QuizKind.BANK -> BankQuestion(item, onDone)
        QuizKind.FILL -> FillQuestion(item, speak, onDone)
        QuizKind.MATCH -> MatchQuestion(item, onDone)
    }
}

/** Строка под вопросом после ответа: «Верно!» или то, как было правильно. */
private fun verdict(s: Strings, item: QuizItem, ok: Boolean): String = when {
    ok -> s.correct
    item.kind == QuizKind.SPEAK -> s.speakSkipped
    item.kind == QuizKind.MATCH -> s.matchWrong
    else -> s.wrongAnswer(item.answer)
}
