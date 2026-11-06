package com.dlstudio.app.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dlstudio.app.data.Prefs
import com.dlstudio.app.library.*
import com.dlstudio.app.R
import com.dlstudio.app.ui.components.AppIcon
import com.dlstudio.app.ui.library.ViewToggle
import com.dlstudio.app.ui.theme.LocalStrings

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudyScreen(vm: LibraryViewModel = viewModel()) {
    val s = LocalStrings.current; val state by vm.state.collectAsStateWithLifecycle()
    fun List<LibNode>.cards(depth: Int = 0): List<LibNode> = flatMap { if (depth >= 1) it.children else it.children.cards(depth + 1) }
    var projectId by remember { mutableStateOf<String?>(null) }; var deckId by remember { mutableStateOf<String?>(null) }; var flipped by remember { mutableStateOf(false) }; var hint by remember { mutableStateOf(false) }; var grid by remember { mutableStateOf(true) }
    val project = state.items.firstOrNull { it.id == projectId }; val deck = project?.children?.firstOrNull { it.id == deckId }
    val cards = remember(state.items, projectId, deckId) { when { projectId == "*" -> state.items.cards(); deckId == "*" -> project?.children?.cards() ?: emptyList(); else -> deck?.children ?: emptyList() } }; val pager = rememberPagerState { cards.size }
    val card = cards.getOrNull(pager.currentPage); val context = LocalContext.current; var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) { val p = Prefs(context); lateinit var e: TextToSpeech; e = TextToSpeech(context) { if (it == TextToSpeech.SUCCESS) p.speechVoiceName?.let { n -> e.voices?.firstOrNull { it.name == n }?.let(e::setVoice) } }; e.setSpeechRate(p.speechRate); e.setPitch(p.speechPitch); tts = e; onDispose { e.stop(); e.shutdown() } }
    LaunchedEffect(cards.size, projectId, deckId) { if (cards.isNotEmpty()) pager.scrollToPage(0); flipped = false; hint = false }
    @Composable fun Header(title: String, back: () -> Unit) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { if (projectId != null) Text("‹", Modifier.clickable { back() }.padding(end = 14.dp), style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary); Text(title, style = MaterialTheme.typography.headlineMedium) } }
    @Composable fun Choice(title: String, subtitle: String, action: () -> Unit, modifier: Modifier = Modifier) { Surface(modifier.clickable { action() }, RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(title, style = MaterialTheme.typography.titleLarge); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
    @Composable fun Catalog(nodes: List<LibNode>, allTitle: String, allSubtitle: String, onAll: () -> Unit, onNode: (LibNode) -> Unit) { ViewToggle(grid) { grid = it }; val entries = listOf(LibNode("*", allTitle, allSubtitle)) + nodes; if (grid) entries.chunked(2).forEach { row -> Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(10.dp)) { row.forEach { n -> Choice(n.name, if (n.id == "*") n.back else "${n.children.size} ${s.statsCards.lowercase()}", { if (n.id == "*") onAll() else onNode(n) }, Modifier.weight(1f)) }; if (row.size == 1) Spacer(Modifier.weight(1f)) } } else entries.forEach { n -> Choice(n.name, if (n.id == "*") n.back else "${n.children.size} ${s.statsCards.lowercase()}", { if (n.id == "*") onAll() else onNode(n) }, Modifier.fillMaxWidth()) } }
    @Composable fun IconAction(icon: Int, action: () -> Unit, enabled: Boolean = true) { Surface(Modifier.size(48.dp).clickable(enabled) { action() }, RoundedCornerShape(16.dp), color = Color.White.copy(.18f)) { Box(contentAlignment = Alignment.Center) { AppIcon(icon, Color.White, 25.dp) } } }
    @Composable fun Flash(c: LibNode) { val turn by animateFloatAsState(if (flipped) 180f else 0f, spring(stiffness = Spring.StiffnessLow), label = "flip"); Box(Modifier.fillMaxWidth().height(390.dp).graphicsLayer { rotationY = turn; cameraDistance = 18f * density }.clip(RoundedCornerShape(32.dp)).background(Brush.linearGradient(if (flipped) listOf(Color(0xFF714BFF), Color(0xFF14C8D4)) else listOf(Color(0xFFFFA52F), Color(0xFFFF3C83)))).clickable { flipped = !flipped; if (!flipped) vm.markStudied() }) { Column(Modifier.fillMaxSize().graphicsLayer { if (flipped) rotationY = 180f }.padding(26.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.CenterHorizontally) { if (!flipped) { Text(c.name, Modifier.fillMaxWidth(), style = MaterialTheme.typography.displaySmall, color = Color.White, textAlign = TextAlign.Center); Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { if (c.hint.isNotBlank()) IconAction(R.drawable.ic_lightbulb, { hint = !hint }); IconAction(R.drawable.ic_volume, { tts?.speak(c.name, TextToSpeech.QUEUE_FLUSH, null, "front") }) }; if (hint) Text(c.hint, color = Color.White, textAlign = TextAlign.Center) } else { Text(c.back.ifBlank { c.name }, Modifier.fillMaxWidth(), style = MaterialTheme.typography.displaySmall, color = Color.White, textAlign = TextAlign.Center); Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) { if (c.explanation.isNotBlank()) Text(c.explanation, color = Color.White); if (c.example.isNotBlank()) Text(c.example, color = Color.White.copy(.85f)) }; IconAction(R.drawable.ic_volume, { tts?.speak(c.back, TextToSpeech.QUEUE_FLUSH, null, "back") }, c.back.isNotBlank()) } } } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        when { card != null -> { Header(deck?.name ?: if (projectId == "*") s.allDecks else project?.name ?: s.allCards) { projectId = if (projectId == "*") null else projectId; deckId = null }; HorizontalPager(pager, Modifier.fillMaxWidth().height(390.dp)) { Flash(cards[it]) } }
            projectId == null -> { Header(s.tabStudy) {}; Text(s.chooseProject, color = MaterialTheme.colorScheme.onSurfaceVariant); Catalog(state.items, s.allDecks, "${state.items.cards().size} ${s.statsCards.lowercase()}", { projectId = "*" }) { projectId = it.id } }
            else -> { Header(project?.name ?: s.allCards) { projectId = null; deckId = null }; Text(s.chooseDeck, color = MaterialTheme.colorScheme.onSurfaceVariant); Catalog(project?.children ?: emptyList(), s.allCards, "${project?.children?.cards()?.size ?: 0} ${s.statsCards.lowercase()}", { deckId = "*" }) { deckId = it.id } }
        }
    }
}
