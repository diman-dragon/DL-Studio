package com.dlstudio.app.study

import com.dlstudio.app.library.LibNode

/** Какие карточки учим: `null` вместо проекта — вся библиотека, `null` вместо колоды — все колоды проекта. */
object StudyScope {
    fun cards(items: List<LibNode>, projectId: String?, deckId: String?): List<LibNode> {
        val projects = if (projectId == null) items else items.filter { it.id == projectId }
        val decks = projects.flatMap { it.children }.filter { deckId == null || it.id == deckId }
        return decks.flatMap { it.children }
    }
}
