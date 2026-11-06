package com.dlstudio.app.study

/** Что открыто на экране «Учёба»: проект и колода (`null` — все) и идущая сессия. `scoped` — выбор сделан. */
data class StudyUi(val scoped: Boolean = false, val projectId: String? = null, val deckId: String? = null, val session: StudySession? = null)
