package com.dlstudio.app.study

/** Сколько карточек вернётся в каждый из ближайших дней, начиная с завтрашнего (сегодняшние и просроченные — в плане). */
object Forecast {
    fun next(states: Collection<CardState>, today: Long, days: Int = 7): List<Int> = (1..days).map { d -> states.count { it.dueDay == today + d } }
}
