package com.dlstudio.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import com.dlstudio.app.study.StudyOptions

/** Настройки учёбы (режимы карточек, микрофон, поздравления) для любого места интерфейса без протаскивания параметров. */
val LocalStudyOptions = staticCompositionLocalOf { StudyOptions() }
