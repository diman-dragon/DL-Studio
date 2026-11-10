package com.dlstudio.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.dlstudio.app.i18n.Strings

/** Описание вкладки: подпись, цвета градиента, иконка в выбранном (залитая) и обычном (контурная) виде; `active = false` — вкладка приглушена. */
class TabSpec(
    val label: (Strings) -> String,
    val colors: List<Color>,
    @DrawableRes val fill: Int,
    @DrawableRes val outline: Int,
    val active: Boolean = true
)
