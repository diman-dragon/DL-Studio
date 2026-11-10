package com.dlstudio.app.ui.components

import androidx.compose.ui.graphics.Color
import com.dlstudio.app.Features
import com.dlstudio.app.R
import com.dlstudio.app.i18n.Strings

/** Четыре вкладки по порядку свайпа: Учёба → Статистика → Библиотека → Настройки (иконки Material Design Icons). */
object Tabs {
    val all = listOf(
        TabSpec(Strings::tabStudy, listOf(Color(0xFFFFB02E), Color(0xFFFF3D77)), R.drawable.ic_tab_study_fill, R.drawable.ic_tab_study_outline, Features.STUDY_HOME),
        TabSpec(Strings::tabStats, listOf(Color(0xFF2EE6FF), Color(0xFF4A5BFF)), R.drawable.ic_tab_stats_fill, R.drawable.ic_tab_stats_outline),
        TabSpec(Strings::tabLibrary, listOf(Color(0xFFA77BFF), Color(0xFFFF4FD8)), R.drawable.ic_tab_library_fill, R.drawable.ic_tab_library_outline),
        TabSpec(Strings::tabSettings, listOf(Color(0xFF3DF08F), Color(0xFF00B4D8)), R.drawable.ic_tab_settings_fill, R.drawable.ic_tab_settings_outline)
    )
}
