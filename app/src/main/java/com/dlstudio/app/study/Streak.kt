package com.dlstudio.app.study

/** Серия дней подряд. Пока сегодня не занимались, серия не обрывается: считается от вчера. */
object Streak {
    fun count(activeDays: Set<Long>, today: Long): Int {
        var day = if (today in activeDays) today else today - 1
        var n = 0
        while (day in activeDays) { n++; day-- }
        return n
    }
}
