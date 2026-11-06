package com.dlstudio.app.ui.library

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.ui.theme.LocalStrings

/** Кнопка «⋮» с меню «Изменить / Удалить». */
@Composable
fun ItemMenu(node: LibNode, actions: ItemActions) {
    val s = LocalStrings.current
    val c = MaterialTheme.colorScheme
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) { PhIcon(R.drawable.ic_ph_dots_three_vertical, c.onSurfaceVariant, 22.dp) }
        DropdownMenu(open, { open = false }) {
            DropdownMenuItem(
                text = { Text(s.edit) },
                leadingIcon = { PhIcon(R.drawable.ic_ph_pencil_simple, c.onSurface, 20.dp) },
                onClick = { open = false; actions.onEdit(node) }
            )
            DropdownMenuItem(
                text = { Text(s.delete, color = c.error) },
                leadingIcon = { PhIcon(R.drawable.ic_ph_trash, c.error, 20.dp) },
                onClick = { open = false; actions.onDelete(node) }
            )
        }
    }
}
