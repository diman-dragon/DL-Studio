package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Нижняя панель: четыре крупные градиентные иконки; нажатие и свайп связаны через `selected`/`onSelect`. */
@Composable
fun BottomTabs(selected: Int, onSelect: (Int) -> Unit) {
    Column(Modifier.background(MaterialTheme.colorScheme.background)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
        Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 10.dp)) {
            Tabs.all.forEachIndexed { index, tab ->
                TabItem(tab, selected == index, { onSelect(index) }, Modifier.weight(1f))
            }
        }
    }
}
