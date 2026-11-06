package com.dlstudio.app.study

/**
 * Расписание одной карточки. Все даты — номера локальных дней (см. `DayMath`), поэтому
 * «к повтору» и «просрочено» считаются от начала дня, а не от текущей минуты.
 */
data class CardState(
    val intervalDays: Int,
    val ease: Double,
    val reps: Int,
    val lapses: Int,
    val dueDay: Long,
    val lastDay: Long
)
