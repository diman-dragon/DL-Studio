package com.dlstudio.app.library

/** Состояние экрана: всё дерево, открытый узел (путь из id), вид «карточки/список», загружено ли, итог импорта. */
data class LibraryState(
    val items: List<LibNode> = emptyList(),
    val path: List<String> = emptyList(),
    val grid: Boolean = true,
    val loaded: Boolean = false,
    val studiedCards: Int = 0,
    val stats: StudyStats = StudyStats(),
    val dueAt: Map<String, Long> = emptyMap(),
    val todayCards: Set<String> = emptySet(),
    val report: ImportReport? = null
)
