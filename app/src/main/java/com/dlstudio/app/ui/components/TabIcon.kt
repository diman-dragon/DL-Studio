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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/** Иконка вкладки 32dp: активная — залитая, крупнее, на цветной «ауре»; обычная — двухтонная. */
@Composable
fun TabIcon(tab: TabSpec, selected: Boolean) {
    val k by animateFloatAsState(if (selected) 1f else 0f, spring(stiffness = Spring.StiffnessMediumLow), label = "tab")
    val pill = RoundedCornerShape(20.dp)
    Box(
        Modifier.size(width = 68.dp, height = 42.dp).background(Brush.linearGradient(tab.colors), pill, alpha = 0.22f * k),
        contentAlignment = Alignment.Center
    ) {
        GradientIcon(
            res = if (selected) tab.fill else tab.duo,
            colors = tab.colors,
            modifier = Modifier.size(32.dp).graphicsLayer { scaleX = 1f + 0.12f * k; scaleY = 1f + 0.12f * k }
        )
    }
}
