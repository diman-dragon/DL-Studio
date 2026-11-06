package com.dlstudio.app.study

import java.util.TimeZone

/** Дни как целые числа по локальному времени: сутки не «плывут» и не усекаются к нулю. */
object DayMath {
    const val DAY = 86_400_000L

    /** Номер локального дня (0 — 1 января 1970); для времени до эпохи тоже корректен (floorDiv). */
    fun dayNumber(ms: Long, zone: TimeZone = TimeZone.getDefault()): Long =
        Math.floorDiv(ms + zone.getOffset(ms), DAY)

    /** День недели номера дня: 0 — понедельник … 6 — воскресенье. */
    fun weekday(day: Long): Int = Math.floorMod(day + 3, 7L).toInt()
}
