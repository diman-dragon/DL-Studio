package com.dlstudio.app.ui.library

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dlstudio.app.ui.components.AppIcon
import com.dlstudio.app.ui.components.FitText

/** Одна половина переключателя вида: иконка и подпись; выбранная заливается цветом темы. */
@Composable
fun ToggleSegment(label: String, @DrawableRes icon: Int, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    val bg by animateColorAsState(if (selected) c.primary else Color.Transparent, label = "segment")
    val fg = if (selected) c.onPrimary else c.onSurfaceVariant
    Row(
        modifier.clip(RoundedCornerShape(14.dp)).background(bg)
            .selectable(selected, role = Role.RadioButton, onClick = onClick).padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIcon(icon, fg, 20.dp)
        Spacer(Modifier.width(8.dp))
        FitText(label, MaterialTheme.typography.labelLarge, color = fg, minSize = 11.sp)
    }
}
