package com.dlstudio.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/** Единая плитка настроек: название слева, текущее значение справа; тап — следующее значение или действие. */
@Composable
fun SettingTile(title: String, value: String, onClick: () -> Unit, enabled: Boolean = true) {
    val shape = RoundedCornerShape(18.dp)
    Card(
        modifier = Modifier.fillMaxWidth().clip(shape).clickable(enabled = enabled, onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Text(value, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
        }
    }
}
