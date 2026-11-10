package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.study.CardMode
import com.dlstudio.app.study.Speak
import com.dlstudio.app.ui.theme.LocalStrings
import com.dlstudio.app.ui.theme.LocalStudyOptions

/** Лицо карточки для переворота: слово крупно, под ним транскрипция; внизу прежний ряд кнопок (подсказка, динамик, микрофон), по лампочке — текст подсказки. */
@Composable
fun FlipFront(card: LibNode, hint: Boolean, onHint: () -> Unit, onSpeak: Speak, pronounce: PronounceState) {
    val mic = LocalStudyOptions.current.pronounce
    Column(Modifier.fillMaxSize()) {
        TaskBanner(LocalStrings.current.taskText(CardMode.FLIP_FWD), Modifier.padding(bottom = 6.dp))
        Column(Modifier.fillMaxWidth().weight(1f), Arrangement.Center, Alignment.CenterHorizontally) {
            Box(Modifier.fillMaxWidth().weight(1f)) { CardWord(card.name, MaterialTheme.colorScheme.onSurface, heard = if (mic) pronounce.heard else "") }
            if (card.ipa.isNotBlank()) IpaText(card.ipa, Modifier.padding(bottom = 8.dp))
        }
        if (hint && card.hint.isNotBlank()) CardNote(card.hint, Modifier.padding(bottom = 8.dp))
        if (mic) PronounceFeedback(pronounce)
        CardActions(card.name, onSpeak, if (mic) pronounce else null, if (card.hint.isNotBlank()) onHint else null, false, true)
    }
}
