package com.dlstudio.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dlstudio.app.content.Topic
import com.dlstudio.app.i18n.Strings
import com.dlstudio.app.speech.LangNeed
import com.dlstudio.app.speech.Needs
import com.dlstudio.app.speech.PackState
import com.dlstudio.app.ui.AppState
import com.dlstudio.app.ui.AppViewModel
import com.dlstudio.app.ui.theme.LocalStrings
import java.util.Locale

/** Языки установленных тем и чего им не хватает (голоса, пакеты распознавания). */
@Composable
private fun rememberNeeds(state: AppState): List<LangNeed> =
    remember(state.topics, state.speech.voices, state.speech.packs) { Needs.of(state.topics, state.speech.voices, state.speech.packs) }

private fun mark(ok: Boolean) = if (ok) "✓" else "✗"

/** Строка состояния языка: голос учительницы, голос преподавателя, распознавание. */
private fun statusLine(s: Strings, need: LangNeed, packs: Map<String, PackState>): String {
    val states = need.tags.map { packs[it] ?: PackState.UNKNOWN }
    return listOfNotNull(
        "${mark(!need.lackFemale)} ${s.teacherFName}",
        "${mark(!need.lackMale)} ${s.teacherMName}",
        when {
            states.all { it == PackState.INSTALLED } -> "✓ ${s.statusRecog}"
            need.packs.isNotEmpty() -> "✗ ${s.statusRecog}"
            states.any { it == PackState.DOWNLOADING } -> "… ${s.statusRecog}"
            else -> null
        }
    ).joinToString("   ")
}

/** Плитки языков установленных тем со статусом и общая кнопка «Скачать недостающее». */
@Composable
fun LangStatus(state: AppState, vm: AppViewModel, shown: Locale) {
    val s = LocalStrings.current
    val needs = rememberNeeds(state)
    if (!state.speech.loaded) {
        Text(s.voicesLoading, style = MaterialTheme.typography.bodyMedium)
        return
    }
    needs.forEach { need ->
        SettingCard(gap = 3.dp) {
            Text(languageName(need.lang, shown), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(statusLine(s, need, state.speech.packs), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Text(s.voiceCounts(need.installed, need.known), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    val pending = needs.filter { it.pending }
    if (pending.isNotEmpty()) ActionTile(s.downloadNeeded, { vm.downloadNeeded(pending) })
    else Text(s.allReady, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** Окно после установки тем: если им не хватает голосов или пакетов, предлагает скачать недостающее. Показывается, когда состояние пакетов уже проверено. */
@Composable
fun NeedsOffer(state: AppState, vm: AppViewModel, shown: Locale) {
    if (!state.offer) return
    val s = LocalStrings.current
    val needs = rememberNeeds(state)
    val tags = state.topics.map { it.tag }.filter { it.isNotBlank() }.distinct()
    if (!state.speech.loaded || tags.any { it !in state.speech.packs }) return
    val pending = needs.filter { it.pending }
    if (pending.isEmpty()) {
        LaunchedEffect(Unit) { vm.clearOffer() }
        return
    }
    DlModal.Shell(s.offerTitle, onDismiss = vm::clearOffer) {
        Text(s.offerText(pending.joinToString(", ") { languageName(it.lang, shown) }), style = MaterialTheme.typography.bodyLarge)
        DlModal.Actions(s.later, s.download, vm::clearOffer, { vm.downloadNeeded(pending); vm.clearOffer() })
    }
}

/** Перечитывает голоса и пакеты языков `tags` при каждом возвращении в приложение (например, после загрузки голоса в системе). */
@Composable
fun LoadVoicesOnResume(tags: List<String>, vm: AppViewModel) {
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, tags) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME && tags.isNotEmpty()) vm.loadVoices(tags) }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
}

/**
 * Открыв тему, предлагает сразу скачать то, чего её языку не хватает (голос учительницы или преподавателя, пакет распознавания), и скачивает по согласию
 * прямо с этого экрана. Спрашивает один раз за открытие темы; пока состояние пакетов не проверено, молчит.
 */
@Composable
fun TopicNeeds(topic: Topic, state: AppState, vm: AppViewModel, shown: Locale) {
    if (topic.tag.isBlank()) return
    LoadVoicesOnResume(listOf(topic.tag), vm)
    var asked by rememberSaveable(topic.id) { mutableStateOf(false) }
    val s = LocalStrings.current
    val pending = remember(topic, state.speech.voices, state.speech.packs) { Needs.of(listOf(topic), state.speech.voices, state.speech.packs).filter { it.pending } }
    if (asked || !state.speech.loaded || topic.tag !in state.speech.packs || pending.isEmpty()) return
    DlModal.Shell(s.offerTitle, onDismiss = { asked = true }) {
        Text(s.offerText(pending.joinToString(", ") { languageName(it.lang, shown) }), style = MaterialTheme.typography.bodyLarge)
        DlModal.Actions(s.later, s.download, { asked = true }, { vm.downloadNeeded(pending); asked = true })
    }
}
