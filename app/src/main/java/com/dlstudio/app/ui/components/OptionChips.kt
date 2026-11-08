package com.dlstudio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Плитка настройки с выбором из нескольких значений: название и ряд чипов, выбранный подсвечен акцентом. */
@Composable
fun <T> OptionChips(title: String, options: List<Pair<String, T>>, selected: T, onSelect: (T) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(Modifier.fillMaxWidth(), RoundedCornerShape(20.dp), colors.surfaceVariant, border = BorderStroke(1.dp, colors.outlineVariant)) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), Arrangement.spacedBy(10.dp)) {
            FitText(title, MaterialTheme.typography.titleMedium, Modifier.fillMaxWidth(), minSize = 13.sp)
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                options.forEach { (label, value) ->
                    val on = value == selected
                    Surface(
                        Modifier.weight(1f).clickable(role = Role.RadioButton) { onSelect(value) }, RoundedCornerShape(14.dp),
                        if (on) colors.primaryContainer else colors.background, border = BorderStroke(1.dp, if (on) colors.primary else colors.outline)
                    ) {
                        Box(Modifier.height(46.dp), Alignment.Center) {
                            ButtonLabel(label, if (on) colors.primary else colors.onSurfaceVariant, Modifier.fillMaxHeight(0.9f), maxSize = 20.sp, minSize = 10.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}
