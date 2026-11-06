package com.dlstudio.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dlstudio.app.library.LibNode

/** Плитка сетки: крупная иконка уровня, меню в углу, название и подпись внизу. */
@Composable
fun ItemTile(node: LibNode, level: Int, actions: ItemActions) {
    Column(
        Modifier.fillMaxWidth().height(184.dp).clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { actions.onOpen(node) }.padding(start = 14.dp, top = 14.dp, end = 4.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
            ItemBadge(level, 52.dp)
            ItemMenu(node, actions)
        }
        ItemText(node, level, Modifier.padding(end = 10.dp))
    }
}
