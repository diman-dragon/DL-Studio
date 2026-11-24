package com.dlstudio.app.ui.topic

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.dlstudio.app.R
import com.dlstudio.app.content.Topic
import com.dlstudio.app.progress.TopicProgress
import com.dlstudio.app.progress.Xp
import com.dlstudio.app.settings.AppLang
import com.dlstudio.app.speech.Check
import com.dlstudio.app.speech.Speak
import com.dlstudio.app.speech.Role as VoiceRole
import com.dlstudio.app.speech.SpeakAs
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.AppViewModel
import com.dlstudio.app.ui.components.AppIcon
import com.dlstudio.app.ui.components.BackButton
import com.dlstudio.app.ui.components.TopicNeeds
import com.dlstudio.app.ui.theme.LocalStrings
import java.util.Locale

private enum class Stage { THEORY, CARDS, QUIZ }

/** Экран темы: три этапа — теория, карточки, практика. Этап, которого в теме нет, не показывается. */
@Composable
fun TopicScreen(topic: Topic, state: AppState, vm: AppViewModel, onBack: () -> Unit) {
    val s = LocalStrings.current
    val progress = state.progress.topics[topic.id] ?: TopicProgress()
    val stages = listOfNotNull(
        Stage.THEORY.takeIf { topic.theory.isNotEmpty() },
        Stage.CARDS.takeIf { topic.cards.isNotEmpty() },
        Stage.QUIZ.takeIf { topic.quiz.isNotEmpty() }
    )
    var picked by rememberSaveable(topic.id) { mutableStateOf(0) }
    val index = picked.coerceIn(0, stages.lastIndex)
    val speak: Speak? = topic.tag.takeIf { it.isNotBlank() }?.let { tag -> { text: String -> vm.speak(text, tag) } }
    val speakAs: SpeakAs? = topic.tag.takeIf { it.isNotBlank() }?.let { tag -> { text: String, role: VoiceRole -> vm.speak(text, tag, role) } }
    val check: Check? = topic.tag.takeIf { it.isNotBlank() }?.let { tag -> Check(state.pronounce, { vm.listen(it, tag) }, vm::stopListening, vm::micDenied, { vm.downloadPack(tag) }) }

    TopicNeeds(topic, state, vm, Locale.forLanguageTag(if (state.lang == AppLang.RU) "ru" else "en"))
    Column(Modifier.fillMaxSize().padding(top = 12.dp)) {
        Row(Modifier.padding(horizontal = 16.dp), Arrangement.spacedBy(12.dp), Alignment.CenterVertically) {
            BackButton(onBack)
            Column(Modifier.weight(1f)) {
                Text(topic.title, style = MaterialTheme.typography.titleLarge, maxLines = 2)
                val where = listOf(topic.course, topic.group).filter { it.isNotBlank() }.joinToString(" · ")
                if (where.isNotBlank()) Text(where, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), Arrangement.spacedBy(8.dp)) {
            stages.forEachIndexed { i, stage ->
                val label = when (stage) { Stage.THEORY -> s.stageTheory; Stage.CARDS -> s.stageCards; Stage.QUIZ -> s.stagePractice }
                val done = when (stage) { Stage.THEORY -> Xp.theoryDone(topic, progress); Stage.CARDS -> Xp.cardsDone(topic, progress); Stage.QUIZ -> Xp.quizDone(topic, progress) }
                StageChip(label, i == index, done, { picked = i }, Modifier.weight(1f))
            }
        }
        val content = Modifier.weight(1f)
        when (stages[index]) {
            Stage.THEORY -> TheoryStage(topic.theory, speakAs, vm.mediaDir(topic.id), vm::seePage, { vm.finishTheory(topic); if (index < stages.lastIndex) picked = index + 1 }, content)
            Stage.CARDS -> CardsStage(topic.cards, progress.seen, speak, check, { vm.seeCard(topic, it) }, content)
            Stage.QUIZ -> QuizStage(topic.quiz, Xp.quizDone(topic, progress), speak, check, { i, ok -> vm.answer(topic, i, ok) }, content)
        }
    }
}

@Composable
private fun StageChip(label: String, selected: Boolean, done: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier.clickable(role = Role.Tab, onClick = onClick), RoundedCornerShape(16.dp), if (selected) colors.primaryContainer else colors.surfaceVariant,
        border = BorderStroke(1.dp, if (selected) colors.primary else colors.outlineVariant)
    ) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 10.dp), Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally), Alignment.CenterVertically) {
            if (done) AppIcon(R.drawable.ic_check, colors.primary, 18.dp)
            Text(label, color = if (selected) colors.primary else colors.onSurfaceVariant, style = MaterialTheme.typography.labelLarge, maxLines = 1)
        }
    }
}
