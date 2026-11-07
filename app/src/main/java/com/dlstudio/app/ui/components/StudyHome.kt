package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.library.LibraryState
import com.dlstudio.app.study.CardCounts
import com.dlstudio.app.study.SessionPicker
import com.dlstudio.app.study.StudyScope
import com.dlstudio.app.study.StudySession
import com.dlstudio.app.study.StudyUi
import com.dlstudio.app.study.StudyViewModel
import com.dlstudio.app.ui.library.LibraryHeader
import com.dlstudio.app.ui.library.StudyCatalog
import com.dlstudio.app.ui.library.ViewToggle
import com.dlstudio.app.ui.theme.LocalStrings

/** Выбор что учить: плитка «Сегодня» для текущего уровня и каталог проектов (или колод) под ней. */
@Composable
fun StudyHome(ui: StudyUi, lib: LibraryState, sessionSize: Int, study: StudyViewModel, onSession: (StudySession) -> Unit) {
    val s = LocalStrings.current
    var grid by rememberSaveable { mutableStateOf(true) }
    val project = lib.items.firstOrNull { it.id == ui.projectId }
    val deck = project?.children?.firstOrNull { it.id == ui.deckId }
    val cards = StudyScope.cards(lib.items, ui.projectId, ui.deckId)
    fun counts(list: List<LibNode>) = CardCounts.of(list, lib.states, lib.today)
    fun start(extra: Boolean) { study.begin(SessionPicker.pick(cards, lib.states, lib.today, sessionSize, extra))?.let(onSession) }
    Column(Modifier.fillMaxSize().padding(start = 16.dp, top = 16.dp, end = 16.dp), Arrangement.spacedBy(14.dp)) {
        LibraryHeader(deck?.name ?: project?.name ?: s.tabStudy, ui.scoped, study::back)
        when {
            lib.failed -> Text(s.libraryFailed, color = MaterialTheme.colorScheme.error)
            !lib.loaded -> Unit
            cards.isEmpty() -> Text(s.noCards, color = MaterialTheme.colorScheme.onSurfaceVariant)
            else -> {
                StudyHero(counts(cards), sessionSize, { start(false) }, { start(true) })
                if (ui.deckId == null) {
                    ViewToggle(grid) { grid = it }
                    val nodes = if (ui.scoped) project?.children.orEmpty() else lib.items
                    StudyCatalog(nodes, if (ui.scoped) 1 else 0, grid, { counts(if (ui.scoped) it.children else it.children.flatMap { d -> d.children }) },
                        { if (ui.scoped) study.openDeck(it.id) else study.openProject(it.id) }, Modifier.weight(1f))
                }
            }
        }
    }
}
