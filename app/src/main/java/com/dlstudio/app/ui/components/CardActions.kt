package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.study.Speak
import com.dlstudio.app.study.SpeechSpeed

/**
 * Кнопки внизу карточки, расположение всегда одинаковое: нижний ряд — подсказка слева, динамик по центру (в 1,5 раза крупнее),
 * микрофон справа; над ним в шахматном порядке между кнопками — «медленно» (черепаха) и «ещё медленнее» (улитка).
 */
@Composable
fun CardActions(speakText: String?, onSpeak: Speak, pronounce: PronounceState?, onHint: (() -> Unit)?, hintLit: Boolean, hintOn: Boolean) {
    val hintColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
    val soundColors = listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val d = minOf(64.dp, maxWidth / 4)
        val small = d * 0.8f
        Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            if (speakText != null) Box(Modifier.fillMaxWidth().height(small)) {
                val middle = maxWidth / 2
                val left = (d / 2 + middle) / 2
                val right = (maxWidth - d / 2 + middle) / 2
                listOf(SpeechSpeed.SLOW to left, SpeechSpeed.SLOWER to right).forEach { (speed, center) ->
                    CardActionIcon(speed.speedIcon(), { onSpeak(speakText, speed) }, Modifier.offset(x = center - small / 2), colors = soundColors, size = small, iconSize = small * 0.6f)
                }
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                if (onHint != null) CardActionIcon(R.drawable.ic_lightbulb, onHint, colors = hintColors, highlight = hintLit, enabled = hintOn, size = d, iconSize = d * 0.6f)
                else Spacer(Modifier.size(d))
                if (speakText != null) CardActionIcon(SpeechSpeed.NORMAL.speedIcon(), { onSpeak(speakText, SpeechSpeed.NORMAL) }, colors = soundColors, size = d * 1.5f, iconSize = d * 0.9f)
                else Spacer(Modifier.size(d * 1.5f))
                if (pronounce != null) PronounceButton(pronounce, size = d) else Spacer(Modifier.size(d))
            }
        }
    }
}
