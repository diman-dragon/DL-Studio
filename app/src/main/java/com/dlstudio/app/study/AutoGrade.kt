package com.dlstudio.app.study

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
