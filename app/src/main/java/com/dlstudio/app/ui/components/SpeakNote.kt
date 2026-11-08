package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.study.Speak

/** Пример (или другая фраза) на изучаемом языке: текст и под ним озвучка — обычно, медленно, ещё медленнее. */
@Composable
fun SpeakNote(text: String, onSpeak: Speak, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp), Alignment.CenterHorizontally) {
        CardNote(text)
        SmallSpeaker(text, onSpeak)
    }
}
