package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.ui.theme.LocalAccent
import com.dlstudio.app.ui.theme.gradientColors

/** Главная кнопка действия в цвете выбранного акцента. */
@Composable
fun GradientButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        label, modifier.clip(RoundedCornerShape(18.dp)).background(Brush.linearGradient(LocalAccent.current.gradientColors()))
            .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 24.dp, vertical = 15.dp),
        color = Color.White, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center
    )
}
