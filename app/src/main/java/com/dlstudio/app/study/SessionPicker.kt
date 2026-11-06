package com.dlstudio.app.study

import com.dlstudio.app.library.LibNode

/** Состав учебной сессии: сначала просроченные и сегодняшние (самые давние первыми), потом новые. */
object SessionPicker {
    fun pick(cards: List<LibNode>, states: Map<String, CardState>, today: Long, limit: Int, extra: Boolean = false): List<LibNode> {
        val due = cards.filter { (states[it.id]?.dueDay ?: Long.MAX_VALUE) <= today }.sortedBy { states.getValue(it.id).dueDay }
        val fresh = cards.filter { it.id !in states }
        val plan = (due + fresh).take(limit)
        if (plan.isNotEmpty() || !extra) return plan
        return cards.filter { it.id in states }.sortedBy { states.getValue(it.id).dueDay }.take(limit)
    }
}
