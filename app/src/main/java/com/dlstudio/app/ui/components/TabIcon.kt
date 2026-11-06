package com.dlstudio.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/** Большая цветная иконка нижней навигации: каждая вкладка имеет собственный градиент и свечение. */
@Composable
fun TabIcon(tab: TabSpec, selected: Boolean) {
    val k by animateFloatAsState(if (selected) 1f else 0f, spring(stiffness = Spring.StiffnessMediumLow), label = "tab")
    val shape = RoundedCornerShape(21.dp)
    val gradient = tab.colors.map { if (selected) it else it.copy(alpha = .28f) }
    Box(
        Modifier.size(58.dp)
            .graphicsLayer { scaleX = 1f + 0.08f * k; scaleY = 1f + 0.08f * k; alpha = .82f + .18f * k }
            .shadow((10 + 12 * k).dp, shape, ambientColor = tab.colors.last(), spotColor = tab.colors.last())
            .background(Brush.linearGradient(gradient), shape),
        contentAlignment = Alignment.Center
    ) { AppIcon(if (selected) tab.fill else tab.outline, Color.White, 34.dp) }
}
