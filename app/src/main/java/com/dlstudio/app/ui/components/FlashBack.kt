package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.study.CardMode
import com.dlstudio.app.study.Speak
import com.dlstudio.app.ui.theme.LocalStudyOptions

/** Обратная сторона: перевод (или изучаемое слово) максимально крупно, пояснение (сколько угодно строк, блок под словом прокручивается) и пример с озвучкой; у изучаемого слова — озвучка и микрофон. */
@Composable
fun FlashBack(card: LibNode, mode: CardMode, onSpeak: Speak, pronounce: PronounceState) {
    val text = if (mode.reverse) card.name else card.back.ifBlank { card.name }
    val mic = mode.reverse && !mode.speaks && LocalStudyOptions.current.pronounce
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().weight(1f), Alignment.Center) { CardWord(text, MaterialTheme.colorScheme.onSurface, heard = if (mode.reverse) pronounce.heard else "") }
        Column(Modifier.heightIn(max = 260.dp).verticalScroll(rememberScrollState())) {
            if (card.explanation.isNotBlank()) ExpandableNote(card.explanation, Modifier.padding(bottom = 8.dp))
            if (card.example.isNotBlank()) SpeakNote(card.example, onSpeak, Modifier.padding(bottom = 8.dp), pronounce = if (!mode.reverse && LocalStudyOptions.current.pronounce) pronounce else null)
        }
        if (mic) PronounceFeedback(pronounce)
        if (mode.reverse) CardActions(card.name, onSpeak, pronounce, {}, false, true, mic)
    }
}
