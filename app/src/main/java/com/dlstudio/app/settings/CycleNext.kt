package com.dlstudio.app.settings

/** Следующее значение перечисления по кругу: после последнего снова первое. */
inline fun <reified T : Enum<T>> T.cycleNext(): T {
    val all = enumValues<T>()
    return all[(ordinal + 1) % all.size]
}
