package com.dlstudio.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
) {
    val alpha by animateFloatAsState(if (enabled) 1f else 0.35f, label = "alpha")
    Box(
        modifier = modifier.size(size).clip(CircleShape)
            .background(Brush.linearGradient(colors.map { it.copy(alpha = alpha) }))
            .then(if (highlight) Modifier.border(3.dp, Color.White, CircleShape) else Modifier)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        AppIcon(icon, Color.White.copy(alpha = alpha), size = iconSize)
    }
}
