package com.dlstudio.app.study

/** Оценка ответа после переворота карточки; `quality` — шкала SM-2 (0–5). */
enum class Grade(val quality: Int) { AGAIN(1), HARD(3), KNOW(5) }
