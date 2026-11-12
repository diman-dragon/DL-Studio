package com.dlstudio.app.ui.components

import android.os.LocaleList
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doAfterTextChanged
import com.dlstudio.app.R
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.library.Meaning
import com.dlstudio.app.settings.FontGroup
import com.dlstudio.app.study.PronounceCheck
import com.dlstudio.app.study.Speak
import com.dlstudio.app.ui.theme.LocalFonts
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.LocalStudyOptions
import com.dlstudio.app.ui.theme.TextRole
import com.dlstudio.app.ui.theme.face
import com.dlstudio.app.ui.theme.style

/**
 * Лицо карточки (слово → перевод): слово по центру максимально крупно (слово, пословица или предложение сами уменьшаются),
 * сразу под ним транскрипция, ниже по кнопке подсказки — подсказка на изучаемом языке (`LibNode.hintText`). Без плашек с заданием.
 * Нижний ряд всегда один: подсказка (приглушена, если текста нет), динамик, микрофон. Высота — какую даст родитель.
 */
@Composable
fun FlipFront(card: LibNode, hint: Boolean, onHint: () -> Unit, onSpeak: Speak, pronounce: PronounceState) {
    val mic = LocalStudyOptions.current.pronounce
    val note = card.hintText
    val shown = hint && note.isNotBlank()
    Column(Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f), Alignment.Center) {
            val spare = (if (card.ipa.isNotBlank()) 40 else 0) + (if (shown) 120 else 0)
            val room = maxOf(48.dp, maxHeight - spare.dp)   // maxHeight читается здесь: внутри Column он недоступен (другой получатель)
            Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(4.dp), Alignment.CenterHorizontally) {
                CardWord(card.name, MaterialTheme.colorScheme.onSurface, Modifier.heightIn(max = room), if (mic) pronounce.heard else "", fill = false)
                if (card.ipa.isNotBlank()) IpaText(card.ipa)
                if (shown) CardNote(note, Modifier.padding(top = 10.dp))
            }
        }
        if (mic) PronounceFeedback(pronounce)
        CardActions(card.name, onSpeak, pronounce, onHint, false, note.isNotBlank(), mic)
    }
}

/** Оборот со значениями: слово сверху мелко (чтобы помнить, о каком речь), под ним рамки значений; три рамки не помещаются — оборот прокручивается. */
@Composable
fun FlipBack(card: LibNode, onSpeak: Speak) {
    Column(Modifier.fillMaxSize()) {
        CardWord(card.name, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.fillMaxWidth().height(44.dp), role = TextRole.CARD_MINI)
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(top = 8.dp), Arrangement.spacedBy(12.dp)) {
            card.meanings.forEach { MeaningBox(it, onSpeak) }
        }
    }
}

/** Рамка одного значения: перевод, смысл по-русски, пример на изучаемом языке с кнопкой озвучки и перевод примера; пустые части не показываются. */
@Composable
fun MeaningBox(meaning: Meaning, onSpeak: Speak, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val type = MaterialTheme.typography
    val shape = RoundedCornerShape(18.dp)
    Column(modifier.fillMaxWidth().clip(shape).background(cs.surfaceContainerHighest).border(1.dp, cs.outlineVariant, shape).padding(14.dp), Arrangement.spacedBy(8.dp)) {
        if (meaning.translation.isNotBlank()) GroupText(meaning.translation.capFirst(), FontGroup.CARD, color = cs.onSurface, style = type.headlineSmall)
        if (meaning.explanation.isNotBlank()) ExpandableNote(meaning.explanation)
        if (meaning.example.isNotBlank()) Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp), Alignment.CenterVertically) {
            GroupText(meaning.example.capFirst(), FontGroup.NOTE, Modifier.weight(1f), cs.onSurface, type.bodyLarge)
            SmallSpeaker(meaning.example, onSpeak, slow = true)
        }
        if (meaning.exampleTranslation.isNotBlank()) GroupText(meaning.exampleTranslation.capFirst(), FontGroup.NOTE, color = cs.onSurfaceVariant, style = type.bodyMedium)
    }
}

/** Пример (или другая фраза) на изучаемом языке: текст и под ним кнопка озвучки. */
@Composable
fun SpeakNote(text: String, onSpeak: Speak, modifier: Modifier = Modifier, pronounce: PronounceState? = null) {
    Column(modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp), Alignment.CenterHorizontally) {
        CardNote(text)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            SmallSpeaker(text, onSpeak, slow = true)
            if (pronounce != null) PronounceButton(pronounce, size = 42.dp, enabled = LocalStudyOptions.current.pronounce)
        }
    }
}

/** Общий стиль контурных карточек приложения. */
object CardStyle {
    val shape = RoundedCornerShape(24.dp)
}

/** Поле ввода с системным переключением раскладки клавиатуры через imeHintLocales. */
@Composable
fun CardWriteField(value: String, onValueChange: (String) -> Unit, placeholder: String, isRussian: Boolean, enabled: Boolean, borderColor: Color, onDone: () -> Unit, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme; val shape = RoundedCornerShape(16.dp)
    val size = TextRole.CARD_INPUT.style().fontSize.value
    val face = LocalFonts.current[FontGroup.CARD].choice.face()
    Box(modifier.height(54.dp).clip(shape).background(cs.surface).border(1.5.dp, borderColor, shape).padding(horizontal = 14.dp), Alignment.CenterStart) {
        AndroidView(modifier = Modifier.fillMaxWidth(), factory = { ctx: android.content.Context ->
            EditText(ctx).apply {
                background = null; isSingleLine = true; imeOptions = EditorInfo.IME_ACTION_DONE
                imeHintLocales = LocaleList.forLanguageTags(if (isRussian) "ru" else "en")
                setTextColor(cs.onSurface.toArgb()); setHintTextColor(cs.onSurfaceVariant.toArgb())
                doAfterTextChanged { if (it != null && it.toString() != value) onValueChange(it.toString()) }
                setOnEditorActionListener { _, a, _ -> if (a == EditorInfo.IME_ACTION_DONE) { onDone(); true } else false }
            }
        }, update = { view: EditText ->
            view.isEnabled = enabled; view.hint = placeholder
            view.textSize = size; view.typeface = face
            view.imeHintLocales = LocaleList.forLanguageTags(if (isRussian) "ru" else "en")
            if (view.text.toString() != value) view.setText(value)
        })
    }
}

/** Блок ввода ответа с авто-раскладкой, кнопкой проверки и реакцией 👍 / 👎; регистр и знаки не важны; для слова на изучаемом языке (`onSpeak`) — динамик. */
@Composable
fun CardWriteInput(correct: String, submitted: Boolean, onAnswer: (Boolean) -> Unit, modifier: Modifier = Modifier, onSpeak: Speak? = null) {
    val s = LocalStrings.current
    var text by remember { mutableStateOf("") }
    val isCorrect = PronounceCheck.normalize(text) == PronounceCheck.normalize(correct)
    val cs = MaterialTheme.colorScheme
    val ok = cs.primary; val err = cs.error
    val btnColor = if (submitted) (if (isCorrect) ok else err) else cs.primary
    val isRu = correct.any { it in 'а'..'я' || it in 'А'..'Я' || it == 'ё' || it == 'Ё' }
    val borderColor = if (submitted) btnColor else cs.outlineVariant
    fun submit() { if (!submitted && text.isNotBlank()) onAnswer(isCorrect) }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp), Alignment.CenterVertically) {
            CardWriteField(
                value = text, onValueChange = { if (!submitted) text = it },
                placeholder = s.writePrompt, isRussian = isRu,
                enabled = !submitted, borderColor = borderColor, onDone = ::submit,
                modifier = Modifier.weight(1f)
            )
            ActionIconButton(R.drawable.ic_check, ::submit, enabled = !submitted && text.isNotBlank(), size = 54.dp, shape = RoundedCornerShape(16.dp), containerColor = btnColor, iconTint = Color.White)
        }
        if (submitted) Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(if (isCorrect) ok.copy(alpha = .16f) else err.copy(alpha = .16f)).padding(horizontal = 14.dp, vertical = 8.dp),
            Arrangement.spacedBy(6.dp)
        ) {
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp), Alignment.CenterVertically) {
                EmojiText(if (isCorrect) "👍" else "👎")
                FitText(if (isCorrect) s.writeRight else s.writeWrong(correct), MaterialTheme.typography.titleMedium, Modifier.weight(1f), if (isCorrect) ok else err, 2)
            }
            if (onSpeak != null) SmallSpeaker(correct, onSpeak)
        }
    }
}
