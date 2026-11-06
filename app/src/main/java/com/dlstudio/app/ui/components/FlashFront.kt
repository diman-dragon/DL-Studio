package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.dlstudio.app.ui.theme.LocalStrings

/** Лицевая сторона: слово, подсказка по кнопке, озвучка. */
@Composable
fun FlashFront(card: LibNode, hint: Boolean, onHint: () -> Unit, onSpeak: (String) -> Unit) {
    Text(card.name, Modifier.fillMaxWidth().padding(top = 36.dp), style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
    Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(14.dp), Alignment.CenterHorizontally) {
        if (hint && card.hint.isNotBlank()) Text(card.hint, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Text(LocalStrings.current.tapToFlip, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            FixedIconAction(R.drawable.ic_lightbulb, onHint, card.hint.isNotBlank(), iconTint = MaterialTheme.colorScheme.tertiary, containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            FixedIconAction(R.drawable.ic_volume, { onSpeak(card.name) }, iconTint = MaterialTheme.colorScheme.primary, containerColor = MaterialTheme.colorScheme.primaryContainer)
        }
    }
}
