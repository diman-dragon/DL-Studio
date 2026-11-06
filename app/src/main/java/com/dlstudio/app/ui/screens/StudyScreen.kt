package com.dlstudio.app.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dlstudio.app.data.Prefs
import com.dlstudio.app.library.*
import com.dlstudio.app.ui.components.*
import com.dlstudio.app.ui.library.StudyCatalog
import com.dlstudio.app.ui.theme.LocalStrings

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudyScreen(vm: LibraryViewModel = viewModel()) {
    val s = LocalStrings.current; val state by vm.state.collectAsStateWithLifecycle()
    fun List<LibNode>.cards(depth: Int = 0): List<LibNode> = flatMap { if (depth >= 1) it.children else it.children.cards(depth + 1) }
    fun stat(nodes: List<LibNode>, depth: Int): CatalogStat { val now = System.currentTimeMillis(); val cards = nodes.cards(depth); return CatalogStat(cards.size, cards.count { (state.dueAt[it.id] ?: 0) <= now }, cards.count { (state.dueAt[it.id] ?: 0) in 1 until now }) }
    var projectId by remember { mutableStateOf<String?>(null) }; var deckId by remember { mutableStateOf<String?>(null) }; var flipped by remember { mutableStateOf(false) }; var hint by remember { mutableStateOf(false) }; var grid by remember { mutableStateOf(true) }; var faceStarted by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val project = state.items.firstOrNull { it.id == projectId }; val deck = project?.children?.firstOrNull { it.id == deckId }
    val cards = remember(state.items, state.dueAt, projectId, deckId) { (if (deckId == "*") project?.children?.cards() ?: emptyList() else deck?.children ?: emptyList()).sortedBy { state.dueAt[it.id] ?: 0L } }; val pager = rememberPagerState { cards.size }; val context = LocalContext.current; var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) { val p = Prefs(context); lateinit var e: TextToSpeech; e = TextToSpeech(context) { if (it == TextToSpeech.SUCCESS) p.speechVoiceName?.let { n -> e.voices?.firstOrNull { it.name == n }?.let(e::setVoice) } }; e.setSpeechRate(p.speechRate); e.setPitch(p.speechPitch); tts = e; onDispose { e.stop(); e.shutdown() } }
    LaunchedEffect(cards.size, projectId, deckId) { if (cards.isNotEmpty()) pager.scrollToPage(0) }; LaunchedEffect(pager.currentPage) { faceStarted = System.currentTimeMillis(); flipped = false; hint = false }
    @Composable fun Header(title: String, back: () -> Unit) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { if (projectId != null) Text("‹", Modifier.clickable { back() }.padding(end = 14.dp), style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary); Text(title, style = MaterialTheme.typography.headlineMedium) } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), Arrangement.spacedBy(16.dp)) {
        when {
            cards.isNotEmpty() -> { Header(deck?.name ?: project?.name.orEmpty()) { if (deckId != null) deckId = null else projectId = null }; HorizontalPager(pager, Modifier.fillMaxWidth().height(390.dp)) { StudyFlashCard(cards[it], flipped, hint, { val front = !flipped; flipped = !flipped; if (front) vm.markStudied(cards[it].id, System.currentTimeMillis() - faceStarted, flips = 1) }, { hint = !hint }, { tts?.speak(it, TextToSpeech.QUEUE_FLUSH, null, "study") }) } }
            projectId == null -> { Header(s.tabStudy) {}; Text(s.chooseProject, color = MaterialTheme.colorScheme.onSurfaceVariant); StudyCatalog(state.items, 0, null, CatalogStat(0, 0, 0), grid, { grid = it }, { stat(it.children, 1) }, {}, { projectId = it.id }) }
            else -> { Header(project?.name ?: s.allCards) { projectId = null; deckId = null }; Text(s.chooseDeck, color = MaterialTheme.colorScheme.onSurfaceVariant); StudyCatalog(project?.children.orEmpty(), 1, s.allDecks, stat(project?.children.orEmpty(), 1), grid, { grid = it }, { stat(listOf(it), 1) }, { deckId = "*" }, { deckId = it.id }) }
        }
    }
}
