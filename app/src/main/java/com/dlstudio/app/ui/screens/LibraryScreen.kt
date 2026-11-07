package com.dlstudio.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dlstudio.app.library.LibNode
import com.dlstudio.app.library.LibraryViewModel
import com.dlstudio.app.library.childrenAt
import com.dlstudio.app.library.findAt
import com.dlstudio.app.ui.library.*
import com.dlstudio.app.ui.theme.LocalStrings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

/** Библиотека: проект → колода → карточка; создание, правка, удаление и импорт из файла на каждом уровне. */
@Composable
fun LibraryScreen(vm: LibraryViewModel = viewModel(), onStudy: (List<LibNode>) -> Unit = {}) {
    val s = LocalStrings.current
    val state by vm.state.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf<LibDialog?>(null) }
    var importing by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) vm.importFile(uri) }
    val level = state.path.size
    val actions = ItemActions(
        onOpen = { node -> if (level < 2) { vm.enter(node.id) } else { dialog = LibDialog(node) } },
        onEdit = { dialog = LibDialog(it) },
        onDelete = { dialog = LibDialog(it, delete = true) },
        onStudy = { node -> onStudy(if (level == 0) node.children.flatMap { it.children } else node.children) }
    )
    BackHandler(enabled = level > 0, onBack = vm::up)
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(start = 16.dp, top = 16.dp, end = 16.dp)) {
            LibraryHeader(state.items.findAt(state.path)?.name ?: s.tabLibrary, level > 0, vm::up)
            ViewToggle(state.grid, vm::setGrid)
            Spacer(Modifier.height(14.dp))
            if (state.failed) Text(s.libraryFailed, color = MaterialTheme.colorScheme.error)
            else if (state.loaded) LibraryContent(state.items.childrenAt(state.path), level, state.grid, actions)
        }
        if (state.loaded && !state.failed) LibraryActions(level, { dialog = LibDialog(null) }, { importing = true }, Modifier.align(Alignment.BottomEnd).padding(20.dp))
    }
    LibraryDialogs(dialog, level, vm) { dialog = null }
    if (importing) ImportDialog(level, { importing = false; picker.launch(arrayOf("*/*")) }) { importing = false }
    state.report?.let { ImportReportDialog(it, vm::closeReport) }
}
