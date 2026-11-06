package com.dlstudio.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.components.DlAction
import com.dlstudio.app.ui.components.DlModal

/** Подсказка перед импортом: что получится на этом уровне, какой формат нужен и пример; «Выбрать файл» открывает выбор. */
@Composable
fun ImportDialog(level: Int, onPick: () -> Unit, onDismiss: () -> Unit) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    DlModal(s.importTitle(level), onDismiss) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(s.importOutcome(level))
                Text(s.importFormat, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "cat;кот\ndog;собака\nhouse;дом",
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.surfaceVariant).padding(12.dp),
                    fontFamily = FontFamily.Monospace
                )
            }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            DlAction(s.cancel, onDismiss, outlined = true)
            Spacer(Modifier.width(8.dp))
            DlAction(s.importPick, onPick)
        }
    }
}
