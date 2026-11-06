package com.dlstudio.app.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.ui.components.AppIcon
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.gradientColors

/** Кнопка «+» в цвете выбранного акцента; `label` читает экранный диктор. */
@Composable
fun AddButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalAccent.current.gradientColors()
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier.size(62.dp)
            .shadow(14.dp, shape, ambientColor = colors.last(), spotColor = colors.last())
            .background(Brush.linearGradient(colors), shape)
            .semantics { contentDescription = label }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { AppIcon(R.drawable.ic_plus, Color.White, 30.dp) }
}
