package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.ui.theme.gradientColors

/** Круглый образец акцента: градиент цвета; выбранный обведён и отмечен галочкой; `label` читает диктор. */
@Composable
fun AccentSwatch(accent: AccentColor, selected: Boolean, label: String, onClick: () -> Unit) {
    val ring = if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier
    Box(
        Modifier.size(34.dp).clip(CircleShape).background(Brush.linearGradient(accent.gradientColors()))
            .then(ring)
            .semantics { contentDescription = label }
            .selectable(selected, role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { if (selected) AppIcon(R.drawable.ic_check, Color.White, 20.dp) }
}
