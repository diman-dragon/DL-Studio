package com.dlstudio.app.ui.components

import androidx.compose.runtime.Composable
import com.dlstudio.app.study.CardPlan
import com.dlstudio.app.study.Speak

/**
 * Область ответа под карточкой: у карточки с переворотом её нет; в режимах голоса — микрофон; в режимах «письмо» —
 * ввод с клавиатуры, иначе четыре варианта. Голосовой ответ завершается тапом по карточке.
 */
@Composable
fun CardAnswers(
    plan: CardPlan, selected: String?, submitted: Boolean,
    onPick: (String) -> Unit, onSubmit: () -> Unit, onSpeak: Speak
) {
    val mode = plan.mode
    when {
        mode.flip -> Unit
        mode.speaks -> Unit
        mode.typed -> CardWriteInput(plan.correct, submitted, { onSubmit() }, onSpeak = if (mode.reverse) onSpeak else null)
        else -> ChoiceOptions(plan.options, plan.correct, selected, onPick)
    }
}
