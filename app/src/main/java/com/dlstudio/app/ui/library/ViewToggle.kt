package com.dlstudio.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.ui.theme.LocalStrings

/** Переключатель «Карточки / Список» под заголовком. */
@Composable
fun ViewToggle(grid: Boolean, onChange: (Boolean) -> Unit) {
    val s = LocalStrings.current
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp)
    ) {
        ToggleSegment(s.viewCards, R.drawable.ic_view_grid, grid, { onChange(true) }, Modifier.weight(1f))
        ToggleSegment(s.viewList, R.drawable.ic_view_list, !grid, { onChange(false) }, Modifier.weight(1f))
    }
}
