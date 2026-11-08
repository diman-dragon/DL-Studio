package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalStrings

/** Ответ голосом под карточкой: результат («Слушаю…», что услышано, оценка), большой микрофон и кнопка «Показать ответ». */
@Composable
fun SpeakAnswer(pronounce: PronounceState, onReveal: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), Arrangement.spacedBy(0.dp)) {
        PronounceFeedback(pronounce)
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp), Alignment.CenterVertically) {
            PronounceButton(pronounce, Modifier.size(72.dp), 72.dp)
            ChoiceButton(LocalStrings.current.revealAnswer, onReveal, Modifier.weight(1f))
        }
    }
}
