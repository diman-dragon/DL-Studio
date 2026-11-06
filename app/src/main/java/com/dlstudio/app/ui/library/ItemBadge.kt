package com.dlstudio.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.Dp
import com.dlstudio.app.ui.components.AppIcon

/** Нейтральная плитка уровня с одинаковой геометрией в библиотеке и учебе. */
@Composable
fun ItemBadge(level: Int, size: Dp) {
    val shape = RoundedCornerShape(size * 0.3f)
    Box(
        Modifier.size(size).background(MaterialTheme.colorScheme.surface, shape),
        contentAlignment = Alignment.Center
    ) { AppIcon(LevelLook.icons[level], MaterialTheme.colorScheme.primary, size * 0.56f) }
}
