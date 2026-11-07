package com.dlstudio.app.ui.components

/** Первая буква заглавная (остальной текст как есть): слова на карточках не начинаются с маленькой буквы. */
fun String.capFirst(): String = replaceFirstChar { it.titlecase() }
