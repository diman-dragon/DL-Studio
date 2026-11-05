package com.lingvadesk.app.domain.srs

import com.lingvadesk.app.domain.model.Grade

/** Сигналы поведения на карточке (мс). `think` — до переворота (null — не переворачивали), `look` — на ответе. */
data class Signals(
    val think: Long? = null,
    val look: Long = 0,
    val dwell: Long = 0,
    val flips: Int = 0,
    val revisit: Boolean = false,
    val away: Boolean = false
)

/** Оценка по поведению (порт `srs/inferGrade.js`, `srs/speechGrade.js`); null — расписание не трогаем. */
object GradeInference {
    const val MIN_DWELL = 1_500L       // меньше — просто пролистал
    const val QUICK_FLIP = 1_500L      // быстрее — рефлекторный переворот, не «легко»
    const val EASY_RECALL_UNTIL = 3_000L
    const val HESITATE = 5_000L
    const val STRUGGLE = 8_000L
    const val LONG_READ = 5_000L
    const val MATURE_REPS = 3
    const val SPEECH_EASY = 0.9
    const val SPEECH_GOOD = 0.75
    const val SPEECH_HARD = 0.5

    fun speechGrade(score: Double, attempts: Int, hinted: Boolean): Grade {
        val base = when {
            score >= SPEECH_EASY -> if (attempts <= 1) Grade.EASY else Grade.GOOD
            score >= SPEECH_GOOD -> Grade.GOOD
            score >= SPEECH_HARD -> Grade.HARD
            else -> Grade.AGAIN
        }
        // с подсказкой — на ступень ниже, но «трудно» не опускается до «снова»
        return if (hinted && base != Grade.AGAIN && base != Grade.HARD) base.down() else base
    }

    fun infer(s: Signals, reps: Int): Grade? {
        if (s.away) return null
        if (s.dwell < MIN_DWELL) return null

        val think = s.think
        if (think == null || s.flips == 0) {
            return when {
                s.dwell >= STRUGGLE -> Grade.HARD
                reps >= MATURE_REPS && s.dwell < 4_000L -> Grade.EASY
                else -> Grade.GOOD
            }
        }

        var grade = when {
            think >= STRUGGLE -> if (s.look >= LONG_READ) Grade.AGAIN else Grade.HARD
            think >= HESITATE -> if (s.look >= LONG_READ) Grade.HARD else Grade.GOOD
            think in QUICK_FLIP until EASY_RECALL_UNTIL && s.look < LONG_READ -> Grade.EASY
            else -> Grade.GOOD
        }
        if (s.flips > 2) grade = grade.down()      // метался туда-обратно
        if (s.revisit) grade = grade.down()        // возвращался к карточке
        return grade
    }
}
