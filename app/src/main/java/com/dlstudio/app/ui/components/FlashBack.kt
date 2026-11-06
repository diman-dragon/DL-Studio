package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.library.LibNode

/** Обратная сторона: перевод, пояснение, пример, озвучка. */
@Composable
fun FlashBack(card: LibNode, onSpeak: (String) -> Unit) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Text(card.back.ifBlank { card.name }, Modifier.fillMaxWidth().padding(top = 36.dp), style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
    Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp), Alignment.CenterHorizontally) {
        if (card.explanation.isNotBlank()) Text(card.explanation, color = muted, textAlign = TextAlign.Center)
        if (card.example.isNotBlank()) Text(card.example, color = muted, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        FixedIconAction(R.drawable.ic_volume, { onSpeak(card.back.ifBlank { card.name }) }, iconTint = MaterialTheme.colorScheme.primary, containerColor = MaterialTheme.colorScheme.primaryContainer)
    }
}
