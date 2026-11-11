package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Главное слово карточки: с заглавной буквы, по центру, максимально крупно в выделенной области (до `maxSize`), слова не рвутся.
 * `fill = false` — текст занимает только свою высоту (в пределах заданной), чтобы под ним можно было поставить транскрипцию.
 * Если передано `heard` (то, что услышано в микрофон), правильно произнесённые слова подсвечиваются зелёным.
 */
@Composable
fun CardWord(text: String, color: Color, modifier: Modifier = Modifier, heard: String = "", maxSize: Float = 72f, fill: Boolean = true) {
    val style = MaterialTheme.typography.displaySmall.copy(
        fontWeight = FontWeight.SemiBold, fontSize = maxSize.sp, lineHeight = (maxSize * 1.15f).sp, letterSpacing = 0.sp
    )
    BigText(SpokenHighlight.build(text.capFirst(), heard, MaterialTheme.colorScheme.primary), style, modifier.then(if (fill) Modifier.fillMaxSize() else Modifier.fillMaxWidth()), color, maxSize = maxSize.sp, minSize = 18.sp, maxLines = 6)
}
