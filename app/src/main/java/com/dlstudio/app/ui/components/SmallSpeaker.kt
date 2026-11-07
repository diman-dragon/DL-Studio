package com.dlstudio.app.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.ui.theme.LocalStrings

/** Маленький динамик рядом с любым текстом на изучаемом языке: нажатие озвучивает `text` выбранным голосом. */
@Composable
fun SmallSpeaker(text: String, onSpeak: (String) -> Unit, modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    ActionIconButton(
        R.drawable.ic_volume, { onSpeak(text) }, modifier, size = 40.dp, iconSize = 22.dp, shape = CircleShape,
        containerColor = c.primaryContainer, iconTint = c.primary, contentDescription = LocalStrings.current.speak
    )
}
