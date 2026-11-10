package com.dlstudio.app.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dlstudio.app.ui.theme.LocalStrings

/** Одна вкладка: иконка и подпись (подпись сама уменьшается, но не переносится и не режется). */
@Composable
fun TabItem(tab: TabSpec, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier.alpha(if (tab.active) 1f else 0.45f).selectable(selected, remember { MutableInteractionSource() }, null, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TabIcon(tab, selected)
        FitText(
            text = tab.label(LocalStrings.current),
            style = MaterialTheme.typography.labelMedium.copy(textAlign = TextAlign.Center),
            modifier = Modifier.padding(top = 4.dp),
            color = if (selected) colors.onBackground else colors.onSurfaceVariant,
            minSize = 9.sp
        )
    }
}
