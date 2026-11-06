package com.dlstudio.app.study

/**
 * Снимок для напоминания: приёмнику будильника не нужна база, ему хватает числа карточек по дням.
 * Хранится строкой в настройках: `новые;последний_день_занятий;день:число,день:число`.
 */
data class ReminderSnapshot(val newCards: Int, val lastStudyDay: Long, val dueByDay: Map<Long, Int>) {
    fun dueCount(today: Long): Int = dueByDay.filterKeys { it <= today }.values.sum()

    /** Напоминать, если есть что повторять, или есть новые карточки, а сегодня ещё не занимались. */
    fun shouldNotify(today: Long): Boolean = dueCount(today) > 0 || (newCards > 0 && lastStudyDay < today)

    fun encode(): String = "$newCards;$lastStudyDay;" + dueByDay.entries.joinToString(",") { "${it.key}:${it.value}" }

    companion object {
        fun of(newCards: Int, lastStudyDay: Long, states: Collection<CardState>) =
            ReminderSnapshot(newCards, lastStudyDay, states.groupingBy { it.dueDay }.eachCount())

        fun decode(text: String?): ReminderSnapshot? = runCatching {
            val (new, last, days) = text!!.split(';')
            ReminderSnapshot(new.toInt(), last.toLong(), days.split(',').filter { it.isNotEmpty() }.associate { p -> p.substringBefore(':').toLong() to p.substringAfter(':').toInt() })
        }.getOrNull()
    }
}
