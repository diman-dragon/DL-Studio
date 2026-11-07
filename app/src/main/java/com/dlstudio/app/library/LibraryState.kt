package com.dlstudio.app.library

import com.dlstudio.app.study.CardState
import com.dlstudio.app.study.SavedSession
import com.dlstudio.app.study.StudyStats

/**
 * Состояние библиотеки: дерево, открытый узел (путь из id), вид, загрузка, расписание карточек и статистика.
 * `failed` — база не открылась: правки отключены, чтобы не затереть сохранённое.
 */
data class LibraryState(
    val items: List<LibNode> = emptyList(),
    val path: List<String> = emptyList(),
    val grid: Boolean = true,
    val loaded: Boolean = false,
    val failed: Boolean = false,
    val states: Map<String, CardState> = emptyMap(),
    val stats: StudyStats = StudyStats(),
    val today: Long = 0,
    val report: ImportReport? = null,
    val resume: SavedSession? = null
)
