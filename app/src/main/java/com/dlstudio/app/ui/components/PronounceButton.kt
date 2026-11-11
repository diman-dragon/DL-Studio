package com.dlstudio.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.ui.theme.LocalStrings

/** Кнопка микрофона: зелёная, а пока идёт запись — красная с белым кольцом; повторное нажатие заканчивает запись. */
@Composable
fun PronounceButton(state: PronounceState, modifier: Modifier = Modifier, size: Dp = 80.dp, enabled: Boolean = true) {
    val on = state.phase == PronouncePhase.LISTENING
    val cs = androidx.compose.material3.MaterialTheme.colorScheme
    val colors = if (on) listOf(cs.error, cs.error.copy(alpha = .75f)) else listOf(cs.primary, cs.primary.copy(alpha = .75f))
    CardActionIcon(R.drawable.ic_mic, state::toggle, modifier, colors = colors, highlight = on, enabled = enabled, size = size, iconSize = size * 0.6f, contentDescription = LocalStrings.current.pronounceSetting)
}
