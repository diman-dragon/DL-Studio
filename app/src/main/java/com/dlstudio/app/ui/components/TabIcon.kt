package com.dlstudio.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/** Цветной значок вкладки: градиентная плитка с белым рисунком; выбранный крупнее и ярче. */
@Composable
fun TabIcon(tab: TabSpec, selected: Boolean) {
    val scale by animateFloatAsState(if (selected) 1.15f else 1f, label = "tabScale")
    val alpha by animateFloatAsState(if (selected) 1f else 0.6f, label = "tabAlpha")
    Canvas(Modifier.size(34.dp).graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }) {
        drawRoundRect(brush = Brush.linearGradient(tab.colors), cornerRadius = CornerRadius(size.width * 0.3f))
        tab.glyph(this, Color.White)
    }
}
