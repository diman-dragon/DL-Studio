package com.dlstudio.app.settings

/** Три набора по пять цветов. При смене набора сохраняется позиция выбранного цвета. */
enum class AccentPalette(val colors: List<AccentColor>) {
    CLASSIC(listOf(AccentColor.VIOLET, AccentColor.BLUE, AccentColor.GREEN, AccentColor.AMBER, AccentColor.RED)),
    SOFT(listOf(AccentColor.LAVENDER, AccentColor.SKY, AccentColor.MINT, AccentColor.PEACH, AccentColor.CRIMSON)),
    BRIGHT(listOf(AccentColor.PLUM, AccentColor.TURQUOISE, AccentColor.LIME, AccentColor.ORANGE, AccentColor.MAGENTA));

    fun slot(accent: AccentColor): Int = colors.indexOf(accent).coerceAtLeast(0)
    companion object { fun forAccent(accent: AccentColor): AccentPalette = values().firstOrNull { accent in it.colors } ?: CLASSIC }
}
