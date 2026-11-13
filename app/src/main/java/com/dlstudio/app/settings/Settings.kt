package com.dlstudio.app.settings



/** Акцентный цвет приложения: светлый оттенок, основной и глубокий цвет градиента. */
enum class AccentColor(val soft: Long, val main: Long, val deep: Long) {
    VIOLET(0xFFB69CFF, 0xFF8A66FF, 0xFF5B3FD6),
    BLUE(0xFF8DB4FF, 0xFF4C8DFF, 0xFF2457D6),
    GREEN(0xFF7BE495, 0xFF3DC768, 0xFF1B8A45),
    AMBER(0xFFFFC857, 0xFFFFA11A, 0xFFB35400),
    RED(0xFFFF9A9A, 0xFFFF5252, 0xFFC62828),
    LAVENDER(0xFFE0C7FF, 0xFFB58BFF, 0xFF7C4DCC),
    SKY(0xFFB9EAFF, 0xFF55C7F2, 0xFF1687B5),
    MINT(0xFFB9F2D0, 0xFF63D69A, 0xFF218A5A),
    PEACH(0xFFFFD1A8, 0xFFFFA15C, 0xFFD45E1D),
    CRIMSON(0xFFFFB0BC, 0xFFE34B63, 0xFFA51F3A),
    PLUM(0xFFE0B7FF, 0xFF9D5CDB, 0xFF642698),
    TURQUOISE(0xFF8CEBE0, 0xFF20C9B5, 0xFF087B71),
    LIME(0xFFD7F58F, 0xFF9BD331, 0xFF56800D),
    ORANGE(0xFFFFC080, 0xFFFF853D, 0xFFC94D13),
    MAGENTA(0xFFFFB5F1, 0xFFE85BD4, 0xFFA62396)
}

/** Три набора по пять цветов. При смене набора сохраняется позиция выбранного цвета. */
enum class AccentPalette(val colors: List<AccentColor>) {
    CLASSIC(listOf(AccentColor.VIOLET, AccentColor.BLUE, AccentColor.GREEN, AccentColor.AMBER, AccentColor.RED)),
    SOFT(listOf(AccentColor.LAVENDER, AccentColor.SKY, AccentColor.MINT, AccentColor.PEACH, AccentColor.CRIMSON)),
    BRIGHT(listOf(AccentColor.PLUM, AccentColor.TURQUOISE, AccentColor.LIME, AccentColor.ORANGE, AccentColor.MAGENTA));

    fun slot(accent: AccentColor): Int = colors.indexOf(accent).coerceAtLeast(0)
    companion object { fun forAccent(accent: AccentColor): AccentPalette = values().firstOrNull { accent in it.colors } ?: CLASSIC }
}

/** Язык интерфейса; `label` — название языка на нём самом. */
enum class AppLang(val label: String) { RU("Русский"), EN("English") }

/** Состояние операции со папкой хранения; показывается в настройках. */
enum class StorageStatus { NONE, MOVING, MOVED, ERROR_INSIDE, ERROR_IO }

/** Тема приложения: тёмная (чёрный AMOLED) или светлая. */
enum class ThemeMode { DARK, LIGHT }

/** Группы текста, для которых шрифт и размер настраиваются отдельно. */
enum class FontGroup { UI, CARD, IPA, NOTE, BUTTON }

/** Доступные семейства: системные шрифты Android, поэтому работают без интернета и не раздувают приложение. */
enum class FontChoice { SANS, SERIF, MONO, CURSIVE }

/** Шрифт и размер (в процентах от базового) одной группы текста. */
data class FontSpec(val choice: FontChoice = FontChoice.SANS, val scale: Int = DEFAULT) {
    companion object {
        const val DEFAULT = 100
        val SCALES = listOf(85, 100, 115, 130)
    }
}

/**
 * Режим шрифтов: `SYSTEM` — размеры фиксированные (слово карточки крупно, всё остальное системным, кнопки мелким),
 * `ADAPTIVE` — размер подбирается под область, `CUSTOM` — адаптивный размер плюс шрифт и масштаб каждой группы.
 */
enum class FontPreset { SYSTEM, ADAPTIVE, CUSTOM }

/** Режим и шрифты групп. Настройки групп действуют только в режиме `CUSTOM`, но сохраняются и при других режимах. */
data class FontSettings(val preset: FontPreset = FontPreset.SYSTEM, private val specs: Map<FontGroup, FontSpec> = emptyMap()) {
    /** Действующий шрифт группы. */
    operator fun get(group: FontGroup): FontSpec = if (preset == FontPreset.CUSTOM) specs[group] ?: FontSpec() else FontSpec()

    fun changed(group: FontGroup, spec: FontSpec): FontSettings =
        copy(specs = if (spec == FontSpec()) specs - group else specs + (group to spec))

    fun withPreset(next: FontPreset): FontSettings = copy(preset = next)
}
