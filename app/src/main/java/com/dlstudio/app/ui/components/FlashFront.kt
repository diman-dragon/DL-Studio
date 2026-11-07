package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dlstudio.app.R
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.study.CardMode
import com.dlstudio.app.ui.theme.LocalStrings

/** Лицевая сторона: центрированное слово, подсказка в левом нижнем углу, динамик в правом. */
@Composable
fun FlashFront(
    card: LibNode,
    mode: CardMode,
    hint: Boolean,
    hintReady: Boolean,
    listenRevealed: Boolean,
    onHint: () -> Unit,
    onSpeak: (String) -> Unit
) {
    val s = LocalStrings.current
    val isReverse = mode == CardMode.CHOICE_REV || mode == CardMode.WRITE_REV
    val word = if (isReverse) card.back.ifBlank { card.name } else card.name
    val canHint = mode == CardMode.LISTEN || card.hint.isNotBlank()
    val isListenHidden = mode == CardMode.LISTEN && !listenRevealed

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().weight(1f), Alignment.Center) {
            if (isListenHidden) Text(s.listenPrompt, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            else FitText(word, MaterialTheme.typography.displayMedium.copy(textAlign = TextAlign.Center), color = MaterialTheme.colorScheme.onSurface, minSize = 22.sp, maxLines = 2)
        }
        if (hint && card.hint.isNotBlank() && mode != CardMode.LISTEN) {
            Box(Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceContainerHighest).padding(horizontal = 14.dp, vertical = 8.dp), Alignment.Center) {
                Text(card.hint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Bottom) {
            if (canHint) {
                val hl = mode == CardMode.LISTEN && hintReady && !listenRevealed
                CardActionIcon(R.drawable.ic_lightbulb, onHint, active = hint || listenRevealed, highlight = hl, enabled = if (mode == CardMode.LISTEN) hintReady else true)
            } else Spacer(Modifier.size(64.dp))
            if (!isReverse) CardActionIcon(R.drawable.ic_volume, { onSpeak(card.name) })
            else Spacer(Modifier.size(64.dp))
        }
    }
}
