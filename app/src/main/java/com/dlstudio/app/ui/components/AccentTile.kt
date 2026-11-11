package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dlstudio.app.settings.AccentColor
import androidx.compose.foundation.BorderStroke

/** Плитка «Акцентный цвет»: ряд образцов и название выбранного цвета под ним. */
@Composable
fun AccentTile(title: String, selected: AccentColor, name: (AccentColor) -> String, onSelect: (AccentColor) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardStyle.shape,
        border = BorderStroke(1.dp, colors.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), Arrangement.spacedBy(12.dp)) {
            FitText(title, MaterialTheme.typography.titleMedium, Modifier.fillMaxWidth(), minSize = 13.sp)
            AccentWheel(selected, onSelect)
            FitText(name(selected), MaterialTheme.typography.bodyMedium, Modifier.fillMaxWidth(), colors.primary, 11.sp)
        }
    }
}
