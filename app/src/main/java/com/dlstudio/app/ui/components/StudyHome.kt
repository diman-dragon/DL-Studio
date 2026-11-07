package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dlstudio.app.library.LibraryViewModel
import com.dlstudio.app.study.StudySession
import com.dlstudio.app.study.StudyUi
import com.dlstudio.app.study.StudyViewModel
import com.dlstudio.app.ui.theme.LocalStrings

/** Экран «Учёба» вне сессии: дашборд или, внутри проекта/колоды, плитка узла. Библиотеку читает только он, а не сессия. */
@Composable
fun StudyHome(ui: StudyUi, sessionSize: Int, study: StudyViewModel, library: LibraryViewModel, onSession: (StudySession) -> Unit) {
    val lib by library.state.collectAsStateWithLifecycle()
    when {
        lib.failed -> Column(Modifier.padding(16.dp)) { Text(LocalStrings.current.libraryFailed, color = MaterialTheme.colorScheme.error) }
        !lib.loaded -> Unit
        ui.scoped -> StudyScopeView(ui, lib, sessionSize, study, onSession)
        else -> StudyDashboard(lib, sessionSize, study, onSession)
    }
}
