package com.dlstudio.app.ui

import com.dlstudio.app.content.CatalogEntry
import com.dlstudio.app.content.Topic
import com.dlstudio.app.progress.Progress
import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.AppLang
import com.dlstudio.app.settings.ThemeMode
import com.dlstudio.app.speech.Pronounce

/** Сообщение под разделом «Темы» после действия с файлом или загрузкой. */
enum class Notice { NONE, DOWNLOAD_FAILED, FILE_FAILED }

/** Последняя награда: сколько XP, закончилась ли тема, и номер (меняется при каждой награде, чтобы всплывашка показывалась снова). */
data class Gain(val xp: Int, val finished: Boolean, val id: Int)

/** Всё, что рисует интерфейс. */
data class AppState(
    val theme: ThemeMode,
    val lang: AppLang,
    val accent: AccentColor,
    val catalogUrl: String,
    val progress: Progress,
    val topics: List<Topic> = emptyList(),
    val catalog: List<CatalogEntry> = emptyList(),
    val catalogLoading: Boolean = false,
    val catalogFailed: Boolean = false,
    val busy: Set<String> = emptySet(),
    val notice: Notice = Notice.NONE,
    val gain: Gain? = null,
    val pronounce: Pronounce = Pronounce()
)
