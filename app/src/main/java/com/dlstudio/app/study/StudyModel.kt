package com.dlstudio.app.study

import com.dlstudio.app.library.LibNode

/** Один ответ пользователя: что записать в журнал и как пересчитать расписание. */
data class Answer(val cardId: String, val grade: Grade, val faceMs: Long, val backMs: Long)

/** Сводка по набору карточек: новые, к повтору (включая просроченные) и просроченные — одинаково везде. */
data class CardCounts(val total: Int, val fresh: Int, val due: Int, val overdue: Int) {
    companion object {
        fun of(cards: List<LibNode>, states: Map<String, CardState>, today: Long): CardCounts {
            val known = cards.mapNotNull { states[it.id] }
            return CardCounts(cards.size, cards.size - known.size, known.count { it.dueDay <= today }, known.count { it.dueDay < today })
        }
    }
}

/**
 * Режим показа карточки в сессии. `reverse` — на лицевой стороне родное слово (перевод), на обороте изучаемое;
 * `speaks` — отвечают голосом в микрофон. Режим выбирается через [ModeWeights].
 */
enum class CardMode(val reverse: Boolean = false, val speaks: Boolean = false) {
    /** Только просмотр из библиотеки: изучаемое слово, тап — перевод. В интервальном повторении не бывает: там всегда нужно отвечать. */
    FLIP_FWD,
    /** Показывается иностранное слово → 4 варианта перевода на родном языке. */
    CHOICE_FWD,
    /** Показывается родное слово → 4 варианта иностранного. */
    CHOICE_REV(reverse = true),
    /** Произносится слово → 4 варианта; текст скрыт, через 4 с появляется подсказка. */
    LISTEN,
    /** Пользователь пишет перевод. */
    WRITE_FWD,
    /** Пользователь пишет слово на иностранном языке. */
    WRITE_REV(reverse = true),
    /** Показано родное слово (одно-два слова): нужно произнести его на изучаемом языке. */
    PRONOUNCE(reverse = true, speaks = true),
    /** Диктор произносит слово или предложение: нужно повторить за ним. */
    REPEAT(speaks = true);

    val flip: Boolean get() = this == FLIP_FWD
    val typed: Boolean get() = this == WRITE_FWD || this == WRITE_REV
    val choice: Boolean get() = !flip && !typed && !speaks

    companion object {
        /** Режимы интервального повторения (все, кроме просмотра): их включают и выключают в настройках. */
        val studied: Set<CardMode> = values().filter { !it.flip }.toSet()
    }
}

/** Всё, что нужно показать для карточки на странице сессии: режим, фраза для голосовых режимов, правильный ответ и варианты. */
data class CardPlan(val mode: CardMode, val target: String, val correct: String, val options: List<String>) {
    companion object {
        /** Просмотр без вопросов (библиотека, карточка без перевода) — обычная карточка с переворотом. */
        fun of(card: LibNode, index: Int, queue: List<LibNode>, interactive: Boolean, study: StudyOptions): CardPlan {
            val mode = if (interactive) ModeWeights.modeFor(card, index, study) else CardMode.FLIP_FWD
            val correct = if (mode.reverse) card.name else card.back
            val options = if (interactive && mode.choice) ModeWeights.optionsFor(card, queue, mode.reverse, index) else emptyList()
            return CardPlan(mode, SpeakTarget.of(card, mode, index), correct, options)
        }
    }
}

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

/** Оценка ответа после переворота карточки; `quality` — шкала SM-2 (0–5). */
enum class Grade(val quality: Int) { AGAIN(1), HARD(3), KNOW(5) }

/** Какую похвалу показать на экране статистики. */
enum class PraiseKind { RECORD_DAY, UP_TODAY, BEST_STREAK, WEEK_UP, ACCURACY, KEEP }

/** Какое сообщение показать на дашборде «Учёба». */
enum class MotivationKind { EMPTY, START, DONE_TODAY, STREAK, CATCH_UP, MATURE, KEEP }

/** Итог проверки произношения: сказано верно, почти верно или не то. */
enum class PronounceVerdict { OK, CLOSE, MISS }

/**
 * Что включено в учёбе: режимы карточек (хотя бы один всегда включён), кнопка проверки произношения, поздравления с анимацией
 * и доля показов «выбора перевода» и «выбора слова» (`flipShare`, %). `slowRate` и `slowPause` — темп (%) и длина пауз (% от обычной) озвучки «помедленнее». Все изменения возвращают новое значение; последний включённый режим выключить нельзя.
 */
data class StudyOptions(
    val modes: Set<CardMode> = CardMode.studied,
    val pronounce: Boolean = true,
    val celebrate: Boolean = true,
    val flipShare: Int = DEFAULT_FLIP_SHARE,
    val slowRate: Int = DEFAULT_SLOW_RATE,
    val slowPause: Int = DEFAULT_SLOW_PAUSE
) {
    fun withMode(mode: CardMode, on: Boolean): StudyOptions {
        val next = if (on) modes + mode else modes - mode
        return if (next.isEmpty()) this else copy(modes = next)
    }

    fun isLastMode(mode: CardMode): Boolean = modes.size == 1 && mode in modes

    companion object {
        const val DEFAULT_FLIP_SHARE = 50
        const val DEFAULT_SLOW_RATE = 75
        const val DEFAULT_SLOW_PAUSE = 100
        val SLOW_RATES = listOf(85, 75, 60, 50)
        val SLOW_PAUSES = listOf(0, 60, 100, 160)
    }
}

/** Какие карточки учим: `null` вместо проекта — вся библиотека, `null` вместо колоды — все колоды проекта. */
object StudyScope {
    fun cards(items: List<LibNode>, projectId: String?, deckId: String?): List<LibNode> {
        val projects = if (projectId == null) items else items.filter { it.id == projectId }
        val decks = projects.flatMap { it.children }.filter { deckId == null || it.id == deckId }
        return decks.flatMap { it.children }
    }
}

/** Всё, что показывает экран статистики. Графики идут от старого дня к сегодняшнему. */
data class StudyStats(
    val totalCards: Int = 0,
    val newCards: Int = 0,
    val studiedCards: Int = 0,
    val dueCards: Int = 0,
    val overdueCards: Int = 0,
    val matureCards: Int = 0,
    val todayReviews: Int = 0,
    val averageFaceMs: Long = 0,
    val accuracy: Int = 0,
    val streakDays: Int = 0,
    val bestStreak: Int = 0,
    val weekReviews: Int = 0,
    val prevWeekReviews: Int = 0,
    val yesterdayReviews: Int = 0,
    val yesterdayAccuracy: Int = 0,
    val reviewsByDay: List<Int> = List(StatsCalc.DAYS) { 0 },
    val intervalBuckets: List<Int> = List(4) { 0 }
)

/** Итог одного дня из журнала: сколько ответов, суммарное время до переворота и сколько ответов были не «Не знал». */
data class DayAgg(val count: Int, val faceMs: Long, val recalled: Int)

/** Сессия, сохранённая в базе (`random` — режим «Учить» из библиотеки, хранится в `all_decks`): порядок карточек, позиция и уже полученные оценки. */
data class SavedSession(val id: String, val startedAt: Long, val position: Int, val cardIds: List<String>, val grades: Map<String, Grade>, val random: Boolean = false)
