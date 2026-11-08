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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Блок подсказки, пояснения или примера: с заглавной буквы, кегль до 24sp подбирается по месту (до 3 строк), слова не рвутся. */
@Composable
fun CardNote(text: String, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp, lineHeight = 30.sp)
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(cs.surfaceContainerHighest).padding(horizontal = 14.dp, vertical = 8.dp), Alignment.Center) {
        BigText(AnnotatedString(text.capFirst()), style, Modifier.fillMaxWidth(), cs.onSurface, maxSize = 24.sp, minSize = 13.sp, maxLines = 3)
    }
}
