package com.dlstudio.app.study

/** Скорость озвучки: обычная, «медленно» и «ещё медленнее». Общей настройки скорости и высоты голоса нет — только эти три кнопки. */
enum class SpeechSpeed(val rate: Float) { NORMAL(1f), SLOW(0.65f), SLOWER(0.4f) }
