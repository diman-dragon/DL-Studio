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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.Role

/** Кнопка варианта ответа: слово с заглавной буквы максимально крупно, до 90% ширины и высоты кнопки, по краям небольшой зазор. */
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
        isChosen && isTarget -> cs.primary.copy(alpha = 0.22f) to cs.primary
        isChosen -> cs.error.copy(alpha = 0.22f) to cs.error
        isTarget -> cs.primary.copy(alpha = 0.16f) to cs.primary.copy(alpha = 0.7f)
        else -> cs.surfaceContainerHigh to cs.outlineVariant
    }
    Box(
        modifier = modifier.height(84.dp).clip(shape)
            .background(bg).border(1.dp, border, shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { ButtonLabel(text, cs.onSurface, Modifier.fillMaxHeight(0.9f)) }
}
