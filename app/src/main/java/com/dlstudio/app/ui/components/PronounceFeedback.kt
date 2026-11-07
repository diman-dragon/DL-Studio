package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dlstudio.app.ui.theme.LocalStrings

/** Полоска результата проверки произношения над кнопками: «Слушаю…», что услышано и оценка; в покое не показывается. */
@Composable
fun PronounceFeedback(state: PronounceState, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    val phase = state.phase
    if (phase == PronouncePhase.IDLE) return
    val ok = Color(0xFF2E7D32); val warn = Color(0xFFB45309); val err = Color(0xFFC62828)
    val (text, tint) = when (phase) {
        PronouncePhase.LISTENING -> s.pronounceListening to MaterialTheme.colorScheme.primary
        PronouncePhase.OK -> "👍 ${s.pronounceOk}\n${s.pronounceHeard(state.heard)}" to ok
        PronouncePhase.CLOSE -> "${s.pronounceClose}\n${s.pronounceHeard(state.heard)}" to warn
        PronouncePhase.MISS -> "👎 ${s.pronounceMiss}\n${s.pronounceHeard(state.heard)}" to err
        PronouncePhase.NO_SPEECH -> s.pronounceNoSpeech to warn
        PronouncePhase.UNAVAILABLE -> s.pronounceUnavailable to err
        else -> s.pronounceDenied to err
    }
    val style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 22.sp, textAlign = TextAlign.Center, hyphens = Hyphens.None)
    Box(modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(14.dp)).background(tint.copy(alpha = .16f)).padding(horizontal = 16.dp, vertical = 10.dp), Alignment.Center) {
        FitText(text, style, Modifier.fillMaxWidth(), tint, minSize = 12.sp, maxLines = 3)
    }
}
