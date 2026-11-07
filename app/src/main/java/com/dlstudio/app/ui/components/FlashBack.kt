package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.study.CardMode

/** Обратная сторона карточки: центрированный перевод, пояснения и крупный цветной динамик при иностранном слове. */
@Composable
fun FlashBack(card: LibNode, mode: CardMode, onSpeak: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val isReverse = mode == CardMode.CHOICE_REV || mode == CardMode.WRITE_REV
    val text = if (isReverse) card.name else card.back.ifBlank { card.name }
    val soundColors = listOf(Color(0xFF2563EB), Color(0xFF7C3AED))

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().weight(1f), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CardWord(text, cs.onSurface)
                if (card.explanation.isNotBlank()) CardNote(card.explanation)
            }
        }
        if (card.example.isNotBlank()) CardNote(card.example, Modifier.padding(bottom = 8.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.End, Alignment.Bottom) {
            if (isReverse) CardActionIcon(R.drawable.ic_volume, { onSpeak(card.name) }, colors = soundColors)
            else Spacer(Modifier.size(80.dp))
        }
    }
}
