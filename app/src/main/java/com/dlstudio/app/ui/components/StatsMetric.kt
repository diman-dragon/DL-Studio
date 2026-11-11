package com.dlstudio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Плитка показателя: крупное значение (в цвете акцента или своём) и подпись. */
@Composable
fun StatsMetric(label: String, value: String, modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.primary, onClick: () -> Unit = {}) {
    val colors = MaterialTheme.colorScheme
    Surface(modifier.clickable(onClick = onClick), RoundedCornerShape(20.dp), colors.surfaceVariant, border = BorderStroke(1.dp, colors.outlineVariant)) {
        Column(Modifier.padding(14.dp), Arrangement.spacedBy(4.dp)) {
            FitText(value, MaterialTheme.typography.headlineSmall, color = tint, minSize = 16.sp)
            Text(label, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, maxLines = 2)
        }
    }
}
