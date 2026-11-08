package com.dlstudio.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.dlstudio.app.study.PronounceCheck

/** Текст карточки, в котором правильно произнесённые слова (по тому, что услышал распознаватель) выделены цветом. */
object SpokenHighlight {
    private val word = Regex("\\S+")

    fun build(text: String, heard: String, ok: Color): AnnotatedString {
        if (heard.isBlank()) return AnnotatedString(text)
        val matched = PronounceCheck.matchedWords(text, heard)
        return buildAnnotatedString {
            var last = 0
            word.findAll(text).forEachIndexed { i, m ->
                append(text.substring(last, m.range.first))
                if (matched.getOrNull(i) == true) withStyle(SpanStyle(color = ok, fontWeight = FontWeight.Bold)) { append(m.value) } else append(m.value)
                last = m.range.last + 1
            }
            append(text.substring(last))
        }
    }
}
