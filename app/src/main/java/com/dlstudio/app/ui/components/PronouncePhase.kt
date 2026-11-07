package com.dlstudio.app.ui.components

/** Состояние проверки произношения на карточке; FAILED — служба ответила ошибкой (код показывается), UNAVAILABLE — службы нет совсем. */
enum class PronouncePhase { IDLE, LISTENING, OK, CLOSE, MISS, NO_SPEECH, UNAVAILABLE, FAILED, DENIED }
