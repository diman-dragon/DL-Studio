package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.study.Speak
import com.dlstudio.app.ui.theme.LocalStudyOptions

/** Пример (или другая фраза) на изучаемом языке: текст и под ним кнопка озвучки. */
@Composable
fun SpeakNote(text: String, onSpeak: Speak, modifier: Modifier = Modifier, pronounce: PronounceState? = null) {
    Column(modifier.fillMaxWidth(), Arrangement.spacedBy(6.dp), Alignment.CenterHorizontally) {
        CardNote(text)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            SmallSpeaker(text, onSpeak)
            if (pronounce != null) PronounceButton(pronounce, size = 42.dp, enabled = LocalStudyOptions.current.pronounce)
        }
    }
}
