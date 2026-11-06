package com.dlstudio.app.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dlstudio.app.data.Prefs
import com.dlstudio.app.data.openParent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Библиотека: чтение при старте, правки сразу в состоянии и в фоне — в файл (пишется всегда самое свежее). */
class LibraryViewModel(private val app: Application) : AndroidViewModel(app) {
    private val prefs = Prefs(app)
    private val lock = Mutex()
    private val mutable = MutableStateFlow(LibraryState(grid = prefs.libraryGrid))
    val state: StateFlow<LibraryState> = mutable.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val items = runCatching { loadLibrary(openParent(app, prefs)) }.getOrDefault(emptyList())
            mutable.update { it.copy(items = items, loaded = true) }
        }
    }

    fun enter(id: String) = mutable.update { it.copy(path = it.path + id) }
    fun up() = mutable.update { it.copy(path = it.path.dropLast(1)) }

    fun setGrid(grid: Boolean) {
        prefs.libraryGrid = grid
        mutable.update { it.copy(grid = grid) }
    }

    fun add(name: String, back: String) = change { it.items.addAt(it.path, LibNode(newId(), name, back)) }
    fun edit(id: String, name: String, back: String) =
        change { s -> s.items.editAt(s.path + id) { it.copy(name = name, back = back) } }
    fun remove(id: String) = change { s -> s.items.editAt(s.path + id) { null } }

    private fun change(transform: (LibraryState) -> List<LibNode>) {
        val items = transform(mutable.value)
        mutable.update { it.copy(items = items) }
        viewModelScope.launch(Dispatchers.IO) {
            lock.withLock { runCatching { saveLibrary(openParent(app, prefs), mutable.value.items) } }
        }
    }
}
