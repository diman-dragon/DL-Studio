package com.dlstudio.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.study.Speak
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.LocalSpeakSlow
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.TextRole
import com.dlstudio.app.ui.theme.gradientColors

/** Главная кнопка действия в цвете выбранного акцента: на всю ширину, текст — до 90% ширины и высоты. */
@Composable
fun GradientButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(18.dp)).background(Brush.linearGradient(LocalAccent.current.gradientColors()))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { ButtonLabel(label, Color.White, Modifier.fillMaxHeight(0.9f), role = TextRole.BUTTON_BIG, maxLines = 1) }
}

/** Компонентная кнопка «Назад» со стрелкой возврата. */
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

/** Кнопка-крестик «закрыть» (выход из сессии). */
@Composable
fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val label = LocalStrings.current.close
    Surface(modifier.size(48.dp).clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = label }, RoundedCornerShape(14.dp), colors.surfaceVariant) {
        Box(contentAlignment = Alignment.Center) { AppIcon(R.drawable.ic_close, colors.onBackground, 24.dp) }
    }
}

/** Компонентная кнопка действия с иконкой в скруглённом контейнере. */
@Composable
fun ActionIconButton(
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 48.dp,
    iconSize: Dp? = null,
    shape: Shape = RoundedCornerShape(13.dp),
    containerColor: Color = Color.Unspecified,
    iconTint: Color = Color.Unspecified,
    contentDescription: String? = null
) {
    val cs = MaterialTheme.colorScheme
    val bg = if (containerColor == Color.Unspecified) cs.surfaceVariant else containerColor
    val tint = if (iconTint == Color.Unspecified) cs.onSurfaceVariant else iconTint
    val actualIconSize = iconSize ?: (size.value * 0.48f).dp
    Box(
        modifier = modifier.size(size).clip(shape).background(bg)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .then(if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        AppIcon(icon, tint, actualIconSize)
    }
}

/** Крупная цветная круглая кнопка действия карточки (в 3 раза крупнее, градиентная). */
@Composable
fun CardActionIcon(
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(Color(0xFF2563EB), Color(0xFF7C3AED)),
    highlight: Boolean = false,
    enabled: Boolean = true,
    size: Dp = 80.dp,
    iconSize: Dp = 48.dp
    , contentDescription: String? = null
) {
    val alpha by animateFloatAsState(if (enabled) 1f else 0.35f, label = "alpha")
    Box(
        modifier = modifier.size(size).clip(CircleShape)
            .background(Brush.linearGradient(colors.map { it.copy(alpha = alpha) }))
            .then(if (highlight) Modifier.border(3.dp, Color.White, CircleShape) else Modifier)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .then(if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        AppIcon(icon, Color.White.copy(alpha = alpha), size = iconSize)
    }
}

/** Иконка (Material Design Icons) нужного размера и цвета. */
@Composable
fun AppIcon(@DrawableRes res: Int, tint: Color, size: Dp = 24.dp) {
    Icon(painterResource(res), contentDescription = null, modifier = Modifier.size(size), tint = tint)
}

/** Кнопка озвучки рядом с любым текстом на изучаемом языке; `slow` добавляет рядом «черепашку» (для фраз и примеров). */
@Composable
fun SmallSpeaker(text: String, onSpeak: Speak, modifier: Modifier = Modifier, slow: Boolean = false) {
    val c = MaterialTheme.colorScheme
    Row(modifier, Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
        ActionIconButton(
            R.drawable.ic_volume, { onSpeak(text) }, size = 38.dp, iconSize = 22.dp, shape = CircleShape,
            containerColor = c.primaryContainer, iconTint = c.primary, contentDescription = LocalStrings.current.speak
        )
        if (slow) SlowButton(text)
    }
}

/** Значок-черепаха: произнести текст помедленнее (темп и паузы — из настроек). Ставится рядом с динамиком. */
@Composable
fun SlowButton(text: String, modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    val speak = LocalSpeakSlow.current
    ActionIconButton(
        R.drawable.ic_turtle, { speak(text) }, modifier, size = 38.dp, iconSize = 24.dp, shape = CircleShape,
        containerColor = c.primaryContainer, iconTint = c.primary, contentDescription = LocalStrings.current.speakSlow
    )
}

/** Кнопка варианта ответа: слово с заглавной буквы максимально крупно, до 90% ширины и высоты кнопки, по краям небольшой зазор. */
@Composable
fun ChoiceButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isChosen: Boolean = false,
    isTarget: Boolean = false,
    enabled: Boolean = true
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    val (bg, border) = when {
        isChosen && isTarget -> cs.primary.copy(alpha = 0.22f) to cs.primary
        isChosen -> cs.error.copy(alpha = 0.22f) to cs.error
        isTarget -> cs.primary.copy(alpha = 0.16f) to cs.primary.copy(alpha = 0.7f)
        else -> cs.surfaceContainerHigh to cs.outlineVariant
    }
    Box(
        modifier = modifier.height(84.dp).clip(shape)
            .background(bg).border(1.dp, border, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { ButtonLabel(text, cs.onSurface, Modifier.fillMaxHeight(0.9f)) }
}

/** 4 варианта ответа в виде сетки 2x2 под карточкой с использованием компонента ChoiceButton. */
@Composable
fun ChoiceOptions(options: List<String>, correct: String, selected: String?, onPick: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { opt ->
                    ChoiceButton(
                        text = opt,
                        onClick = { onPick(opt) },
                        modifier = Modifier.weight(1f),
                        isChosen = selected != null && opt == selected,
                        isTarget = selected != null && opt == correct,
                        enabled = selected == null
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
