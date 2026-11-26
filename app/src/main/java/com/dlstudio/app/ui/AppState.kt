package com.dlstudio.app.ui

import com.dlstudio.app.content.Topic
import com.dlstudio.app.progress.Progress
import com.dlstudio.app.settings.AccentColor
import com.dlstudio.app.settings.AppLang
import com.dlstudio.app.settings.ThemeMode
import com.dlstudio.app.speech.PackState
import com.dlstudio.app.speech.Pronounce
import com.dlstudio.app.speech.RawVoice

/** Сообщение под разделом «Темы» после установки файла. */
enum class Notice { NONE, FILE_FAILED }

/** Последняя награда: сколько XP, закончилась ли тема, выполнена ли цель дня, и номер (меняется при каждой награде, чтобы всплывашка показывалась снова). */
data class Gain(val xp: Int, val finished: Boolean, val id: Int, val goal: Boolean = false)

/**
 * Озвучка и распознавание речи: голоса встроенного движка (`voices`, пусто до первой загрузки — `loaded`), выбранные голоса по языкам,
 * скорость речи, закреплённые за персонажами голоса (`auto`) и состояние офлайн-пакетов распознавания по тегам языков тем.
 */
data class SpeechUi(
    val voices: List<RawVoice> = emptyList(), val loaded: Boolean = false, val chosen: Map<String, String> = emptyMap(),
    val rate: Float = 1f, val packs: Map<String, PackState> = emptyMap(), val auto: Map<String, String> = emptyMap()
)

/** Всё, что рисует интерфейс. `offer` — после установки тем предложить скачать недостающие языковые пакеты; `met` — персонажи, которых ученик уже встречал; `introduced` — курсы, чьих героев уже представили. */
data class AppState(
    val theme: ThemeMode,
    val lang: AppLang,
    val accent: AccentColor,
    val progress: Progress,
    val dailyGoal: Int,
    val lastTopic: String = "",
    val topics: List<Topic> = emptyList(),
    val notice: Notice = Notice.NONE,
    val offer: Boolean = false,
    val gain: Gain? = null,
    val pronounce: Pronounce = Pronounce(),
    val speech: SpeechUi = SpeechUi(),
    val met: Set<String> = emptySet(),
    val introduced: Set<String> = emptySet(),
    val crash: String? = null
)
