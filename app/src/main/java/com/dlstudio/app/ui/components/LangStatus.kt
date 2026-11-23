package com.dlstudio.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
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
    remember(state.topics, state.speech.voices, state.speech.packs, state.speech.chosen) { Needs.of(state.topics, state.speech.voices, state.speech.packs, state.speech.chosen) }

private fun mark(ok: Boolean) = if (ok) "✓" else "✗"

/** Строка состояния языка: голос, распознавание, а также женский и мужской голос — если диалогов в темах они нужны, а голоса нет. */
private fun statusLine(s: Strings, need: LangNeed, packs: Map<String, PackState>): String {
    val states = need.tags.map { packs[it] ?: PackState.UNKNOWN }
    return listOfNotNull(
        "${mark(!need.voiceMissing)} ${s.statusVoice}",
        when {
            states.all { it == PackState.INSTALLED } -> "✓ ${s.statusRecog}"
            need.packs.isNotEmpty() -> "✗ ${s.statusRecog}"
            states.any { it == PackState.DOWNLOADING } -> "… ${s.statusRecog}"
            else -> null
        },
        "✗ ${s.statusFemale}".takeIf { need.lackFemale },
        "✗ ${s.statusMale}".takeIf { need.lackMale }
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
