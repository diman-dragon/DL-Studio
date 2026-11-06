package com.dlstudio.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/** Цветная иконка нижней навигации. Анимация читается только на этапах слоя и рисования — без перекомпоновок на каждый кадр. */
@Composable
fun TabIcon(tab: TabSpec, selected: Boolean) {
    val k by animateFloatAsState(if (selected) 1f else 0f, spring(stiffness = Spring.StiffnessMediumLow), label = "tab")
    val shape = RoundedCornerShape(21.dp)
    val glow = if (selected) Modifier.shadow(14.dp, shape, ambientColor = tab.colors.last(), spotColor = tab.colors.last()) else Modifier
    Box(
        Modifier.size(56.dp)
            .graphicsLayer { val scale = 1f + 0.08f * k; scaleX = scale; scaleY = scale; alpha = 0.82f + 0.18f * k }
            .then(glow)
            .drawBehind { drawRoundRect(Brush.linearGradient(tab.colors), cornerRadius = CornerRadius(21.dp.toPx()), alpha = 0.28f + 0.72f * k) },
        contentAlignment = Alignment.Center
    ) { AppIcon(if (selected) tab.fill else tab.outline, Color.White, 32.dp) }
}
