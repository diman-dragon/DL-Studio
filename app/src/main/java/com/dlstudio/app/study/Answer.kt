package com.dlstudio.app.study

/** Один ответ пользователя: что записать в журнал и как пересчитать расписание. */
data class Answer(val cardId: String, val grade: Grade, val faceMs: Long, val backMs: Long)
