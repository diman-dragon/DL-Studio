package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.dlstudio.app.ui.theme.DlFonts

/** Главное слово карточки: с заглавной буквы, по центру, максимально крупно в выделенной области (до 72sp), слова не рвутся. */
@Composable
fun CardWord(text: String, color: Color, modifier: Modifier = Modifier) {
    val style = MaterialTheme.typography.displaySmall.copy(
        fontFamily = DlFonts, fontWeight = FontWeight.SemiBold, fontSize = 72.sp, lineHeight = 83.sp, letterSpacing = 0.sp
    )
    BigText(text.capFirst(), style, modifier.fillMaxSize(), color, maxSize = 72.sp, minSize = 18.sp, maxLines = 6)
}
