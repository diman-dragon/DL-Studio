package com.dlstudio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.study.CardMode
import com.dlstudio.app.ui.theme.LocalStrings

/** Выключенные режимы одной свёрнутой плиткой («Другие режимы · Скоро · 6»): по тапу раскрывается список с неактивными выключателями, настройки остаются короткими. */
@Composable
fun SoonModes() {
    val s = LocalStrings.current
    val cs = MaterialTheme.colorScheme
    val modes = CardMode.studied.toList()
    var open by rememberSaveable { mutableStateOf(false) }
    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), cs.surfaceVariant, border = BorderStroke(1.dp, cs.outlineVariant)) {
        Column {
            Row(Modifier.fillMaxWidth().clickable { open = !open }.padding(horizontal = 20.dp, vertical = 12.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column(Modifier.weight(1f), Arrangement.spacedBy(3.dp)) {
                    Text(s.modesLater, style = MaterialTheme.typography.titleMedium, color = cs.onSurfaceVariant)
                    Text(s.modesSoon(modes.size), style = MaterialTheme.typography.bodyMedium, color = cs.primary)
                }
                Text(if (open) "▴" else "▾", style = MaterialTheme.typography.titleMedium, color = cs.onSurfaceVariant)
            }
            if (open) modes.forEach { SoonModeRow(it) }
        }
    }
}
