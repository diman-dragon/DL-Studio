package com.dlstudio.app.ui.components

import com.dlstudio.app.R

/** Кадры портретов персонажей (создаётся скриптом scripts/portraits.mjs; не править вручную). Ресурсы названы явно, чтобы сжатие ресурсов их не вырезало. */
object PortraitRes {
    private val frames: Map<String, Map<String, Int>> = mapOf(
        "grace" to mapOf("idle" to R.drawable.ch_grace_idle, "blink" to R.drawable.ch_grace_blink, "talk1" to R.drawable.ch_grace_talk1, "talk2" to R.drawable.ch_grace_talk2, "happy" to R.drawable.ch_grace_happy, "sad" to R.drawable.ch_grace_sad),
        "leo" to mapOf("idle" to R.drawable.ch_leo_idle, "blink" to R.drawable.ch_leo_blink, "talk1" to R.drawable.ch_leo_talk1, "talk2" to R.drawable.ch_leo_talk2, "happy" to R.drawable.ch_leo_happy, "sad" to R.drawable.ch_leo_sad),
        "beatrice" to mapOf("idle" to R.drawable.ch_beatrice_idle, "blink" to R.drawable.ch_beatrice_blink, "talk1" to R.drawable.ch_beatrice_talk1, "talk2" to R.drawable.ch_beatrice_talk2, "happy" to R.drawable.ch_beatrice_happy, "sad" to R.drawable.ch_beatrice_sad),
        "john" to mapOf("idle" to R.drawable.ch_john_idle, "blink" to R.drawable.ch_john_blink, "talk1" to R.drawable.ch_john_talk1, "talk2" to R.drawable.ch_john_talk2, "happy" to R.drawable.ch_john_happy, "sad" to R.drawable.ch_john_sad),
        "narrator" to mapOf("idle" to R.drawable.ch_narrator_idle, "blink" to R.drawable.ch_narrator_blink, "talk1" to R.drawable.ch_narrator_talk1, "talk2" to R.drawable.ch_narrator_talk2, "happy" to R.drawable.ch_narrator_happy, "sad" to R.drawable.ch_narrator_sad)
    )

    /** Ресурс кадра персонажа; нет такого кадра — спокойный. */
    fun of(id: String, frame: String): Int? = frames[id]?.let { it[frame] ?: it["idle"] }
}
