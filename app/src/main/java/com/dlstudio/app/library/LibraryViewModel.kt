package com.dlstudio.app.library

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dlstudio.app.data.Prefs
import com.dlstudio.app.data.openParent
import com.dlstudio.app.learning.LearningCatalog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Библиотека и расписание. Правки сразу видны в состоянии, в базу пишутся под одним замком,
 * а выгрузка в папку хранения откладывается (см. `DatabaseSync`) и добивается при уходе в фон.
 */
class LibraryViewModel(private val app: Application) : AndroidViewModel(app) {
    private val prefs = Prefs(app)
    private val lock = Mutex()
    private var database: LibraryDatabase? = null
    private var pushJob: Job? = null
    private val mutable = MutableStateFlow(LibraryState(grid = prefs.libraryGrid))
    val state: StateFlow<LibraryState> = mutable.asStateFlow()

    init { viewModelScope.launch(Dispatchers.IO) { lock.withLock { open() } } }

    private fun open() {
        val db = runCatching { LibraryDatabase(app, openParent(app, prefs)) }.getOrNull()
        database = db
        val items = db?.let { runCatching { it.nodes.load() }.getOrNull() }
        if (db == null || items == null) { mutable.update { it.copy(loaded = true, failed = true) }; return }
        mutable.update { it.copy(items = items, loaded = true, failed = false, path = emptyList()) }
    }

    private fun refreshDerived(db: LibraryDatabase) {
        mutable.update { it.copy(motivation = LearningCatalog.motivation(emptyList())) }
    }

    private fun schedulePush() {
        pushJob?.cancel()
        pushJob = viewModelScope.launch(Dispatchers.IO) { delay(PUSH_DELAY_MS); push() }
    }

    private suspend fun push() = lock.withLock { runCatching { database?.push() } }

    /** Немедленная выгрузка (уход приложения в фон). */
    fun flush() { pushJob?.cancel(); viewModelScope.launch(Dispatchers.IO) { push() } }

    /** Новый день или возврат в приложение: пересчитать «к повтору» и статистику. */
    fun refresh() { viewModelScope.launch(Dispatchers.IO) { lock.withLock { database?.let { refreshDerived(it) } } } }

    /** Перед переносом папки хранения: дописать всё и закрыть базу. */
    suspend fun release() { lock.withLock { pushJob?.cancel(); runCatching { database?.push() }; database?.close(); database = null; mutable.update { LibraryState(grid = it.grid) } } }

    /** После переноса: открыть базу на новом месте. */
    suspend fun reopen() { lock.withLock { if (database == null) open() } }

    fun enter(id: String) = mutable.update { it.copy(path = it.path + id) }
    fun up() = mutable.update { it.copy(path = it.path.dropLast(1)) }
    fun setGrid(grid: Boolean) { prefs.libraryGrid = grid; mutable.update { it.copy(grid = grid) } }
    fun closeReport() = mutable.update { it.copy(report = null) }

    private val canEdit: Boolean get() = mutable.value.loaded && !mutable.value.failed

    fun importFile(uri: Uri) {
        val path = mutable.value.path
        viewModelScope.launch(Dispatchers.IO) {
            val (nodes, report) = tryImport(app, uri, path.size)
            if (nodes.isNotEmpty() && canEdit) {
                mutable.update { it.copy(items = it.items.addAllAt(path, nodes)) }
                change { db -> db.nodes.insert(path.lastOrNull(), path.size, nodes) }
            }
            mutable.update { it.copy(report = report) }
        }
    }

    fun add(draft: LibNode) {
        if (!canEdit) return
        val path = mutable.value.path; val node = draft.copy(id = newId())
        mutable.update { it.copy(items = it.items.addAt(path, node)) }
        change { db -> db.nodes.insert(path.lastOrNull(), path.size, listOf(node)) }
    }

    /** Замена узла целиком (его `id` не меняется): поля, которых нет в диалоге, например значения слова, приходят из самого узла. */
    fun edit(node: LibNode) {
        if (!canEdit) return
        val path = mutable.value.path + node.id
        mutable.update { s -> s.copy(items = s.items.editAt(path) { node }) }
        change { db -> db.nodes.update(node) }
    }

    fun remove(id: String) {
        if (!canEdit) return
        val path = mutable.value.path + id
        mutable.update { s -> s.copy(items = s.items.editAt(path) { null }) }
        change { db -> db.nodes.delete(id) }
    }

    private fun change(refresh: Boolean = true, op: (LibraryDatabase) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            lock.withLock { database?.let { db -> runCatching { db.write { op(db) } }; if (refresh) refreshDerived(db) } }
            schedulePush()
        }
    }

    override fun onCleared() {
        val db = database; database = null
        if (db != null) Thread { runCatching { db.push() }; db.close() }.start()
        super.onCleared()
    }

    private companion object { const val PUSH_DELAY_MS = 2000L }
}
