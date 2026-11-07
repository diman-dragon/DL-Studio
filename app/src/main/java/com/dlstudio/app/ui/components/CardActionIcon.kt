package com.dlstudio.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Крупная функциональная круглая кнопка для карточки (звук в правом углу, подсказка в левом). */
@Composable
fun CardActionIcon(
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    highlight: Boolean = false,
    enabled: Boolean = true,
    size: Dp = 64.dp
) {
    val cs = MaterialTheme.colorScheme
    val targetBg = when {
        !enabled -> cs.surfaceContainerHighest.copy(alpha = 0.35f)
        highlight -> cs.tertiaryContainer
        active -> cs.primaryContainer
        else -> cs.surfaceContainerHigh
    }
    val targetTint = when {
        !enabled -> cs.onSurfaceVariant.copy(alpha = 0.3f)
        highlight -> cs.tertiary
        active -> cs.primary
        else -> cs.onSurfaceVariant
    }
    val bg by animateColorAsState(targetBg, label = "bg")
    val tint by animateColorAsState(targetTint, label = "tint")
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(bg)
            .then(if (highlight) Modifier.border(2.dp, cs.tertiary, CircleShape) else Modifier)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AppIcon(icon, tint, size = (size.value * 0.5f).dp)
    }
}
