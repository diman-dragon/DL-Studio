package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.study.Speak
import com.dlstudio.app.ui.theme.LocalStudyOptions

/**
 * Лицо карточки (слово → перевод): слово по центру максимально крупно (слово, пословица или предложение сами уменьшаются),
 * сразу под ним транскрипция, ниже по кнопке подсказки — подсказка на изучаемом языке (`LibNode.hintText`). Без плашек с заданием.
 * Нижний ряд всегда один: подсказка (приглушена, если текста нет), динамик, микрофон. Высота — какую даст родитель.
 */
@Composable
fun FlipFront(card: LibNode, hint: Boolean, onHint: () -> Unit, onSpeak: Speak, pronounce: PronounceState) {
    val mic = LocalStudyOptions.current.pronounce
    val note = card.hintText
    val shown = hint && note.isNotBlank()
    Column(Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f), Alignment.Center) {
            val spare = (if (card.ipa.isNotBlank()) 40 else 0) + (if (shown) 120 else 0)
            val room = maxOf(48.dp, maxHeight - spare.dp)   // maxHeight читается здесь: внутри Column он недоступен (другой получатель)
            Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(4.dp), Alignment.CenterHorizontally) {
                CardWord(card.name, MaterialTheme.colorScheme.onSurface, Modifier.heightIn(max = room), if (mic) pronounce.heard else "", fill = false)
                if (card.ipa.isNotBlank()) IpaText(card.ipa)
                if (shown) CardNote(note, Modifier.padding(top = 10.dp))
            }
        }
        if (mic) PronounceFeedback(pronounce)
        CardActions(card.name, onSpeak, if (mic) pronounce else null, onHint, false, note.isNotBlank())
    }
}
