package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.dlstudio.app.ui.theme.DlFonts

/** Главное слово карточки: крупно, по центру, с заглавной буквы; переносится только между словами, без переносов внутри слова. */
@Composable
fun CardWord(text: String, color: Color, modifier: Modifier = Modifier) {
    val style = MaterialTheme.typography.displaySmall.copy(
        fontFamily = DlFonts, fontWeight = FontWeight.SemiBold, fontSize = 38.sp, lineHeight = 44.sp,
        letterSpacing = 0.sp, textAlign = TextAlign.Center, hyphens = Hyphens.None
    )
    FitText(text.capFirst(), style, modifier.fillMaxWidth(), color, minSize = 20.sp, maxLines = 3)
}
