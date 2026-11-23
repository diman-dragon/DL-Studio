package com.dlstudio.app.ui

import com.dlstudio.app.content.CatalogEntry
import com.dlstudio.app.content.Topic
import com.dlstudio.app.progress.Progress
import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.AppLang
import com.dlstudio.app.settings.ThemeMode
import com.dlstudio.app.speech.PackState
import com.dlstudio.app.speech.Pronounce
import com.dlstudio.app.speech.RawVoice

/** Сообщение под разделом «Темы» после действия с файлом или загрузкой. */
enum class Notice { NONE, DOWNLOAD_FAILED, FILE_FAILED }

/** Последняя награда: сколько XP, закончилась ли тема, выполнена ли цель дня, и номер (меняется при каждой награде, чтобы всплывашка показывалась снова). */
data class Gain(val xp: Int, val finished: Boolean, val id: Int, val goal: Boolean = false)

/**
 * Озвучка и распознавание речи: голоса встроенного движка (`voices`, пусто до первой загрузки — `loaded`), выбранные голоса по языкам,
 * скорость речи и состояние офлайн-пакетов распознавания по тегам языков тем.
 */
data class SpeechUi(
    val voices: List<RawVoice> = emptyList(), val loaded: Boolean = false, val chosen: Map<String, String> = emptyMap(),
    val rate: Float = 1f, val packs: Map<String, PackState> = emptyMap()
)

/** Всё, что рисует интерфейс. */
data class AppState(
    val theme: ThemeMode,
    val lang: AppLang,
    val accent: AccentColor,
    val catalogUrl: String,
    val progress: Progress,
    val dailyGoal: Int,
    val topics: List<Topic> = emptyList(),
    val catalog: List<CatalogEntry> = emptyList(),
    val catalogLoading: Boolean = false,
    val catalogFailed: Boolean = false,
    val busy: Set<String> = emptySet(),
    val notice: Notice = Notice.NONE,
    val gain: Gain? = null,
    val pronounce: Pronounce = Pronounce(),
    val speech: SpeechUi = SpeechUi()
)
