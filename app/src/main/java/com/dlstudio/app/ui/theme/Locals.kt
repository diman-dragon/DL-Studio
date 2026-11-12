package com.dlstudio.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import com.dlstudio.app.i18n.Strings
import com.dlstudio.app.i18n.StringsRu
import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.FontSettings
import com.dlstudio.app.study.Speak
import com.dlstudio.app.study.StudyOptions

/** Выбранный акцентный цвет для любого экрана (градиенты кнопок и плиток). */
val LocalAccent = staticCompositionLocalOf { AccentColor.VIOLET }

/** Язык (BCP 47) выбранного голоса озвучки: по нему распознаётся и произношение; null — определить по слову. */
val LocalSpeechTag = staticCompositionLocalOf<String?> { null }

/** Тексты текущего языка для любого экрана. */
val LocalStrings = staticCompositionLocalOf<Strings> { StringsRu }

/** Настройки учёбы (режимы карточек, микрофон, поздравления) для любого места интерфейса без протаскивания параметров. */
val LocalStudyOptions = staticCompositionLocalOf { StudyOptions() }

/** Шрифты групп текста для любого места интерфейса без протаскивания параметров. */
val LocalFonts = staticCompositionLocalOf { FontSettings() }

/** Медленная озвучка текста: берётся из `AppViewModel`; кнопки «🐢» вызывают её без протаскивания параметров. */
val LocalSpeakSlow = staticCompositionLocalOf<Speak> { {} }
