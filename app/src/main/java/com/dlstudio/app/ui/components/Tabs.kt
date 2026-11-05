package com.dlstudio.app.ui.components

import androidx.compose.ui.graphics.Color
import com.dlstudio.app.i18n.Strings

/** Четыре вкладки по порядку свайпа: Учёба → Статистика → Библиотека → Настройки. */
object Tabs {
    val all = listOf(
        TabSpec(Strings::tabStudy, listOf(Color(0xFFFFA23D), Color(0xFFFF3D77)), { d, c -> d.glyphStudy(c) }),
        TabSpec(Strings::tabStats, listOf(Color(0xFF29D3FF), Color(0xFF3D5AFE)), { d, c -> d.glyphStats(c) }),
        TabSpec(Strings::tabLibrary, listOf(Color(0xFF9B6BFF), Color(0xFFE040FB)), { d, c -> d.glyphLibrary(c) }),
        TabSpec(Strings::tabSettings, listOf(Color(0xFF2CE68A), Color(0xFF00B8D4)), { d, c -> d.glyphSettings(c) })
    )
}
