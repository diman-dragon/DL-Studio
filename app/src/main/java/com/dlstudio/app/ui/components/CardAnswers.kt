package com.dlstudio.app.ui.components

import androidx.compose.runtime.Composable
import com.dlstudio.app.study.CardMode

/** Область ответа под карточкой: ввод с клавиатуры в режимах «письмо», иначе четыре варианта; `onSpeak` — динамик у правильного слова на изучаемом языке. */
@Composable
fun CardAnswers(mode: CardMode, correct: String, options: List<String>, selected: String?, submitted: Boolean, onPick: (String) -> Unit, onSubmit: () -> Unit, onSpeak: ((String) -> Unit)?) {
    if (mode == CardMode.WRITE_FWD || mode == CardMode.WRITE_REV) CardWriteInput(correct, submitted, { onSubmit() }, onSpeak = onSpeak)
    else ChoiceOptions(options, correct, selected, onPick)
}
