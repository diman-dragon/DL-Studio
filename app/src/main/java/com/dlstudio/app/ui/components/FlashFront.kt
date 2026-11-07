package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.study.CardMode
import com.dlstudio.app.ui.theme.LocalStrings

/** Лицевая сторона: слово по центру, подсказка слева, справа динамик и (для иностранного слова) микрофон проверки произношения. */
@Composable
fun FlashFront(card: LibNode, mode: CardMode, hint: Boolean, hintReady: Boolean, listenRevealed: Boolean, onHint: () -> Unit, onSpeak: (String) -> Unit, pronounce: PronounceState) {
    val s = LocalStrings.current
    val isReverse = mode == CardMode.CHOICE_REV || mode == CardMode.WRITE_REV
    val word = if (isReverse) card.back.ifBlank { card.name } else card.name
    val canHint = mode == CardMode.LISTEN || card.hint.isNotBlank()
    val isListenHidden = mode == CardMode.LISTEN && !listenRevealed
    val hintColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
    val soundColors = listOf(Color(0xFF2563EB), Color(0xFF7C3AED))

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().weight(1f), Alignment.Center) {
            if (isListenHidden) Text(s.listenPrompt, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            else CardWord(word, MaterialTheme.colorScheme.onSurface)
        }
        if (hint && card.hint.isNotBlank() && mode != CardMode.LISTEN) CardNote(card.hint, Modifier.padding(bottom = 8.dp))
        if (!isReverse) PronounceFeedback(pronounce)
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Bottom) {
            if (canHint) {
                val hl = mode == CardMode.LISTEN && hintReady && !listenRevealed
                CardActionIcon(R.drawable.ic_lightbulb, onHint, colors = hintColors, highlight = hl, enabled = if (mode == CardMode.LISTEN) hintReady else true)
            } else Spacer(Modifier.size(80.dp))
            if (!isReverse) {
                PronounceButton(pronounce)
                CardActionIcon(R.drawable.ic_volume, { onSpeak(card.name) }, colors = soundColors)
            } else Spacer(Modifier.size(80.dp))
        }
    }
}
