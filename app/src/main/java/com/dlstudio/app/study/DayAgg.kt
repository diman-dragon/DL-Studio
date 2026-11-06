package com.dlstudio.app.study

/** Итог одного дня из журнала: сколько ответов, суммарное время до переворота и сколько ответов были не «Не знал». */
data class DayAgg(val count: Int, val faceMs: Long, val recalled: Int)
