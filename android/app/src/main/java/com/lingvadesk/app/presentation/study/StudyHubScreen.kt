package com.lingvadesk.app.presentation.study

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.lingvadesk.app.domain.model.DeckStat
import com.lingvadesk.app.domain.model.ProjectStat
import com.lingvadesk.app.domain.model.TodayInfo
import com.lingvadesk.app.domain.repository.LingvaRepository
import com.lingvadesk.app.presentation.common.components.EmptyHint
import com.lingvadesk.app.presentation.common.components.ItemCard
import com.lingvadesk.app.presentation.common.components.ItemsView
import com.lingvadesk.app.presentation.common.components.ScreenTitle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HubUi(
    val projects: List<ProjectStat> = emptyList(),
    val selected: ProjectStat? = null,
    val decks: List<DeckStat> = emptyList(),
    val today: TodayInfo = TodayInfo(0, 0, 0)
)

@HiltViewModel
class StudyHubViewModel @Inject constructor(private val repo: LingvaRepository) : ViewModel() {
    private val _ui = MutableStateFlow(HubUi())
    val ui: StateFlow<HubUi> = _ui.asStateFlow()

    fun reload() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val projects = repo.projectStats(now)
            val sel = _ui.value.selected?.let { s -> projects.firstOrNull { it.project.id == s.project.id } }
            val decks = sel?.let { repo.deckStats(it.project.id, now) } ?: emptyList()
            _ui.value = HubUi(projects, sel, decks, repo.loadToday(now))
        }
    }

    fun select(p: ProjectStat?) {
        _ui.value = _ui.value.copy(selected = p, decks = emptyList())
        reload()
    }
}

/** «Учёба»: проекты → колоды; нажатие запускает сессию (`all`, `p:<id>`, `d:<id>`). */
@Composable
fun StudyHubScreen(grid: Boolean, onStart: (String) -> Unit, vm: StudyHubViewModel = hiltViewModel()) {
    val ui by vm.ui.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.reload() }
    BackHandler(enabled = ui.selected != null) { vm.select(null) }

    Column(Modifier.fillMaxSize()) {
        val sel = ui.selected
        ScreenTitle(title = sel?.project?.name ?: "Давай-ка поучим…", onBack = if (sel != null) ({ vm.select(null) }) else null)
        Text(
            "Сегодня: ${ui.today.n} · серия ${ui.today.current} дн. (лучшая ${ui.today.best})",
            modifier = Modifier.padding(horizontal = 16.dp), style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
        )
        when {
            ui.projects.isEmpty() -> EmptyHint("Пока нет карточек. Добавьте проект, колоду и карточки в «Библиотеке» или импортируйте файл.")
            sel == null -> {
                val all = ProjectStat(com.lingvadesk.app.domain.model.Project(-1, "Вся библиотека", ""), ui.projects.sumOf { it.decks }, ui.projects.sumOf { it.total }, ui.projects.sumOf { it.due }, ui.projects.sumOf { it.fresh })
                ItemsView(listOf(all) + ui.projects, grid) { p ->
                    ItemCard(p.project.name, "${p.total} карт. · ${p.due} повторить · ${p.fresh} новых",
                        onClick = { if (p.project.id < 0) onStart("all") else vm.select(p) })
                }
            }
            else -> {
                val whole = DeckStat(com.lingvadesk.app.domain.model.Deck(-1, sel.project.id, "Весь проект"), sel.total, sel.due, sel.fresh)
                ItemsView(listOf(whole) + ui.decks, grid) { d ->
                    ItemCard(d.deck.name, "${d.total} карт. · ${d.due} повторить · ${d.fresh} новых",
                        onClick = { onStart(if (d.deck.id < 0) "p:${sel.project.id}" else "d:${d.deck.id}") })
                }
            }
        }
    }
}
