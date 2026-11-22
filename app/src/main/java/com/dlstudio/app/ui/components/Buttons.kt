package com.dlstudio.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.speech.Speak
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.gradientColors

/** Главная кнопка действия в цвете выбранного акцента. */
@Composable
fun GradientButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val source = remember { MutableInteractionSource() }
    Box(
        modifier.fillMaxWidth().heightIn(min = 56.dp).pressScale(source).clip(RoundedCornerShape(18.dp)).background(Brush.linearGradient(LocalAccent.current.gradientColors()))
            .clickable(source, null, role = Role.Button, onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) { Text(label, color = Color.White, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center) }
}

/** Второстепенная кнопка: контур акцентного цвета, без заливки. */
@Composable
fun OutlineButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.primary
    val source = remember { MutableInteractionSource() }
    Box(
        modifier.fillMaxWidth().heightIn(min = 56.dp).pressScale(source).clip(RoundedCornerShape(18.dp)).border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            .clickable(source, null, role = Role.Button, onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) { Text(label, color = color, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center) }
}

/** Пока кнопка зажата, она плавно уменьшается до 96% и возвращается обратно. */
@Composable
private fun Modifier.pressScale(source: MutableInteractionSource): Modifier {
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, spring(stiffness = Spring.StiffnessMedium), label = "press")
    return graphicsLayer { scaleX = scale; scaleY = scale }
}

/** Кнопка «Назад» со стрелкой возврата. */
@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val label = LocalStrings.current.previous
    Surface(
        modifier = modifier.size(48.dp).clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = label },
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center) { AppIcon(R.drawable.ic_back, colors.onBackground, 26.dp) }
    }
}

/** Иконка (Material Design Icons) нужного размера и цвета. */
@Composable
fun AppIcon(@DrawableRes res: Int, tint: Color, size: Dp = 24.dp) {
    Icon(painterResource(res), contentDescription = null, modifier = Modifier.size(size), tint = tint)
}

/** Яркая круглая кнопка с иконкой: цветной градиент, свечение, при `pulse` мягко «дышит» (например, пока идёт запись). */
@Composable
fun GlowButton(@DrawableRes icon: Int, colors: List<Color>, onClick: () -> Unit, contentDescription: String, modifier: Modifier = Modifier, pulse: Boolean = false) {
    val beat = if (pulse) rememberInfiniteTransition(label = "pulse").animateFloat(1f, 1.16f, infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "beat") else null
    Box(
        modifier.size(54.dp).graphicsLayer { val k = beat?.value ?: 1f; scaleX = k; scaleY = k }
            .shadow(14.dp, CircleShape, ambientColor = colors.last(), spotColor = colors.last()).clip(CircleShape)
            .background(Brush.linearGradient(colors)).clickable(role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) { AppIcon(icon, Color.White, 28.dp) }
}

/** Кнопка озвучки рядом с текстом на изучаемом языке: цвет акцента. */
@Composable
fun SmallSpeaker(text: String, onSpeak: Speak, modifier: Modifier = Modifier) {
    GlowButton(R.drawable.ic_volume, LocalAccent.current.gradientColors(), { onSpeak(text) }, LocalStrings.current.speak, modifier)
}

/** Варианты ответа сеткой 2×N: выбранный верный — акцентом, выбранный неверный — красным, верный после ответа подсвечивается. */
@Composable
fun ChoiceOptions(options: List<String>, correct: String, selected: String?, onPick: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { option ->
                    ChoiceButton(option, { onPick(option) }, Modifier.weight(1f), selected != null && option == selected, selected != null && option == correct, selected == null)
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ChoiceButton(text: String, onClick: () -> Unit, modifier: Modifier, isChosen: Boolean, isTarget: Boolean, enabled: Boolean) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    val (bg, border) = when {
        isChosen && isTarget -> cs.primary.copy(alpha = 0.22f) to cs.primary
        isChosen -> cs.error.copy(alpha = 0.22f) to cs.error
        isTarget -> cs.primary.copy(alpha = 0.16f) to cs.primary.copy(alpha = 0.7f)
        else -> cs.surfaceContainerHigh to cs.outlineVariant
    }
    Box(
        modifier.heightIn(min = 64.dp).clip(shape).background(bg).border(BorderStroke(1.dp, border), shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick).padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, color = cs.onSurface, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center) }
}
