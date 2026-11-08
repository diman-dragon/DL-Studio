package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalStrings

/** Верхняя строка сессии на компонентах: крестик «закрыть», полоса прогресса и счётчик. */
@Composable
fun SessionHeader(finished: Int, total: Int, onClose: () -> Unit) {
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(14.dp), Alignment.CenterVertically) {
        CloseButton(onClose)
        SessionProgress(finished, total, Modifier.weight(1f))
        Text(LocalStrings.current.sessionProgress(finished, total), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
