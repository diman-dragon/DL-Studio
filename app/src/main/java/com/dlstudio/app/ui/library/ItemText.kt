package com.dlstudio.app.ui.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.ui.components.FitText
import com.dlstudio.app.ui.theme.LocalStrings

/** Название элемента и подпись: для проекта и колоды — сколько внутри, для карточки — обратная сторона. */
@Composable
fun ItemText(node: LibNode, level: Int, modifier: Modifier = Modifier) {
    val sub = if (level == 2) node.back else LocalStrings.current.contentCount(level, node.children.size)
    Column(modifier) {
        FitText(node.name, MaterialTheme.typography.titleMedium, Modifier.fillMaxWidth(), minSize = 12.sp, maxLines = 2)
        FitText(
            sub, MaterialTheme.typography.bodyMedium, Modifier.fillMaxWidth().padding(top = 2.dp),
            MaterialTheme.colorScheme.onSurfaceVariant, 11.sp, 2
        )
    }
}
