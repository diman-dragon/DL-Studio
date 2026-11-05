package com.lingvadesk.app.presentation.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lingvadesk.app.data.transfer.LibraryTransfer
import com.lingvadesk.app.domain.model.Card
import com.lingvadesk.app.domain.model.DeckStat
import com.lingvadesk.app.domain.model.ProjectStat
import com.lingvadesk.app.domain.repository.LingvaRepository
import com.lingvadesk.app.domain.study.Option
import com.lingvadesk.app.domain.study.PrefKeys
import com.lingvadesk.app.domain.study.PrefOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class Level { Projects, Decks, Cards }

data class LibraryUi(
    val level: Level = Level.Projects,
    val projects: List<ProjectStat> = emptyList(),
    val project: ProjectStat? = null,
    val decks: List<DeckStat> = emptyList(),
    val deck: DeckStat? = null,
    val cards: List<Card> = emptyList(),
    val message: String? = null
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repo: LingvaRepository,
    private val transfer: LibraryTransfer
) : ViewModel() {
    private val _ui = MutableStateFlow(LibraryUi())
    val ui: StateFlow<LibraryUi> = _ui.asStateFlow()
    private val _deckPrefs = MutableStateFlow<Map<String, String>>(emptyMap())

    private fun run(block: suspend () -> Unit) {
        viewModelScope.launch {
            try { block() } catch (e: Exception) { _ui.update { it.copy(message = e.message ?: "Ошибка") } }
        }
    }

    fun messageShown() { _ui.update { it.copy(message = null) } }

    fun reload() = run {
        val now = System.currentTimeMillis()
        val projects = repo.projectStats(now)
        val s = _ui.value
        val project = s.project?.let { p -> projects.firstOrNull { it.project.id == p.project.id } }
        val decks = project?.let { repo.deckStats(it.project.id, now) } ?: emptyList()
        val deck = s.deck?.let { d -> decks.firstOrNull { it.deck.id == d.deck.id } }
        val cards = deck?.let { repo.listCards(it.deck.id) } ?: emptyList()
        val level = when { deck != null -> Level.Cards; project != null -> Level.Decks; else -> Level.Projects }
        _ui.update { it.copy(level = level, projects = projects, project = project, decks = decks, deck = deck, cards = cards) }
    }

    fun openProject(p: ProjectStat) { _ui.update { it.copy(project = p, deck = null, level = Level.Decks) }; reload() }
    fun openDeck(d: DeckStat) { _ui.update { it.copy(deck = d, level = Level.Cards) }; reload() }
    fun back() {
        _ui.update { s ->
            when (s.level) {
                Level.Cards -> s.copy(deck = null, cards = emptyList(), level = Level.Decks)
                Level.Decks -> s.copy(project = null, decks = emptyList(), level = Level.Projects)
                Level.Projects -> s
            }
        }
        reload()
    }

    fun addProject(name: String, tts: String) = run { repo.insertProject(name, tts); reload() }
    fun editProject(id: Long, name: String, tts: String) = run { repo.updateProject(id, name, tts); reload() }
    fun deleteProject(id: Long) = run { repo.deleteProject(id); reload() }

    fun addDeck(name: String) = run { repo.insertDeck(_ui.value.project?.project?.id ?: return@run, name); reload() }
    fun editDeck(id: Long, name: String) = run { repo.updateDeck(id, name); reload() }
    fun deleteDeck(id: Long) = run { repo.deleteDeck(id); reload() }

    fun addCard(c: Card) = run {
        repo.insertCard(_ui.value.deck?.deck?.id ?: return@run, c.front, c.back, c.example, c.exampleTr, c.cloze, c.clozeAnswer, c.clozeTr); reload()
    }
    fun editCard(c: Card) = run { repo.updateCard(c); reload() }
    fun deleteCard(id: Long) = run { repo.deleteCard(id); reload() }

    // параметры повторения колоды (`deck:<id>:<параметр>` в settings)
    val deckPrefs: StateFlow<Map<String, String>> = _deckPrefs.asStateFlow()

    fun loadPrefs(deckId: Long) = run { _deckPrefs.value = loadDeckPrefs(deckId) }

    private suspend fun loadDeckPrefs(deckId: Long): Map<String, String> {
        val all = repo.settings()
        return listOf(PrefKeys.DECK_NEW_PER_DAY, PrefKeys.DECK_SESSION_MAX, PrefKeys.DECK_PACE, PrefKeys.DECK_ORDER)
            .mapNotNull { p -> all[PrefKeys.deckKey(deckId, p)]?.let { p to it } }.toMap()
    }

    fun cycleDeckPref(deckId: Long, param: String, options: List<Option>) = run {
        val next = PrefOptions.next(options, _deckPrefs.value[param]).value
        repo.setSetting(PrefKeys.deckKey(deckId, param), next)
        _deckPrefs.value = loadDeckPrefs(deckId)
    }

    fun import(uri: Uri) = run {
        val r = transfer.import(uri)
        _ui.update { it.copy(message = "Добавлено карточек: ${r.added}, пропущено повторов: ${r.skipped}, новых проектов: ${r.projects}, колод: ${r.decks}") }
        reload()
    }

    fun export(uri: Uri) = run {
        val n = transfer.export(uri)
        _ui.update { it.copy(message = "Экспортировано карточек: $n") }
    }
}
