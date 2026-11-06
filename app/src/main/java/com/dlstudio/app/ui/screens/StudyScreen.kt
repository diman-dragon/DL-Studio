package com.dlstudio.app.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.library.LibraryViewModel
import com.dlstudio.app.data.Prefs
import com.dlstudio.app.ui.theme.LocalStrings

private fun List<LibNode>.cards(depth: Int = 0): List<LibNode> = flatMap { node ->
    if (depth >= 2) listOf(node) else node.children.cards(depth + 1)
}

@Composable
fun StudyScreen(vm: LibraryViewModel = viewModel()) {
    val s = LocalStrings.current
    val state by vm.state.collectAsStateWithLifecycle()
    val cards = remember(state.items) { state.items.cards() }
    var index by remember { mutableIntStateOf(0) }
    var revealed by remember { mutableStateOf(false) }
    var showHint by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val card = cards.getOrNull(index)
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val engine = TextToSpeech(context) { }
        val prefs = Prefs(context)
        engine.setSpeechRate(prefs.speechRate)
        engine.setPitch(prefs.speechPitch)
        tts = engine
        onDispose { engine.stop(); engine.shutdown() }
    }
    LaunchedEffect(cards.size) { index = index.coerceIn(0, (cards.size - 1).coerceAtLeast(0)) }
    LaunchedEffect(index) { revealed = false; showHint = false }
    Column(Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(s.tabStudy, style = MaterialTheme.typography.headlineMedium)
        if (card == null) {
            Text(s.noCards, Modifier.fillMaxWidth().padding(top = 80.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Text("${index + 1} / ${cards.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (revealed) card.back.ifBlank { card.name } else card.name, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
                    if (!revealed) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (card.hint.isNotBlank()) OutlinedButton({ showHint = !showHint }) { Text(s.hintButton) }
                            OutlinedButton({ tts?.speak(card.name, TextToSpeech.QUEUE_FLUSH, null, "card") }) { Text(s.speak) }
                        }
                        if (showHint && card.hint.isNotBlank()) Text(card.hint, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
                        Button({ revealed = true; vm.markStudied() }) { Text(s.showAnswer) }
                    } else {
                        if (card.explanation.isNotBlank()) Detail("Пояснение", card.explanation)
                        if (card.example.isNotBlank()) Detail("Пример", card.example)
                        OutlinedButton({ tts?.speak(card.back, TextToSpeech.QUEUE_FLUSH, null, "answer") }, enabled = card.back.isNotBlank()) { Text(s.speak) }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton({ index = (index - 1).coerceAtLeast(0) }, enabled = index > 0) { Text(s.previous) }
                Button({ index = (index + 1).coerceAtMost(cards.lastIndex) }, enabled = index < cards.lastIndex) { Text(s.next) }
            }
        }
    }
}

@Composable
private fun Detail(title: String, text: String) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
