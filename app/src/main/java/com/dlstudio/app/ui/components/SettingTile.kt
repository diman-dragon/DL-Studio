package com.dlstudio.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke

/** Плитка настроек: название сверху, значение под ним (после «/» разрешён перенос); тап — действие. */
@Composable
fun SettingTile(title: String, value: String, onClick: () -> Unit, enabled: Boolean = true) {
    val shape = RoundedCornerShape(20.dp)
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth().clip(shape).clickable(enabled = enabled, onClick = onClick),
        shape = shape,
        border = BorderStroke(1.dp, colors.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 13.dp), Arrangement.spacedBy(3.dp)) {
            FitText(title, MaterialTheme.typography.titleMedium, Modifier.fillMaxWidth(), minSize = 13.sp)
            FitText(value.replace("/", "/​"), MaterialTheme.typography.bodyMedium, Modifier.fillMaxWidth(), colors.primary, 11.sp, 2)
        }
    }
}
