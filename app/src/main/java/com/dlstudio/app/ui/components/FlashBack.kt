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

/** Обратная сторона карточки: центрированный перевод, пояснения и динамик если здесь иностранное слово. */
@Composable
fun FlashBack(card: LibNode, mode: CardMode, onSpeak: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val isReverse = mode == CardMode.CHOICE_REV || mode == CardMode.WRITE_REV
    val text = if (isReverse) card.name else card.back.ifBlank { card.name }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().weight(1f), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FitText(text, MaterialTheme.typography.displayMedium.copy(textAlign = TextAlign.Center), color = cs.onSurface, minSize = 22.sp, maxLines = 2)
                if (card.explanation.isNotBlank()) Text(card.explanation, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
        if (card.example.isNotBlank()) {
            Box(Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(12.dp)).background(cs.surfaceContainerHighest).padding(horizontal = 14.dp, vertical = 8.dp), Alignment.Center) {
                Text(card.example, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
        Row(Modifier.fillMaxWidth(), Arrangement.End, Alignment.Bottom) {
            if (isReverse) CardActionIcon(R.drawable.ic_volume, { onSpeak(card.name) })
            else Spacer(Modifier.size(64.dp))
        }
    }
}
