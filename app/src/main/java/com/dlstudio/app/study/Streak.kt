package com.dlstudio.app.study

/** Серия дней подряд. Пока сегодня не занимались, серия не обрывается: считается от вчера. */
object Streak {
    fun count(activeDays: Set<Long>, today: Long): Int {
        var day = if (today in activeDays) today else today - 1
        var n = 0
        while (day in activeDays) { n++; day-- }
        return n
    }

    /** Самая длинная серия за всё время, что есть в `activeDays`: считаем от начала каждой серии. */
    fun longest(activeDays: Set<Long>): Int =
        activeDays.filter { it - 1 !in activeDays }.maxOfOrNull { start -> generateSequence(start) { it + 1 }.takeWhile { it in activeDays }.count() } ?: 0
}
