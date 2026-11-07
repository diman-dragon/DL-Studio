package com.dlstudio.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dlstudio.app.library.LibraryState
import com.dlstudio.app.study.CardCounts
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.ui.library.StudyCatalog
import com.dlstudio.app.ui.library.ViewToggle
import com.dlstudio.app.ui.theme.LocalStrings

/** Раздел «Проекты» дашборда: каталог проектов со сводкой «к повтору / новые»; тап открывает проект. */
@Composable
fun DashProjects(lib: LibraryState, onProject: (LibNode) -> Unit) {
    var grid by rememberSaveable { mutableStateOf(true) }
    if (lib.items.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(LocalStrings.current.projectsTitle, style = MaterialTheme.typography.titleLarge)
            ViewToggle(grid) { grid = it }
        }
        StudyCatalog(lib.items, 0, grid, { CardCounts.of(it.children.flatMap { d -> d.children }, lib.states, lib.today) }, onProject)
    }
}
