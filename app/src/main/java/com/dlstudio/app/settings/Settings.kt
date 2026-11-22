package com.dlstudio.app.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb

/**
 * Акцентный цвет приложения — любой оттенок радуги (`hue` 0..360). Из него считаются три цвета:
 * светлый (на чёрном фоне), основной и глубокий (концы градиента, акцент на светлой теме).
 * Основной и глубокий затемняются, пока белый текст на них не станет читаться (жёлтый и салатовый иначе были бы бледными).
 */
data class AccentColor(val hue: Float) {
    val soft: Long get() = argb(0.38f, 1f, 1f)
    val main: Long get() = argb(0.72f, 0.95f, MAIN_LUMINANCE)
    val deep: Long get() = argb(0.9f, 0.66f, DEEP_LUMINANCE)

    private fun argb(saturation: Float, value: Float, maxLuminance: Float): Long {
        var v = value
        var color = Color.hsv(hue.coerceIn(0f, 360f), saturation, v)
        while (color.luminance() > maxLuminance && v > 0.2f) { v -= 0.02f; color = Color.hsv(hue.coerceIn(0f, 360f), saturation, v) }
        return color.toArgb().toLong() and 0xFFFFFFFFL
    }

    companion object {
        val DEFAULT = AccentColor(258f)
        /** Предельная яркость (0..1): контраст белого текста с основным цветом ≥ 3:1, с глубоким ≥ 4.5:1. */
        const val MAIN_LUMINANCE = 0.30f
        const val DEEP_LUMINANCE = 0.18f
    }
}

/** Язык интерфейса; `label` — название языка на нём самом. */
enum class AppLang(val label: String) { RU("Русский"), EN("English") }

/** Тема приложения: тёмная (чёрный AMOLED) или светлая. */
enum class ThemeMode { DARK, LIGHT }
