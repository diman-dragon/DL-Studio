package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.study.Speak

/** Оборот со значениями: слово сверху мелко (чтобы помнить, о каком речь), под ним рамки значений; три рамки не помещаются — оборот прокручивается. */
@Composable
fun FlipBack(card: LibNode, onSpeak: Speak) {
    Column(Modifier.fillMaxSize()) {
        CardWord(card.name, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.fillMaxWidth().height(44.dp), maxSize = 28f)
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(top = 8.dp), Arrangement.spacedBy(12.dp)) {
            card.meanings.forEach { MeaningBox(it, onSpeak) }
        }
    }
}
