package com.dlstudio.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.dlstudio.app.R

/** Кнопка микрофона: зелёная, а пока идёт запись — красная с белым кольцом; повторное нажатие заканчивает запись. */
@Composable
fun PronounceButton(state: PronounceState, modifier: Modifier = Modifier) {
    val on = state.phase == PronouncePhase.LISTENING
    val colors = if (on) listOf(Color(0xFFDC2626), Color(0xFFEF4444)) else listOf(Color(0xFF059669), Color(0xFF10B981))
    CardActionIcon(R.drawable.ic_mic, state::toggle, modifier, colors = colors, highlight = on)
}
