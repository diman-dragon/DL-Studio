package com.dlstudio.app.ui.topic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.content.Answers
import com.dlstudio.app.content.QuizItem
import com.dlstudio.app.content.QuizKind
import com.dlstudio.app.speech.Characters
import com.dlstudio.app.speech.Check
import com.dlstudio.app.speech.Grade
import com.dlstudio.app.speech.Phase
import com.dlstudio.app.speech.Pronunciation
import com.dlstudio.app.speech.Speak
import com.dlstudio.app.ui.components.CharacterAvatar
import com.dlstudio.app.ui.components.ChoiceOptions
import com.dlstudio.app.ui.components.GradientButton
import com.dlstudio.app.ui.components.OutlineButton
import com.dlstudio.app.ui.components.PronounceRow
import com.dlstudio.app.ui.components.SettingCard
import com.dlstudio.app.ui.components.SmallSpeaker
import com.dlstudio.app.ui.theme.LocalStrings

/** Размер большой кнопки озвучки в вопросах «на слух» и диктанте и портрета говорящего рядом с ней. */
private val BIG_SPEAKER = 76.dp
private val AVATAR = 72.dp

/** Метка строки вопроса: оживает портрет того, чей голос звучит в вопросе (`who`, по умолчанию мисс Грейс). */
const val QUIZ_LINE = "quiz"

/** Карточка с текстом вопроса; у вопросов с озвучкой над текстом стоят портрет говорящего и большая кнопка динамика. */
@Composable
private fun QuestionCard(text: String, speak: Speak?, say: String, who: String?, hint: String = "") {
    SettingCard(gap = 12.dp) {
        if (speak != null && say.isNotBlank()) {
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally), Alignment.CenterVertically) {
                CharacterAvatar(Characters.of(who).id, AVATAR, line = QUIZ_LINE)
                SmallSpeaker(say, speak, size = BIG_SPEAKER)
            }
        }
        if (text.isNotBlank()) Text(text, Modifier.fillMaxWidth(), style = MaterialTheme.typography.headlineSmall, textAlign = if (say.isBlank()) TextAlign.Start else TextAlign.Center)
        if (hint.isNotBlank()) Text("💡 $hint", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Выбор из четырёх. У вопроса «на слух» (`listen`) фраза сама звучит при показе, повторить можно кнопкой динамика. */
@Composable
fun ChoiceQuestion(item: QuizItem, speak: Speak?, onDone: (Boolean) -> Unit) {
    val s = LocalStrings.current
    val listen = item.kind == QuizKind.LISTEN
    var picked by remember(item) { mutableStateOf<String?>(null) }
    if (listen) LaunchedEffect(item) { speak?.invoke(item.say) }
    Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(16.dp)) {
        QuestionCard(item.question.ifBlank { if (listen) s.listenPrompt else "" }, speak.takeIf { listen }, item.say, item.who, item.hint)
        ChoiceOptions(item.options, item.answer, picked, { choice -> picked = choice; onDone(choice == item.answer) })
    }
}

/**
 * Произнести фразу вслух: динамик показывает образец, микрофон слушает и оценивает. Вопрос засчитывается, когда совпало не меньше 60%.
 * Если распознавание не работает или не получается, вопрос можно пропустить.
 */
@Composable
fun SpeakQuestion(item: QuizItem, speak: Speak, check: Check?, onDone: (Boolean) -> Unit) {
    val s = LocalStrings.current
    val start = remember(item) { check?.state }
    val now = check?.state
    var finished by remember(item) { mutableStateOf(false) }
    LaunchedEffect(now) {
        val fresh = now != null && now !== start && now.text == item.say && now.phase == Phase.DONE
        if (fresh && !finished && Pronunciation.grade(now!!.score) != Grade.RETRY) { finished = true; onDone(true) }
    }
    Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(16.dp), Alignment.CenterHorizontally) {
        SettingCard(gap = 10.dp) {
            Text(item.question.ifBlank { s.speakPrompt }, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(item.answer, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            if (item.hint.isNotBlank()) Text("💡 ${item.hint}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        PronounceRow(item.say, speak, check)
        if (!finished) OutlineButton(s.skip, { finished = true; onDone(false) })
    }
}

/** Вписать ответ с клавиатуры. С `say` это диктант: фраза звучит, а человек пишет, что услышал. Регистр, знаки препинания и «ё» не важны. */
@Composable
fun FillQuestion(item: QuizItem, speak: Speak?, onDone: (Boolean) -> Unit) {
    val s = LocalStrings.current
    var text by remember(item) { mutableStateOf("") }
    var locked by remember(item) { mutableStateOf(false) }
    val dictation = speak != null && item.say.isNotBlank()
    if (dictation) LaunchedEffect(item) { speak?.invoke(item.say) }
    val submit: () -> Unit = {
        if (!locked && text.isNotBlank()) { locked = true; onDone(Answers.fillOk(item, text)) }
    }
    Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(16.dp)) {
        QuestionCard(item.question.ifBlank { if (dictation) s.dictationPrompt else "" }, speak.takeIf { dictation }, item.say, item.who, item.hint)
        OutlinedTextField(
            text, { text = it }, Modifier.fillMaxWidth(), enabled = !locked, singleLine = true, textStyle = MaterialTheme.typography.titleLarge,
            placeholder = { Text(s.typeAnswer) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, autoCorrect = false, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() })
        )
        if (!locked) GradientButton(s.check, submit)
    }
}
