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
import com.dlstudio.app.ui.components.AppIcon

/** Градиентная плитка уровня (проект, колода, карточка) с одинаковой геометрией в библиотеке и учёбе. */
@Composable
fun ItemBadge(level: Int, size: Dp) {
    Box(
        Modifier.size(size).background(Brush.linearGradient(LevelLook.colors[level]), RoundedCornerShape(size * 0.3f)),
        contentAlignment = Alignment.Center
    ) { AppIcon(LevelLook.icons[level], Color.White, size * 0.56f) }
}
