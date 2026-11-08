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
 * Лицевая сторона: слово по центру максимально крупно, внизу подсказка, микрофон проверки и озвучка (медленно / ещё медленнее).
 * В режимах голоса микрофон стоит под карточкой, на лицевой стороне только подсказка («Произношение») или озвучка («Повтор»).
 */
@Composable
fun FlashFront(card: LibNode, mode: CardMode, target: String, hint: Boolean, hintReady: Boolean, listenRevealed: Boolean, onHint: () -> Unit, onSpeak: Speak, pronounce: PronounceState) {
    val s = LocalStrings.current
    val word = when { mode == CardMode.REPEAT -> target; mode.reverse -> card.back.ifBlank { card.name }; else -> card.name }
    val listening = mode == CardMode.LISTEN
    val canHint = listening || (mode != CardMode.REPEAT && card.hint.isNotBlank())
    val mic = !mode.reverse && !mode.speaks && LocalStudyOptions.current.pronounce
    Column(Modifier.fillMaxSize()) {
        TaskBanner(s.taskText(mode), Modifier.padding(bottom = 6.dp))
        Box(Modifier.fillMaxWidth().weight(1f), Alignment.Center) {
            if (listening && !listenRevealed) CardWord(s.listenPrompt, MaterialTheme.colorScheme.onSurfaceVariant) else CardWord(word, MaterialTheme.colorScheme.onSurface)
        }
        if (hint && card.hint.isNotBlank() && !listening) CardNote(card.hint, Modifier.padding(bottom = 8.dp))
        if (mic) PronounceFeedback(pronounce)
        CardActions(
            speakText = if (mode.reverse) null else if (mode == CardMode.REPEAT) target else card.name, onSpeak = onSpeak,
            pronounce = if (mic) pronounce else null, onHint = if (canHint) onHint else null,
            hintLit = listening && hintReady && !listenRevealed, hintOn = !listening || hintReady
        )
    }
}
