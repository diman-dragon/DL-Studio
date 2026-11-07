package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Пример (или другая фраза) на изучаемом языке: текст и справа маленький динамик для прослушки. */
@Composable
fun SpeakNote(text: String, onSpeak: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp), Alignment.CenterVertically) {
        CardNote(text, Modifier.weight(1f))
        SmallSpeaker(text, onSpeak)
    }
}
