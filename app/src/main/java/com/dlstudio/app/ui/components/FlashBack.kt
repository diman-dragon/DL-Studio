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
import com.dlstudio.app.ui.theme.LocalStudyOptions

/** Обратная сторона: перевод (или изучаемое слово) максимально крупно, пояснение и пример с озвучкой; у изучаемого слова — озвучка и микрофон. */
@Composable
fun FlashBack(card: LibNode, mode: CardMode, onSpeak: Speak, pronounce: PronounceState) {
    val text = if (mode.reverse) card.name else card.back.ifBlank { card.name }
    val mic = mode.reverse && !mode.speaks && LocalStudyOptions.current.pronounce
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().weight(1f), Alignment.Center) { CardWord(text, MaterialTheme.colorScheme.onSurface) }
        if (card.explanation.isNotBlank()) CardNote(card.explanation, Modifier.padding(bottom = 8.dp))
        if (card.example.isNotBlank()) SpeakNote(card.example, onSpeak, Modifier.padding(bottom = 8.dp))
        if (mic) PronounceFeedback(pronounce)
        if (mode.reverse || mode == CardMode.REPEAT) CardActions(card.name, onSpeak, if (mic) pronounce else null, null, false, true)
    }
}
