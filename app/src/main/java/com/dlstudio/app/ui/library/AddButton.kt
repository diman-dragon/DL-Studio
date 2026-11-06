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

/** Кнопка «+» в цвете текущего уровня; `label` читает экранный диктор. */
@Composable
fun AddButton(level: Int, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = LevelLook.colors[level]
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier.size(62.dp)
            .shadow(14.dp, shape, ambientColor = colors.last(), spotColor = colors.last())
            .background(Brush.linearGradient(colors), shape)
            .semantics { contentDescription = label }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { PhIcon(R.drawable.ic_ph_plus, Color.White, 30.dp) }
}
