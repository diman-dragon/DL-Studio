package com.dlstudio.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalStrings

/** Нижняя панель с цветными значками; нажатие и свайп страниц связаны через `selected`/`onSelect`. */
@Composable
fun BottomTabs(selected: Int, onSelect: (Int) -> Unit) {
    val strings = LocalStrings.current
    NavigationBar(containerColor = MaterialTheme.colorScheme.background, tonalElevation = 0.dp) {
        Tabs.all.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = selected == index,
                onClick = { onSelect(index) },
                icon = { TabIcon(tab, selected == index) },
                label = { Text(tab.label(strings), maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
            )
        }
    }
}
