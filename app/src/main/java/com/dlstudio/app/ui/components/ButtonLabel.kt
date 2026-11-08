package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Подпись кнопки: слово с заглавной буквы занимает 90% ширины кнопки (по краям небольшой зазор), кегль подбирается
 * между `maxSize` и `minSize` так, чтобы слово было максимально крупным и не рвалось. Высоту области
 * ограничивает вызывающий через `modifier` (например, `fillMaxHeight(0.9f)`).
 */
@Composable
fun ButtonLabel(text: String, color: Color, modifier: Modifier = Modifier, maxSize: TextUnit = 44.sp, minSize: TextUnit = 14.sp, maxLines: Int = 2) {
    val style = MaterialTheme.typography.titleMedium.copy(fontSize = maxSize, lineHeight = (maxSize.value * 1.15f).sp, fontWeight = FontWeight.SemiBold)
    BigText(AnnotatedString(text.capFirst()), style, modifier.fillMaxWidth(0.9f), color, maxSize, minSize, maxLines)
}
