package com.dlstudio.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.library.LibraryViewModel
import com.dlstudio.app.ui.theme.LocalStrings

@Composable
fun StatsScreen(vm: LibraryViewModel = viewModel()) {
    val s = LocalStrings.current
    val state by vm.state.collectAsStateWithLifecycle()
    fun List<LibNode>.atDepth(depth: Int): List<LibNode> = if (depth == 0) this else flatMap { it.children.atDepth(depth - 1) }
    val projects = state.items.size; val decks = state.items.atDepth(1).size; val cards = state.items.atDepth(2).size
    val studied = state.studiedCards.coerceAtMost(cards)
    val progress = if (cards == 0) 0f else studied.toFloat() / cards
    @Composable fun statCard(label: String, value: String, modifier: Modifier) { Surface(modifier, shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary); Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text(s.tabStats, style = MaterialTheme.typography.headlineMedium)
        Text(s.statsOverview, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            statCard(s.statsProjects, projects.toString(), Modifier.weight(1f)); statCard(s.statsDecks, decks.toString(), Modifier.weight(1f)); statCard(s.statsCards, cards.toString(), Modifier.weight(1f))
        }
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(s.statsProgress, style = MaterialTheme.typography.titleMedium)
                    Text("${(progress * 100).toInt()}%", color = MaterialTheme.colorScheme.primary)
                }
                Box(Modifier.fillMaxWidth().height(10.dp).background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))) {
                    Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)))
                }
                Text("$studied / $cards · ${s.statsStudied}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (cards == 0 || studied == 0) Text(s.statsEmpty, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
