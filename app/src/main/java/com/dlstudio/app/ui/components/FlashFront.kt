package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.study.CardMode
import com.dlstudio.app.study.Speak
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.LocalStudyOptions

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
