package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalStrings

/** Верхняя строка сессии на компонентах: крестик «закрыть», полоса прогресса и счётчик. */
@Composable
fun SessionHeader(finished: Int, total: Int, onClose: () -> Unit, taskText: String? = null, taskKey: String = "") {
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(14.dp), Alignment.CenterVertically) {
        CloseButton(onClose)
        androidx.compose.foundation.layout.Box(Modifier.weight(1f).height(10.dp), contentAlignment = Alignment.Center) {
            SessionProgress(finished, total)
            if (taskText != null) StudyTaskPopup(taskText, taskKey)
        }
        Text(LocalStrings.current.sessionProgress(finished, total), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
