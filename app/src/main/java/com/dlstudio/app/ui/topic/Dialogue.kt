package com.dlstudio.app.ui.topic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dlstudio.app.content.Block
import com.dlstudio.app.speech.Characters
import com.dlstudio.app.speech.SpeakAll
import com.dlstudio.app.speech.SpeakAs
import com.dlstudio.app.speech.Utter
import com.dlstudio.app.speech.VoiceNames
import com.dlstudio.app.ui.components.CharacterAvatar
import com.dlstudio.app.ui.components.GradientButton
import com.dlstudio.app.ui.components.LocalSpeaking
import com.dlstudio.app.ui.components.OutlineButton
import com.dlstudio.app.ui.components.SmallSpeaker
import com.dlstudio.app.ui.components.hello
import com.dlstudio.app.ui.components.isSpeaking
import com.dlstudio.app.ui.theme.LocalStrings

/**
 * Всё, что теории нужно для голосов персонажей: озвучка реплики (`say`: текст, персонаж, метка строки) и диалога (`all`), остановка, язык темы `tag`
 * (пусто — озвучки нет, тогда `voiced` ложь), персонажи, с которыми ученик уже знаком (`met`), и отметка знакомства (`onMeet`).
 */
class Talk(val say: SpeakAs, val all: SpeakAll, val stop: () -> Unit, val tag: String, val met: Set<String>, val onMeet: (String) -> Unit) {
    val voiced: Boolean get() = tag.isNotBlank()
}

/** Метка строки на экране: страница и номер блока. По ней оживает аватар именно этой реплики. */
fun lineId(page: Int, block: Int) = "t$page.$block"

/** Реплика: у персонажа с отметкой — его портрет и имя; озвучивается голосом этого персонажа, пока звучит — строка подсвечена цветом персонажа. */
@Composable
fun SayRow(say: Block.Say, line: String, talk: Talk) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val profile = Characters.of(say.who)
    val tint = Color(profile.tint)
    val live = isSpeaking(LocalSpeaking.current, profile.id, line)
    val shape = RoundedCornerShape(18.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape).background(if (live) tint.copy(alpha = 0.28f) else colors.primaryContainer)
            .border(if (live) 2.dp else 0.dp, if (live) tint else Color.Transparent, shape).padding(start = 12.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
        Arrangement.spacedBy(10.dp), Alignment.CenterVertically
    ) {
        if (say.who != null) CharacterAvatar(profile.id, 48.dp, line = line)
        Column(Modifier.weight(1f), Arrangement.spacedBy(2.dp)) {
            if (say.who != null) Text(s.characterName(profile.id), style = MaterialTheme.typography.labelSmall, color = colors.primary)
            Text(say.text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (say.note.isNotBlank()) Text(say.note, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
        if (talk.voiced) SmallSpeaker(say.say.ifBlank { say.text }, { text -> talk.say(text, say.who, line) }, size = 42.dp)
    }
}

/** Над диалогом из нескольких реплик: участники и кнопка «Слушать диалог» — реплики звучат подряд, каждая голосом своего персонажа. */
@Composable
fun DialogueBar(ids: List<String>, items: List<Utter>, talk: Talk) {
    val s = LocalStrings.current
    val line = LocalSpeaking.current?.line
    val playing = line != null && items.any { it.line == line }
    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp), Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { ids.forEach { CharacterAvatar(it, 36.dp) } }
        Box(Modifier.weight(1f)) {
            if (playing) OutlineButton(s.dialogueStop, talk.stop) else GradientButton(s.dialogueListen, { talk.all(items) })
        }
    }
}

/** Первое появление персонажа: портрет, имя, роль и характер; нажатие на портрет — персонаж здоровается своим голосом. */
@Composable
fun MeetBanner(id: String, talk: Talk) {
    val s = LocalStrings.current
    val tint = Color(Characters.of(id).tint)
    val shape = RoundedCornerShape(18.dp)
    val line = "meet:$id"
    val greet: (() -> Unit)? = if (talk.voiced) { { talk.say(hello(id, VoiceNames.language(talk.tag)), id, line) } } else null
    Row(
        Modifier.fillMaxWidth().clip(shape).background(tint.copy(alpha = 0.16f)).border(1.dp, tint.copy(alpha = 0.6f), shape).padding(12.dp),
        Arrangement.spacedBy(12.dp), Alignment.CenterVertically
    ) {
        CharacterAvatar(id, 64.dp, line = line, onClick = greet)
        Column(Modifier.weight(1f).padding(end = 4.dp), Arrangement.spacedBy(2.dp)) {
            Text(s.meetTitle(s.characterName(id)), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(s.characterRole(id), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(s.characterBio(id), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (greet != null) Text(s.sayHello, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}
