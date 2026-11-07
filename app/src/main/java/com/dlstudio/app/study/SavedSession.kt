package com.dlstudio.app.study

/** Сессия, сохранённая в базе: порядок карточек, позиция и уже полученные оценки. */
data class SavedSession(val id: String, val startedAt: Long, val position: Int, val cardIds: List<String>, val grades: Map<String, Grade>)
