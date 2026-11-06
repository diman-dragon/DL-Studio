package com.dlstudio.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Нижняя панель: четыре градиентные иконки в «капсуле» с тонким контуром. */
@Composable
fun BottomTabs(selected: Int, onSelect: (Int) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Surface(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), RoundedCornerShape(30.dp),
            MaterialTheme.colorScheme.surfaceVariant, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(Modifier.padding(6.dp)) {
                Tabs.all.forEachIndexed { index, tab -> TabItem(tab, selected == index, { onSelect(index) }, Modifier.weight(1f)) }
            }
        }
    }
}
