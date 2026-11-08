package com.dlstudio.app.ui.components

import androidx.compose.runtime.Composable
import com.dlstudio.app.study.CardPlan
import com.dlstudio.app.study.Speak

/**
 * Область ответа под карточкой: у карточки с переворотом её нет; в режимах голоса — микрофон; в режимах «письмо» —
 * ввод с клавиатуры, иначе четыре варианта. `onSpeak` — озвучка правильного слова на изучаемом языке (только в режиме «Ввод слова»).
 */
@Composable
fun CardAnswers(
    plan: CardPlan, selected: String?, submitted: Boolean, pronounce: PronounceState,
    onPick: (String) -> Unit, onSubmit: () -> Unit, onReveal: () -> Unit, onSpeak: Speak
) {
    val mode = plan.mode
    when {
        mode.flip -> Unit
        mode.speaks -> SpeakAnswer(pronounce, onReveal)
        mode.typed -> CardWriteInput(plan.correct, submitted, { onSubmit() }, onSpeak = if (mode.reverse) onSpeak else null)
        else -> ChoiceOptions(plan.options, plan.correct, selected, onPick)
    }
}
