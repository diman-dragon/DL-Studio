package com.dlstudio.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.library.LibraryViewModel
import com.dlstudio.app.study.Speak
import com.dlstudio.app.study.StudySession
import com.dlstudio.app.study.StudyViewModel
import com.dlstudio.app.ui.components.StudyHome
import com.dlstudio.app.ui.components.StudySessionView
import kotlinx.coroutines.flow.first

/**
 * Учёба: дашборд и сессия. Сессия не читает состояние библиотеки, поэтому запись позиции и оценок
 * не перерисовывает карточки. Состояние живёт в `StudyViewModel` и переживает смену вкладки.
 */
@Composable
fun StudyScreen(sessionSize: Int, onSpeak: Speak, study: StudyViewModel = viewModel(), library: LibraryViewModel = viewModel()) {
    val ui by study.ui.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { library.state.first { it.loaded }.let { study.restore(it.items, it.resume) } }
    BackHandler(enabled = ui.session != null || ui.scoped, onBack = study::back)
    val saveSession = { new: StudySession -> library.startSession(new.id, new.queue.map(LibNode::id), new.random) }
    val session = ui.session
    if (session != null) {
        StudySessionView(
            session, { id, forced -> study.flip(id, forced)?.let(library::grade) }, study::hint,
            { page -> study.goTo(page); library.savePosition(session.id, page) }, { study.nextRound()?.let(saveSession) }, study::close, onSpeak
        )
    } else StudyHome(ui, sessionSize, study, library, saveSession)
}
