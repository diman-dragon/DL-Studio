package com.dlstudio.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Компонентная кнопка действия с иконкой в скруглённом контейнере. */
@Composable
fun ActionIconButton(
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 42.dp,
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
