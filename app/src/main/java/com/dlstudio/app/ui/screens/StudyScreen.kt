package com.dlstudio.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.library.LibraryViewModel
import com.dlstudio.app.study.StudyViewModel
import com.dlstudio.app.ui.components.StudyHome
import com.dlstudio.app.ui.components.StudySessionView

/** Учёба: выбор (вся библиотека → проект → колода) и сессия. Состояние в `StudyViewModel`, поэтому переживает смену вкладки. */
@Composable
fun StudyScreen(sessionSize: Int, onSpeak: (String) -> Unit, study: StudyViewModel = viewModel(), library: LibraryViewModel = viewModel()) {
    val ui by study.ui.collectAsStateWithLifecycle()
    val lib by library.state.collectAsStateWithLifecycle()
    LaunchedEffect(lib.loaded) { if (lib.loaded) study.restore(lib.items, lib.resume) }
    BackHandler(enabled = ui.session != null || ui.scoped, onBack = study::back)
    val session = ui.session
    if (session != null) {
        StudySessionView(session, { study.flip()?.let(library::grade) }, study::hint, { page -> study.goTo(page); library.savePosition(session.id, page) }, study::close, onSpeak)
    } else StudyHome(ui, lib, sessionSize, study) { library.startSession(it.id, it.queue.map(LibNode::id)) }
}
