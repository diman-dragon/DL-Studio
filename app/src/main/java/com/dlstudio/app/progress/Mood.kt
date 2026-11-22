package com.dlstudio.app.progress

/** Как у пользователя дела прямо сейчас — выводится только из того, что он делал (XP и день последнего занятия). */
enum class Mood { FIRST_STEP, DONE_TODAY, KEEP_STREAK, COMEBACK }

/** Часть суток для приветствия. */
enum class DayPart { MORNING, DAY, EVENING, NIGHT }

/** Выбор приветствия, настроения и «слова дня» без каких-либо самооценок пользователя. */
object Motivation {
    fun mood(p: Progress, today: Long): Mood = when {
        p.xp == 0 -> Mood.FIRST_STEP
        p.lastDay >= today -> Mood.DONE_TODAY
        p.lastDay == today - 1 -> Mood.KEEP_STREAK
        else -> Mood.COMEBACK
    }

    fun dayPart(hour: Int): DayPart = when (hour) {
        in 5..10 -> DayPart.MORNING
        in 11..16 -> DayPart.DAY
        in 17..22 -> DayPart.EVENING
        else -> DayPart.NIGHT
    }

    /** Элемент списка на сегодня: один и тот же весь день, назавтра — следующий. */
    fun <T> ofDay(items: List<T>, today: Long, salt: Int = 0): T = items[Math.floorMod(today + salt, items.size.toLong()).toInt()]
}
