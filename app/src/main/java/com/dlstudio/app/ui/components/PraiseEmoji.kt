package com.dlstudio.app.ui.components

import com.dlstudio.app.study.PraiseKind

/** Значок к похвале на экране статистики. */
fun praiseEmoji(kind: PraiseKind): String = when (kind) {
    PraiseKind.RECORD_DAY -> "🏆"
    PraiseKind.UP_TODAY -> "📈"
    PraiseKind.BEST_STREAK -> "🔥"
    PraiseKind.WEEK_UP -> "🚀"
    PraiseKind.ACCURACY -> "🎯"
    PraiseKind.KEEP -> "💪"
}
