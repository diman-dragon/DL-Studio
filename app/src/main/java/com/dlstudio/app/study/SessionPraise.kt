package com.dlstudio.app.study

/** Уровень похвалы за сессию: 0 — поддержка, 1 — «хорошо», 2 — «отлично» (знал 80% и больше). */
object SessionPraise {
    fun level(know: Int, total: Int): Int = when {
        total == 0 -> 0
        know * 100 >= total * 80 -> 2
        know * 100 >= total * 50 -> 1
        else -> 0
    }
}
