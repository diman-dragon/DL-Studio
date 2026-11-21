package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.Gain
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.gradientColors
import kotlinx.coroutines.delay

/** Всплывашка «+N XP» (и «Тема пройдена!») на пару секунд после награды. */
@Composable
fun GainToast(gain: Gain?, onDone: () -> Unit, modifier: Modifier = Modifier) {
    LaunchedEffect(gain?.id) {
        if (gain != null) { delay(2000); onDone() }
    }
    if (gain == null) return
    val s = LocalStrings.current
    val shape = RoundedCornerShape(22.dp)
    val text = if (gain.finished) "${s.topicDone}  +${gain.xp} XP" else "+${gain.xp} XP"
    Text(
        text, modifier.shadow(10.dp, shape).clip(shape).background(Brush.linearGradient(LocalAccent.current.gradientColors())).padding(horizontal = 20.dp, vertical = 10.dp),
        Color.White, style = MaterialTheme.typography.titleMedium
    )
}
