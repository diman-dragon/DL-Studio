package com.dlstudio.app.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

/** Разметка текста теории: **жирный** и _курсив_. Незакрытая отметка просто игнорируется. */
object Markup {
    fun annotate(text: String): AnnotatedString = buildAnnotatedString {
        var boldStart = -1
        var italicStart = -1
        var i = 0
        while (i < text.length) {
            when {
                text.startsWith("**", i) -> {
                    if (boldStart < 0) boldStart = length else { addStyle(SpanStyle(fontWeight = FontWeight.Bold), boldStart, length); boldStart = -1 }
                    i += 2
                }
                text[i] == '_' -> {
                    if (italicStart < 0) italicStart = length else { addStyle(SpanStyle(fontStyle = FontStyle.Italic), italicStart, length); italicStart = -1 }
                    i += 1
                }
                else -> { append(text[i]); i += 1 }
            }
        }
    }
}
