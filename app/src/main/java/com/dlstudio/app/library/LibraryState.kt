package com.dlstudio.app.library

import com.dlstudio.app.learning.MotivationTotals

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
    val report: ImportReport? = null,
    val motivation: MotivationTotals = MotivationTotals()
)
