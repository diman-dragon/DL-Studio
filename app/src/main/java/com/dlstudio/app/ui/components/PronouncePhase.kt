package com.dlstudio.app.ui.components

import com.dlstudio.app.study.PronounceVerdict

/** Состояние проверки произношения на карточке; FAILED — служба ответила ошибкой (код показывается), UNAVAILABLE — службы нет совсем. */
enum class PronouncePhase {
    IDLE, LISTENING, OK, CLOSE, MISS, NO_SPEECH, UNAVAILABLE, FAILED, DENIED;

    /** Итог сравнения, если он уже есть. */
    val verdict: PronounceVerdict?
        get() = when (this) { OK -> PronounceVerdict.OK; CLOSE -> PronounceVerdict.CLOSE; MISS -> PronounceVerdict.MISS; else -> null }

    /** Попытка закончена (есть итог или служба не сработала): карточку можно перевернуть. «Не расслышал» — ещё можно повторить. */
    val finished: Boolean get() = this != IDLE && this != LISTENING && this != NO_SPEECH
}
