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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Плашка с заданием наверху карточки («Скажите слово на изучаемом языке», «Выберите перевод»…) на языке интерфейса. */
@Composable
fun TaskBanner(text: String, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(cs.primaryContainer).padding(horizontal = 12.dp, vertical = 8.dp), Alignment.Center) {
        FitText(text.capFirst(), MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center), Modifier.fillMaxWidth(), cs.onPrimaryContainer, 11.sp, 2)
    }
}
