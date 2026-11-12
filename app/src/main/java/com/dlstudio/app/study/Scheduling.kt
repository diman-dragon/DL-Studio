package com.dlstudio.app.study

import com.dlstudio.app.library.LibNode
import java.util.TimeZone
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Расписание повторов по SM-2 с тремя оценками. Интервалы целые, в днях:
 * 1 → 6 → ×ease. «Не знал» сбрасывает карточку и оставляет её на сегодня;
 * повтор раньше срока (практика) расписание не двигает, чтобы интервал нельзя было накрутить.
 */
object Sm2 {
    const val START_EASE = 2.5
    const val MIN_EASE = 1.3
    const val MAX_EASE = 2.8
    const val MAX_INTERVAL = 365

    private fun ease(value: Double) = value.coerceIn(MIN_EASE, MAX_EASE)

    private fun grown(prev: Int, factor: Double) = min(MAX_INTERVAL, max(prev + 1, (prev * factor).roundToInt()))

    fun next(prev: CardState?, grade: Grade, today: Long): CardState {
        val old = prev ?: CardState(0, START_EASE, 0, 0, today, today)
        if (grade == Grade.AGAIN) {
            val lapses = old.lapses + if (old.reps > 0) 1 else 0
            return CardState(1, ease(old.ease - 0.2), 0, lapses, today, today)
        }
        if (old.reps > 0 && today < old.dueDay) return old.copy(lastDay = today)
        val miss = 5 - grade.quality
        val ease = ease(old.ease + 0.1 - miss * (0.08 + miss * 0.02))
        val interval = when {
            old.reps == 0 -> 1
            old.reps == 1 -> if (grade == Grade.HARD) 3 else 6
            else -> grown(old.intervalDays, if (grade == Grade.HARD) 1.2 else ease)
        }
        return CardState(interval, ease, old.reps + 1, old.lapses, today + interval, today)
    }
}

/**
 * Оценка только по поведению, без кнопок. Мало времени до переворота — карточку не пытались вспомнить
 * («не знал»); нормальная пауза без подсказки — «знал»; подсказка или долгое раздумье — «трудно»;
 * подсказка и долгое раздумье вместе, как и слишком долгая пауза, — «не знал».
 */
object AutoGrade {
    const val GAVE_UP_MS = 800L
    const val SLOW_MS = 6_000L
    const val STUCK_MS = 15_000L

    /** Голосовые режимы: оценивает не время, а результат — верно, почти или нет (не сказал, не вышло, показал ответ). */
    fun ofSpeech(verdict: PronounceVerdict?, hinted: Boolean): Grade = when (verdict) {
        PronounceVerdict.OK -> if (hinted) Grade.HARD else Grade.KNOW
        PronounceVerdict.CLOSE -> Grade.HARD
        else -> Grade.AGAIN
    }

    fun of(faceMs: Long, hinted: Boolean): Grade = when {
        faceMs < GAVE_UP_MS -> Grade.AGAIN
        faceMs >= STUCK_MS || (hinted && faceMs >= SLOW_MS) -> Grade.AGAIN
        hinted || faceMs >= SLOW_MS -> Grade.HARD
        else -> Grade.KNOW
    }
}

/** Дни как целые числа по локальному времени: сутки не «плывут» и не усекаются к нулю. */
object DayMath {
    const val DAY = 86_400_000L

    /** Номер локального дня (0 — 1 января 1970); для времени до эпохи тоже корректен (floorDiv). */
    fun dayNumber(ms: Long, zone: TimeZone = TimeZone.getDefault()): Long =
        Math.floorDiv(ms + zone.getOffset(ms), DAY)

    /** День недели номера дня: 0 — понедельник … 6 — воскресенье. */
    fun weekday(day: Long): Int = Math.floorMod(day + 3, 7L).toInt()
}

/** Сколько карточек вернётся в каждый из ближайших дней, начиная с завтрашнего (сегодняшние и просроченные — в плане). */
object Forecast {
    fun next(states: Collection<CardState>, today: Long, days: Int = 7): List<Int> = (1..days).map { d -> states.count { it.dueDay == today + d } }
}

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

/** Расчёт статистики из расписания и журнала; без доступа к базе, поэтому проверяется тестами. */
object StatsCalc {
    const val DAYS = 14
    const val MATURE_DAYS = 21

    fun compute(total: Int, states: Collection<CardState>, perDay: Map<Long, DayAgg>, activeDays: Set<Long>, today: Long): StudyStats {
        val window = (today - DAYS + 1..today).map { perDay[it] }
        val answers = window.sumOf { it?.count ?: 0 }
        val face = window.sumOf { it?.faceMs ?: 0L }
        val recalled = window.sumOf { it?.recalled ?: 0 }
        return StudyStats(
            totalCards = total,
            newCards = (total - states.size).coerceAtLeast(0),
            studiedCards = states.size,
            dueCards = states.count { it.dueDay <= today },
            overdueCards = states.count { it.dueDay < today },
            matureCards = states.count { it.intervalDays >= MATURE_DAYS },
            todayReviews = window.last()?.count ?: 0,
            averageFaceMs = if (answers == 0) 0 else face / answers,
            accuracy = if (answers == 0) 0 else (recalled * 100.0 / answers).roundToInt(),
            streakDays = Streak.count(activeDays, today),
            bestStreak = Streak.longest(activeDays),
            weekReviews = window.takeLast(7).sumOf { it?.count ?: 0 },
            prevWeekReviews = window.dropLast(7).takeLast(7).sumOf { it?.count ?: 0 },
            yesterdayReviews = window[window.lastIndex - 1]?.count ?: 0,
            yesterdayAccuracy = window[window.lastIndex - 1]?.let { if (it.count == 0) 0 else (it.recalled * 100.0 / it.count).roundToInt() } ?: 0,
            reviewsByDay = window.map { it?.count ?: 0 },
            intervalBuckets = listOf(1, 2, 7, 30).mapIndexed { i, from ->
                states.count { it.intervalDays >= from && (i == 3 || it.intervalDays < listOf(2, 7, 30)[i]) }
            }
        )
    }
}

/**
 * Выбор учебного режима. Половина показов (`flipShare`, по умолчанию 50%) — выбор перевода и выбор слова
 * (изучаемое → родное и родное → изучаемое поровну). Остальное — по весам среди включённых режимов. Простого «перевернуть» здесь нет: всегда нужно ответить. «Произношение» — только для одного-двух слов.
 */
object ModeWeights {
    /** Самые частые показы: слово → выбор перевода и перевод → выбор слова (карточка переворачивается после ответа). */
    private val frequent = listOf(CardMode.CHOICE_FWD, CardMode.CHOICE_REV)
    /** Веса в процентах от «остального» (при доле выбора 50% это половина показов): ввод слова — по 6, то есть по 3% всех показов. */
    private val weights = listOf(
        CardMode.LISTEN to 33, CardMode.WRITE_FWD to 6, CardMode.WRITE_REV to 6, CardMode.PRONOUNCE to 31, CardMode.REPEAT to 24
    )

    fun eligible(card: LibNode, mode: CardMode): Boolean =
        mode != CardMode.PRONOUNCE || (card.back.isNotBlank() && card.name.trim().split(Regex("\\s+")).size <= 2)

    fun modeFor(card: LibNode, index: Int, options: StudyOptions = StudyOptions()): CardMode {
        val random = Random(card.id.hashCode() * 31L + index)
        val usable = options.modes.filter { eligible(card, it) }.ifEmpty { listOf(CardMode.CHOICE_FWD) }
        val flips = usable.filter { it in frequent }
        val rest = weights.filter { it.first in usable }
        if (flips.isNotEmpty() && (rest.isEmpty() || random.nextInt(100) < options.flipShare)) return flips[random.nextInt(flips.size)]
        var slot = random.nextInt(rest.sumOf { it.second })
        return rest.first { (_, w) -> if (slot < w) true else { slot -= w; false } }.first
    }

    fun optionsFor(card: LibNode, queue: List<LibNode>, reverse: Boolean, index: Int): List<String> {
        val correct = if (reverse) card.name else card.back
        val pool = queue.asSequence().filter { it.id != card.id }
            .map { if (reverse) it.name else it.back }
            .filter { it.isNotBlank() && it != correct }
            .distinct().toList()
        val distractors = pool.shuffled(Random(card.id.hashCode())).take(3)
        return (distractors + correct).shuffled(Random(card.id.hashCode() + index * 31))
    }
}

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

/** Случайный порядок для круга карточек; новый круг никогда не повторяет прежний порядок. */
object Shuffle {
    fun differentOrder(cards: List<LibNode>, previous: List<String>, random: Random = Random.Default): List<LibNode> {
        if (cards.size < 2) return cards
        repeat(TRIES) { val order = cards.shuffled(random); if (order.map { it.id } != previous) return order }
        return cards.drop(1) + cards.first()
    }

    private const val TRIES = 10
}

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
