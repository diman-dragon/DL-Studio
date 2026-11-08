package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
 * Ряд круглых кнопок внизу карточки: подсказка (`onHint`), микрофон (`pronounce`) и озвучка `speakText` в трёх скоростях.
 * Размер кнопок подбирается по ширине, чтобы в ряд всегда помещались пять.
 */
@Composable
fun CardActions(speakText: String?, onSpeak: Speak, pronounce: PronounceState?, onHint: (() -> Unit)?, hintLit: Boolean, hintOn: Boolean) {
    val hintColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
    val soundColors = listOf(Color(0xFF2563EB), Color(0xFF7C3AED))
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val d = minOf(64.dp, (maxWidth - 24.dp) / 5)
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Bottom) {
            if (onHint != null) CardActionIcon(R.drawable.ic_lightbulb, onHint, colors = hintColors, highlight = hintLit, enabled = hintOn, size = d, iconSize = d * 0.6f)
            else Spacer(Modifier.size(d))
            if (pronounce != null) PronounceButton(pronounce, size = d)
            if (speakText != null) Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SpeechSpeed.values().forEach { speed -> CardActionIcon(speed.speedIcon(), { onSpeak(speakText, speed) }, colors = soundColors, size = d, iconSize = d * 0.6f) }
            }
        }
    }
}
