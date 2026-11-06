package com.dlstudio.app.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import com.dlstudio.app.ui.theme.LocalStrings

@Composable
fun StudyScreen(vm: LibraryViewModel = viewModel()) {
    val s = LocalStrings.current; val state by vm.state.collectAsStateWithLifecycle()
    fun List<LibNode>.cards(depth: Int = 0): List<LibNode> = flatMap { if (depth >= 1) it.children else it.children.cards(depth + 1) }
    var projectId by remember { mutableStateOf<String?>(null) }; var deckId by remember { mutableStateOf<String?>(null) }; var index by remember { mutableIntStateOf(0) }; var flipped by remember { mutableStateOf(false) }; var hint by remember { mutableStateOf(false) }
    val project = state.items.firstOrNull { it.id == projectId }; val deck = project?.children?.firstOrNull { it.id == deckId }
    val cards = remember(state.items, projectId, deckId) { when { projectId == "*" -> state.items.cards(); deckId == "*" -> project?.children?.cards() ?: emptyList(); else -> deck?.children ?: emptyList() } }
    val card = cards.getOrNull(index); val context = LocalContext.current; var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) { val e = TextToSpeech(context) {}; val p = Prefs(context); e.setSpeechRate(p.speechRate); e.setPitch(p.speechPitch); tts = e; onDispose { e.stop(); e.shutdown() } }
    LaunchedEffect(cards.size, projectId, deckId) { index = 0; flipped = false; hint = false }
    @Composable fun Header(title: String, back: () -> Unit) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { if (projectId != null) Text("‹", Modifier.clickable { back() }.padding(end = 14.dp), style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary); Text(title, style = MaterialTheme.typography.headlineMedium) } }
    @Composable fun Choice(title: String, subtitle: String, action: () -> Unit) { Surface(Modifier.fillMaxWidth().clickable { action() }, RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(title, style = MaterialTheme.typography.titleLarge); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
    @Composable fun Chip(text: String, action: () -> Unit, enabled: Boolean = true) { Surface(Modifier.clickable(enabled) { action() }, RoundedCornerShape(15.dp), color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant) { Text(text, Modifier.padding(horizontal = 15.dp, vertical = 10.dp), color = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant) } }
    @Composable fun Flash() { val turn by animateFloatAsState(if (flipped) 180f else 0f, spring(stiffness = Spring.StiffnessLow), label = "flip"); Box(Modifier.fillMaxWidth().height(390.dp).graphicsLayer { rotationY = turn; cameraDistance = 18f * density }.clip(RoundedCornerShape(32.dp)).background(Brush.linearGradient(if (flipped) listOf(Color(0xFF714BFF), Color(0xFF14C8D4)) else listOf(Color(0xFFFFA52F), Color(0xFFFF3C83)))).clickable { flipped = !flipped }) { Column(Modifier.fillMaxSize().graphicsLayer { if (flipped) rotationY = 180f }.padding(26.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.CenterHorizontally) { if (!flipped) { Text("${index + 1} / ${cards.size}", color = Color.White.copy(.75f)); Text(card!!.name, Modifier.fillMaxWidth(), style = MaterialTheme.typography.displaySmall, color = Color.White, textAlign = TextAlign.Center); Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { if (card.hint.isNotBlank()) Chip(if (hint) "✓ ${s.hintButton}" else s.hintButton, { hint = !hint }); Chip(s.speak, { tts?.speak(card.name, TextToSpeech.QUEUE_FLUSH, null, "front") }) }; if (hint) Text(card.hint, color = Color.White, textAlign = TextAlign.Center) }; Text(s.flipHint, color = Color.White.copy(.75f)) } else { Text(s.cardBack, color = Color.White.copy(.75f)); Text(card!!.back.ifBlank { card.name }, Modifier.fillMaxWidth(), style = MaterialTheme.typography.displaySmall, color = Color.White, textAlign = TextAlign.Center); Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) { if (card.explanation.isNotBlank()) Text(card.explanation, color = Color.White); if (card.example.isNotBlank()) Text(card.example, color = Color.White.copy(.85f)); Chip(s.speak, { tts?.speak(card.back, TextToSpeech.QUEUE_FLUSH, null, "back") }, card.back.isNotBlank()) } } } } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        when { card != null -> { Header(deck?.name ?: if (projectId == "*") s.allDecks else project?.name ?: s.allCards) { projectId = if (projectId == "*") null else projectId; deckId = null }; Flash(); Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Chip(s.previous, { index--; flipped = false }, index > 0); Chip(s.next, { index++; flipped = false }, index < cards.lastIndex) } }
            projectId == null -> { Header(s.tabStudy) {}; Text(s.chooseProject, color = MaterialTheme.colorScheme.onSurfaceVariant); Choice(s.allDecks, "${state.items.cards().size} ${s.statsCards.lowercase()}") { projectId = "*" }; state.items.forEach { p -> Choice(p.name, "${p.children.size} ${s.statsDecks.lowercase()}") { projectId = p.id } } }
            else -> { Header(project?.name ?: s.allCards) { projectId = null; deckId = null }; Text(s.chooseDeck, color = MaterialTheme.colorScheme.onSurfaceVariant); Choice(s.allCards, "${project?.children?.cards()?.size ?: 0} ${s.statsCards.lowercase()}") { deckId = "*" }; project?.children?.forEach { d -> Choice(d.name, "${d.children.size} ${s.statsCards.lowercase()}") { deckId = d.id } } }
        }
    }
}
