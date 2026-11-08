package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dlstudio.app.ui.theme.DlFonts

/**
 * Главное слово карточки: с заглавной буквы, по центру, максимально крупно в выделенной области (до `maxSize`), слова не рвутся.
 * Если передано `heard` (то, что услышано в микрофон), правильно произнесённые слова подсвечиваются зелёным.
 */
@Composable
fun CardWord(text: String, color: Color, modifier: Modifier = Modifier, heard: String = "", maxSize: Float = 72f) {
    val style = MaterialTheme.typography.displaySmall.copy(
        fontFamily = DlFonts, fontWeight = FontWeight.SemiBold, fontSize = maxSize.sp, lineHeight = (maxSize * 1.15f).sp, letterSpacing = 0.sp
    )
    BigText(SpokenHighlight.build(text.capFirst(), heard, Color(0xFF2E9D4F)), style, modifier.fillMaxSize(), color, maxSize = maxSize.sp, minSize = 18.sp, maxLines = 6)
}
