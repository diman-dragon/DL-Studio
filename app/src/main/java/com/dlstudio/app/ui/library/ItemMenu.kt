package com.dlstudio.app.ui.library

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.ui.components.ActionIconButton
import com.dlstudio.app.ui.theme.LocalStrings

/** Всегда видимые действия карточки через компонент ActionIconButton: карандаш и корзина. */
@Composable
fun ItemMenu(node: LibNode, actions: ItemActions) {
    val s = LocalStrings.current
    val c = MaterialTheme.colorScheme
    Row {
        ActionIconButton(
            icon = R.drawable.ic_edit,
            onClick = { actions.onEdit(node) },
            size = 42.dp,
            iconSize = 20.dp,
            containerColor = c.surfaceVariant,
            iconTint = c.primary,
            contentDescription = s.edit
        )
        ActionIconButton(
            icon = R.drawable.ic_trash,
            onClick = { actions.onDelete(node) },
            size = 42.dp,
            iconSize = 20.dp,
            containerColor = c.surfaceVariant,
            iconTint = c.error,
            contentDescription = s.delete
        )
    }
}
