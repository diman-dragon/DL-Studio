package com.dlstudio.app.library

/** Таблица «слово; перевод» → колоды с карточками. Первая колонка — лицевая сторона, вторая — обратная. */
object ImportTable {
    private val FRONT = setOf("front", "word", "term", "слово", "лицевая", "лицевая сторона")
    private val BACK = setOf("back", "translation", "definition", "перевод", "обратная", "обратная сторона")
    private val DECK = setOf("deck", "колода")

    /**
     * Строка-заголовок (все её ячейки — известные названия колонок) задаёт порядок колонок; колонка «deck» делит
     * карточки на колоды, без неё всё идёт в одну колоду `name`. Строки без второй колонки пропускаются.
     */
    fun decks(text: String, name: String): List<LibNode> {
        val all = TableRows.read(text)
        val head = all.firstOrNull().orEmpty().map { it.lowercase() }
        val header = head.isNotEmpty() && head.all { it in FRONT || it in BACK || it in DECK }
        val front = if (header) column(head, FRONT, 0) else 0
        val back = if (header) column(head, BACK, 1) else 1
        val deck = if (header) column(head, DECK, -1) else -1
        val rows = (if (header) all.drop(1) else all).filter { it.size > 1 && it.at(front).isNotBlank() }
        return rows.groupBy { it.at(deck).ifBlank { name } }.map { (title, group) ->
            LibNode("", title, children = group.map { LibNode("", it.at(front), it.at(back)) })
        }
    }

    private fun column(head: List<String>, names: Set<String>, default: Int): Int =
        head.indexOfFirst { it in names }.takeIf { it >= 0 } ?: default

    private fun List<String>.at(index: Int): String = getOrElse(index) { "" }.trim()
}
