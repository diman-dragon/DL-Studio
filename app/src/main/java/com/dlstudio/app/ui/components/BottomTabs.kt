package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Нижняя панель: четыре крупные градиентные иконки; нажатие и свайп связаны через `selected`/`onSelect`. */
@Composable
fun BottomTabs(selected: Int, onSelect: (Int) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background, tonalElevation = 8.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(28.dp)).padding(5.dp)) {
            Tabs.all.forEachIndexed { index, tab ->
                TabItem(tab, selected == index, { onSelect(index) }, Modifier.weight(1f))
            }
        }
    }
}
