package com.dlstudio.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Пункт выпадающего списка. `badge` — действие справа текстом (например «Скачать»); `onPreview` — кнопка прослушивания справа.
 * Список один и тот же для голосов, языков и пакетов распознавания: так они ведут себя одинаково.
 */
data class PickItem(val key: String, val title: String, val subtitle: String = "", val selected: Boolean = false, val badge: String = "", val onPreview: ((String) -> Unit)? = null, val previewText: String = "")

private const val MENU_MAX_HEIGHT = 420
private const val MENU_MAX_WIDTH = 360

/**
 * Плитка с выпадающим списком: сверху название, под ним текущее значение; тап раскрывает список. Выбор пункта закрывает список и вызывает `onPick(key)`;
 * кнопка прослушивания внутри пункта список не закрывает.
 */
@Composable
fun PickerTile(title: String, value: String, items: List<PickItem>, onPick: (String) -> Unit, modifier: Modifier = Modifier.fillMaxWidth()) {
    val colors = MaterialTheme.colorScheme
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        SettingCard(Modifier.fillMaxWidth(), { open = true }) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column(Modifier.weight(1f), Arrangement.spacedBy(3.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                    Text(value, style = MaterialTheme.typography.bodyMedium, color = colors.primary, maxLines = 1)
                }
                Text("▾", style = MaterialTheme.typography.titleLarge, color = colors.primary)
            }
        }
        DropdownMenu(open, { open = false }, Modifier.widthIn(max = MENU_MAX_WIDTH.dp).heightIn(max = MENU_MAX_HEIGHT.dp)) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                            Text(item.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                            if (item.subtitle.isNotBlank()) Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        }
                    },
                    onClick = { open = false; onPick(item.key) },
                    leadingIcon = { RadioButton(item.selected, null) },
                    trailingIcon = {
                        Row(Modifier.padding(start = 4.dp), Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
                            if (item.badge.isNotBlank()) Text(item.badge, style = MaterialTheme.typography.labelLarge, color = colors.primary)
                            item.onPreview?.let { preview ->
                                Text("🔊", Modifier.clickable { preview(item.previewText) }.padding(6.dp), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                )
            }
        }
    }
}
