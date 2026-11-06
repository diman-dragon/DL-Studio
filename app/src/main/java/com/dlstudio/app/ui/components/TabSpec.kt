package com.dlstudio.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.dlstudio.app.i18n.Strings

/** Описание вкладки: подпись, цвета градиента, иконка в активном (залитая) и обычном (контурная) виде. */
class TabSpec(
    val label: (Strings) -> String,
    val colors: List<Color>,
    @DrawableRes val fill: Int,
    @DrawableRes val outline: Int
)
