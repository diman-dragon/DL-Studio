package com.dlstudio.app.ui.components

import com.dlstudio.app.study.MotivationKind

/** Значок к мотивационной фразе дашборда. */
fun motivationEmoji(kind: MotivationKind): String = when (kind) {
    MotivationKind.EMPTY -> "📚"
    MotivationKind.START -> "🚀"
    MotivationKind.DONE_TODAY -> "🎉"
    MotivationKind.STREAK -> "🔥"
    MotivationKind.CATCH_UP -> "⏰"
    MotivationKind.MATURE -> "🌟"
    MotivationKind.KEEP -> "💪"
}
