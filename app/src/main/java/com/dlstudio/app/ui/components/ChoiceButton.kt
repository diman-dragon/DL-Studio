package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Адаптивная кнопка варианта ответа: текст занимает до 90% ширины и высоты кнопки, отступы по краям сохраняются. */
@Composable
fun ChoiceButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isChosen: Boolean = false,
    isTarget: Boolean = false,
    enabled: Boolean = true
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    val (bg, border) = when {
        isChosen && isTarget -> Color(0xFF2E7D32).copy(alpha = 0.22f) to Color(0xFF2E7D32)
        isChosen -> Color(0xFFC62828).copy(alpha = 0.22f) to Color(0xFFC62828)
        isTarget -> Color(0xFF2E7D32).copy(alpha = 0.16f) to Color(0xFF2E7D32).copy(alpha = 0.7f)
        else -> cs.surfaceContainerHigh to cs.outlineVariant
    }
    Box(
        modifier = modifier.height(64.dp).clip(shape)
            .background(bg).border(1.dp, border, shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { ButtonLabel(text, cs.onSurface, Modifier.fillMaxHeight(0.9f)) }
}
