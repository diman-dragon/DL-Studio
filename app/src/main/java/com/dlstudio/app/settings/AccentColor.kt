package com.dlstudio.app.settings

/**
 * Акцентный цвет приложения (ARGB): `soft` — светлый оттенок для тёмной темы, `deep` — насыщенный для светлой,
 * `main` и `deep` вместе — градиент плиток и кнопки «+».
 */
enum class AccentColor(val soft: Long, val main: Long, val deep: Long) {
    VIOLET(0xFFB69CFF, 0xFF8A66FF, 0xFF5B3FD6),
    BLUE(0xFF8DB4FF, 0xFF4C8DFF, 0xFF2457D6),
    TEAL(0xFF4FE0D0, 0xFF14C2B0, 0xFF0A7F86),
    GREEN(0xFF7BE495, 0xFF3DC768, 0xFF1B8A45),
    AMBER(0xFFFFC857, 0xFFFFA11A, 0xFFB35400),
    PINK(0xFFFF9BD0, 0xFFFF5CB0, 0xFFC72A86),
    RED(0xFFFF9A9A, 0xFFFF5252, 0xFFC62828)
}
