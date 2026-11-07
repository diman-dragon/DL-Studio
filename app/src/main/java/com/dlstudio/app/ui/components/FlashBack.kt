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
import com.dlstudio.app.ui.theme.LocalStudyOptions

/** Обратная сторона: перевод по центру, пояснения; при иностранном слове — динамик и микрофон проверки произношения. */
@Composable
fun FlashBack(card: LibNode, mode: CardMode, onSpeak: (String) -> Unit, pronounce: PronounceState) {
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
        if (card.example.isNotBlank()) SpeakNote(card.example, onSpeak, Modifier.padding(bottom = 8.dp))
        if (isReverse) PronounceFeedback(pronounce)
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(16.dp, Alignment.End), Alignment.Bottom) {
            if (isReverse) {
                if (LocalStudyOptions.current.pronounce) PronounceButton(pronounce)
                CardActionIcon(R.drawable.ic_volume, { onSpeak(card.name) }, colors = soundColors)
            } else Spacer(Modifier.size(80.dp))
        }
    }
}
