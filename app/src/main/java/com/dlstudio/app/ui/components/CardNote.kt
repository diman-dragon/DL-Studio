package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Блок подсказки, пояснения или примера: 18sp с запасом по высоте строки, заглавная буква, без разрезания слов; при нехватке места уменьшается целиком. */
@Composable
fun CardNote(text: String, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val style = MaterialTheme.typography.bodyLarge.copy(
        fontSize = 18.sp, lineHeight = 23.sp, textAlign = TextAlign.Center, hyphens = Hyphens.None, lineBreak = LineBreak.Paragraph
    )
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(cs.surfaceContainerHighest).padding(horizontal = 16.dp, vertical = 10.dp), Alignment.Center) {
        FitText(text.capFirst(), style, Modifier.fillMaxWidth(), cs.onSurface, minSize = 14.sp, maxLines = 4)
    }
}
