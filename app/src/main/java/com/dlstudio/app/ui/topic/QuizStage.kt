package com.dlstudio.app.ui.topic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.content.QuizItem
import com.dlstudio.app.content.QuizKind
import com.dlstudio.app.i18n.Strings
import com.dlstudio.app.speech.Check
import com.dlstudio.app.speech.Characters
import com.dlstudio.app.speech.Speak
import com.dlstudio.app.speech.SpeakAs
import com.dlstudio.app.ui.components.CharacterAvatar
import com.dlstudio.app.ui.components.GradientButton
import com.dlstudio.app.ui.components.Mood
import com.dlstudio.app.ui.components.ProgressBar
import com.dlstudio.app.ui.theme.LocalStrings

private const val PASS_PERCENT = 60

/**
 * Практика: вопросы по порядку, виды — выбор из четырёх, «на слух», произнести вслух, мешок слов, вписать, соединить пары.
 * Верный ответ даёт XP (один раз за вопрос), неверный показывает правильный. Вопросы, которым нужны озвучка и микрофон,
 * показываются только в темах с языком озвучки. Персонаж радуется верному ответу и огорчается неверному: тот, чей голос звучал в вопросе, а у неозвученных — преподаватели по очереди. В конце — результат с мисс Грейс и «Пройти снова». Этап считается пройденным, когда верно решено 60% вопросов (`done` приходит снаружи).
 */
@Composable
fun QuizStage(items: List<QuizItem>, done: Boolean, speak: SpeakAs?, check: Check?, onAnswer: (Int, Boolean) -> Unit, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    val order = remember(items, speak != null) { items.indices.filter { speak != null || !items[it].kind.needsVoice } }
    var pos by rememberSaveable { mutableStateOf(0) }
    var correct by rememberSaveable { mutableStateOf(0) }
    var result by remember(pos) { mutableStateOf<Boolean?>(null) }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), Arrangement.spacedBy(16.dp)) {
        if (pos >= order.size) {
            val good = correct * 100 >= order.size * PASS_PERCENT
            Box(Modifier.fillMaxWidth().padding(top = 16.dp), Alignment.Center) { CharacterAvatar(Characters.DEFAULT, 140.dp, mood = if (good) Mood.HAPPY else Mood.SAD) }
            Text(s.quizResult(correct, order.size), Modifier.fillMaxWidth(), style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            if (done) Text(s.stageDone, Modifier.fillMaxWidth(), MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            GradientButton(s.retry, { pos = 0; correct = 0 })
        } else {
            val index = order[pos]
            val item = items[index]
            ProgressBar(pos / order.size.toFloat())
            key(pos) {
                Question(item, speak, check) { ok ->
                    if (result == null) {
                        result = ok
                        if (ok) correct += 1
                        onAnswer(index, ok)
                    }
                }
            }
            result?.let { ok ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    CharacterAvatar(reactor(item, pos), 64.dp, mood = if (ok) Mood.HAPPY else Mood.SAD)
                    Text(verdict(s, item, ok), Modifier.weight(1f), MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                }
                GradientButton(if (pos == order.lastIndex) s.seeResult else s.next, { pos += 1 })
            }
        }
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
