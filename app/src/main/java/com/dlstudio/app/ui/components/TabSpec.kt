package com.dlstudio.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.dlstudio.app.i18n.Strings

/** Описание вкладки: подпись (из текстов), два цвета градиента значка и рисунок значка. */
class TabSpec(
    val label: (Strings) -> String,
    val colors: List<Color>,
    val glyph: (DrawScope, Color) -> Unit
)
