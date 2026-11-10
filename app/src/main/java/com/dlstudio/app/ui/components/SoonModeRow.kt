package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.study.CardMode
import com.dlstudio.app.ui.theme.LocalStrings

/** Строка выключенного режима: название и неактивный выключатель. */
@Composable
fun SoonModeRow(mode: CardMode) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(LocalStrings.current.modeName(mode), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Switch(false, null, enabled = false)
    }
}
