package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dlstudio.app.i18n.StringsRu
import com.dlstudio.app.speech.CharacterProfile
import com.dlstudio.app.speech.Characters
import com.dlstudio.app.speech.SpeechProblem
import com.dlstudio.app.speech.VoiceNames
import com.dlstudio.app.ui.theme.LocalStrings

/** Приветствие персонажа на языке темы `lang` («en», «ru»…): так он представляется, когда ученик нажимает на портрет. */
fun hello(id: String, lang: String): String =
    if (lang == "ru") "Здравствуйте! Меня зовут ${StringsRu.characterName(id)}." else "Hello! My name is ${Characters.of(id).name}."

/** Карточка персонажа: портрет (нажмите — поздоровается своим голосом), имя, роль, характер и откуда у него голос (`voiceNote`; null — пока неизвестно). */
@Composable
fun CharacterCard(id: String, voiceNote: String?, onGreet: (() -> Unit)?) {
    val s = LocalStrings.current
    SettingCard {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            CharacterAvatar(id, 72.dp, onClick = onGreet)
            Column(Modifier.weight(1f), Arrangement.spacedBy(2.dp)) {
                Text(s.characterName(id), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(s.characterRole(id), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(s.characterBio(id), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (voiceNote != null) Text(voiceNote, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * Окно в начале курса: герои курса с портретами, ролями и характерами. Нажатие на портрет — герой здоровается своим голосом (`greet`; null — озвучки нет).
 * Показывается один раз за курс, закрытие отмечает знакомство.
 */
@Composable
fun CastIntro(course: String, cast: List<CharacterProfile>, tag: String, greet: ((id: String, text: String, line: String) -> Unit)?, onClose: () -> Unit) {
    val s = LocalStrings.current
    DlModal.Shell(s.castTitle(course), onDismiss = onClose) {
        Text(s.castText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(Modifier.fillMaxWidth().heightIn(max = 380.dp).verticalScroll(rememberScrollState()), Arrangement.spacedBy(14.dp)) {
            cast.forEach { c ->
                val line = "intro:${c.id}"
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    CharacterAvatar(c.id, 64.dp, line = line, onClick = greet?.let { f -> { f(c.id, hello(c.id, VoiceNames.language(tag)), line) } })
                    Column(Modifier.weight(1f), Arrangement.spacedBy(2.dp)) {
                        Text(s.characterName(c.id), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(s.characterRole(c.id), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(s.characterBio(c.id), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        DlModal.Action(s.castStart, onClose, Modifier.fillMaxWidth())
    }
}

/** Плашка, когда звука нет (нет синтезатора или голоса языка темы): объясняет, что делать; текст на экране остаётся, персонажи говорят без звука. */
@Composable
fun SpeechProblemBar(problem: SpeechProblem?, onDownload: () -> Unit) {
    var hidden by rememberSaveable(problem) { mutableStateOf(false) }
    if (problem == null || hidden) return
    val s = LocalStrings.current
    SettingCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp), gap = 10.dp) {
        Text(if (problem == SpeechProblem.NO_ENGINE) s.problemNoEngine else s.problemNoLanguage, style = MaterialTheme.typography.bodyMedium)
        Row(Modifier.fillMaxWidth(), Arrangement.End, Alignment.CenterVertically) {
            DlModal.Action(s.problemOk, { hidden = true }, outlined = true)
            DlModal.Action(s.download, onDownload, Modifier.padding(start = 8.dp))
        }
    }
}
