package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.study.Speak
import com.dlstudio.app.ui.theme.LocalStrings

/**
 * Нижний ряд кнопок карточки, расположение всегда одинаковое: подсказка слева, динамик по центру (в 1,5 раза крупнее), микрофон справа.
 * Подсказка без текста (`hintOn = false`) остаётся на месте приглушённой.
 */
@Composable
fun CardActions(speakText: String, onSpeak: Speak, pronounce: PronounceState, onHint: () -> Unit, hintLit: Boolean, hintOn: Boolean, pronounceOn: Boolean) {
    val cs = androidx.compose.material3.MaterialTheme.colorScheme
    val hintColors = listOf(cs.tertiary, cs.tertiary.copy(alpha = .75f))
    val soundColors = listOf(cs.primary, cs.primary.copy(alpha = .75f))
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val d = minOf(64.dp, maxWidth / 4)
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            CardActionIcon(R.drawable.ic_lightbulb, onHint, colors = hintColors, highlight = hintLit, enabled = hintOn, size = d, iconSize = d * 0.6f, contentDescription = LocalStrings.current.hintButton)
            CardActionIcon(R.drawable.ic_volume, { onSpeak(speakText) }, colors = soundColors, size = d * 1.5f, iconSize = d * 0.9f, contentDescription = LocalStrings.current.speak)
            PronounceButton(pronounce, size = d, enabled = pronounceOn)
        }
    }
}
