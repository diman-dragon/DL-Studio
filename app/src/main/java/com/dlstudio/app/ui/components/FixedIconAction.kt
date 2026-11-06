package com.dlstudio.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FixedIconAction(@DrawableRes icon: Int, onClick: () -> Unit, enabled: Boolean = true, modifier: Modifier = Modifier, iconTint: Color = Color.Unspecified, containerColor: Color = Color.Unspecified) {
    val tint = if (iconTint == Color.Unspecified) MaterialTheme.colorScheme.onSurface else iconTint; val background = if (containerColor == Color.Unspecified) MaterialTheme.colorScheme.surfaceVariant else containerColor
    Surface(modifier.size(60.dp).clickable(enabled) { onClick() }, RoundedCornerShape(18.dp), color = background.copy(alpha = if (enabled) 1f else .48f)) { Box(contentAlignment = Alignment.Center) { AppIcon(icon, tint.copy(alpha = if (enabled) 1f else .55f), 34.dp) } }
}
