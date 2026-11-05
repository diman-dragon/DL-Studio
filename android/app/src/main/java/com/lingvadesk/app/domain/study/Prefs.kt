package com.lingvadesk.app.domain.study

/** Значения настроек, которые переключаются по кругу (порт `constants/PREF_OPTIONS`, `DECK_PREFS`). */
data class Option(val value: String, val label: String)

object PrefOptions {
    val theme = listOf(Option("auto", "Системная"), Option("light", "Бумага"), Option("dark", "Чёрная AMOLED"))
    val font = listOf(Option("s", "Мелкий"), Option("m", "Средний"), Option("l", "Крупный"))
    val layout = listOf(Option("list", "Список"), Option("grid", "Сетка"))
    val newPerDay = listOf("3", "5", "10", "15", "20", "30", "50").map { Option(it, "$it в день") }
    val sessionMax = listOf(
        Option("10", "10 в сессии"), Option("20", "20 в сессии"), Option("30", "30 в сессии"),
        Option("50", "50 в сессии"), Option("100", "100 в сессии"), Option("9999", "все в сессии")
    )
    val pace = listOf(
        Option("0.75", "Чаще"), Option("1", "Обычный"), Option("1.3", "Реже"), Option("1.7", "Гораздо реже")
    )
    val order = listOf(Option("shuffle", "Вперемешку"), Option("seq", "По порядку"))
    val speechRate = listOf(Option("0.7", "Медленно"), Option("1", "Обычно"), Option("1.3", "Быстро"))
    val onOff = listOf(Option("1", "Вкл"), Option("0", "Выкл"))

    /** Следующее значение по кругу; неизвестное → первое (`util/nextOption`). */
    fun next(options: List<Option>, current: String?): Option {
        val i = options.indexOfFirst { it.value == current }
        return if (i < 0) options.first() else options[(i + 1) % options.size]
    }

    fun labelOf(options: List<Option>, value: String?, fallback: String = options.first().label): String =
        options.firstOrNull { it.value == value }?.label ?: fallback
}

/** Ключи таблицы `settings` и значения по умолчанию. Параметры колоды — `deck:<id>:<параметр>` (без миграции БД). */
object PrefKeys {
    const val THEME = "theme"
    const val FONT = "font"
    const val LAYOUT = "layout"
    const val NEW_PER_DAY = "newPerDay"
    const val SESSION_MAX = "sessionMax"
    const val SPEECH_RATE = "speechRate"
    const val VIBRATE = "vibrate"
    const val KEEP_AWAKE = "keepAwake"

    const val DECK_NEW_PER_DAY = "newPerDay"
    const val DECK_SESSION_MAX = "sessionMax"
    const val DECK_PACE = "pace"
    const val DECK_ORDER = "order"

    val defaults: Map<String, String> = mapOf(
        THEME to "auto", FONT to "m", LAYOUT to "list", NEW_PER_DAY to "10", SESSION_MAX to "50",
        SPEECH_RATE to "1", VIBRATE to "1", KEEP_AWAKE to "1"
    )

    fun deckKey(deckId: Long, param: String) = "deck:$deckId:$param"
}

/** Собственные параметры повторения колоды; null — взять общие. */
data class DeckLimits(
    val perDay: Int? = null,
    val max: Int? = null,
    val inOrder: Boolean = false,
    val pace: Double = 1.0
)

/** Лимиты сессии: общие + по колодам; `used` — сколько новых колода уже взяла сегодня. */
data class Limits(
    val perDay: Int = 10,
    val max: Int = 50,
    val used: Map<Long, Int> = emptyMap(),
    val decks: Map<Long, DeckLimits> = emptyMap()
) {
    fun paceOf(deckId: Long): Double = decks[deckId]?.pace ?: 1.0
}

object DeckPrefs {
    private fun valid(options: List<Option>, v: String?): String? = v?.takeIf { s -> options.any { it.value == s } }

    /** Собирает лимиты из `settings`; значения не из списка игнорируются (мусор не ломает сессию). */
    fun limitsFrom(settings: Map<String, String>, deckIds: Collection<Long>, used: Map<Long, Int>): Limits {
        val perDay = valid(PrefOptions.newPerDay, settings[PrefKeys.NEW_PER_DAY])?.toInt()
            ?: PrefKeys.defaults.getValue(PrefKeys.NEW_PER_DAY).toInt()
        val max = valid(PrefOptions.sessionMax, settings[PrefKeys.SESSION_MAX])?.toInt()
            ?: PrefKeys.defaults.getValue(PrefKeys.SESSION_MAX).toInt()
        val decks = deckIds.associateWith { id ->
            DeckLimits(
                perDay = valid(PrefOptions.newPerDay, settings[PrefKeys.deckKey(id, PrefKeys.DECK_NEW_PER_DAY)])?.toInt(),
                max = valid(PrefOptions.sessionMax, settings[PrefKeys.deckKey(id, PrefKeys.DECK_SESSION_MAX)])?.toInt(),
                inOrder = settings[PrefKeys.deckKey(id, PrefKeys.DECK_ORDER)] == "seq",
                pace = valid(PrefOptions.pace, settings[PrefKeys.deckKey(id, PrefKeys.DECK_PACE)])?.toDouble() ?: 1.0
            )
        }
        return Limits(perDay, max, used, decks)
    }
}
