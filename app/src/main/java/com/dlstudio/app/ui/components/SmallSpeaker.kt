package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.study.Speak
import com.dlstudio.app.study.SpeechSpeed
import com.dlstudio.app.ui.theme.LocalStrings

/** Динамик рядом с любым текстом на изучаемом языке: обычно, медленно и ещё медленнее (значки динамик, черепаха, улитка). */
@Composable
fun SmallSpeaker(text: String, onSpeak: Speak, modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    val s = LocalStrings.current
    Row(modifier, Arrangement.spacedBy(6.dp), Alignment.CenterVertically) {
        SpeechSpeed.values().forEach { speed ->
            ActionIconButton(
                speed.speedIcon(), { onSpeak(text, speed) }, size = 38.dp, iconSize = 22.dp, shape = CircleShape,
                containerColor = c.primaryContainer, iconTint = c.primary, contentDescription = s.speakLabel(speed)
            )
        }
    }
}
