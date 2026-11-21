package com.dlstudio.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.dlstudio.app.ui.theme.LocalSpeakSlow
import com.dlstudio.app.ui.theme.LocalStrings

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

