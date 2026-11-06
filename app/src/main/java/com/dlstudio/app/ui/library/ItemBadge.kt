package com.dlstudio.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/** Градиентная плитка уровня с белой иконкой. */
@Composable
fun ItemBadge(level: Int, size: Dp) {
    val shape = RoundedCornerShape(size * 0.3f)
    Box(
        Modifier.size(size).background(Brush.linearGradient(LevelLook.colors[level]), shape),
        contentAlignment = Alignment.Center
    ) { PhIcon(LevelLook.icons[level], Color.White, size * 0.56f) }
}
