package com.dlstudio.app.study

import com.dlstudio.app.library.LibNode

/** Сводка по набору карточек: новые, к повтору (включая просроченные) и просроченные — одинаково везде. */
data class CardCounts(val total: Int, val fresh: Int, val due: Int, val overdue: Int) {
    companion object {
        fun of(cards: List<LibNode>, states: Map<String, CardState>, today: Long): CardCounts {
            val known = cards.mapNotNull { states[it.id] }
            return CardCounts(cards.size, cards.size - known.size, known.count { it.dueDay <= today }, known.count { it.dueDay < today })
        }
    }
}
