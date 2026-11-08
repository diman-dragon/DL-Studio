package com.dlstudio.app.ui.components

import com.dlstudio.app.R
import com.dlstudio.app.study.SpeechSpeed

/** Значок скорости озвучки: динамик — обычно, черепаха — медленно, улитка — ещё медленнее. */
fun SpeechSpeed.speedIcon(): Int = when (this) {
    SpeechSpeed.NORMAL -> R.drawable.ic_volume
    SpeechSpeed.SLOW -> R.drawable.ic_turtle
    SpeechSpeed.SLOWER -> R.drawable.ic_snail
}
