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
import androidx.compose.ui.unit.dp

@Composable
fun FixedIconAction(@DrawableRes icon: Int, onClick: () -> Unit, enabled: Boolean = true, modifier: Modifier = Modifier) {
    Surface(modifier.size(52.dp).clickable(enabled) { onClick() }, RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) { Box(contentAlignment = Alignment.Center) { AppIcon(icon, MaterialTheme.colorScheme.onSurface, 28.dp) } }
}
