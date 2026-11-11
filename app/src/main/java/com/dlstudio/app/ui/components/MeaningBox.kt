package com.dlstudio.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dlstudio.app.library.Meaning
import com.dlstudio.app.study.Speak

/** Рамка одного значения: перевод, смысл по-русски, пример на изучаемом языке с кнопкой озвучки и перевод примера; пустые части не показываются. */
@Composable
fun MeaningBox(meaning: Meaning, onSpeak: Speak, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val type = MaterialTheme.typography
    val shape = RoundedCornerShape(18.dp)
    Column(modifier.fillMaxWidth().clip(shape).background(cs.surfaceContainerHighest).border(1.dp, cs.outlineVariant, shape).padding(14.dp), Arrangement.spacedBy(8.dp)) {
        if (meaning.translation.isNotBlank()) Text(meaning.translation.capFirst(), style = type.headlineSmall.copy(fontWeight = FontWeight.SemiBold), color = cs.onSurface)
        if (meaning.explanation.isNotBlank()) ExpandableNote(meaning.explanation)
        if (meaning.example.isNotBlank()) Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp), Alignment.CenterVertically) {
            Text(meaning.example.capFirst(), Modifier.weight(1f), style = type.bodyLarge, color = cs.onSurface)
            SmallSpeaker(meaning.example, onSpeak)
        }
        if (meaning.exampleTranslation.isNotBlank()) Text(meaning.exampleTranslation.capFirst(), style = type.bodyMedium, color = cs.onSurfaceVariant)
    }
}
