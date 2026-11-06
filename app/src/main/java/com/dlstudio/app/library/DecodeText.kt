package com.dlstudio.app.library

import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction

/** Текст файла: UTF-16 (с BOM) или UTF-8 (BOM убирается); не UTF-8 — значит Excel в Windows, то есть windows-1251. */
fun decodeText(bytes: ByteArray): String {
    val head = bytes.take(2).map { it.toInt() and 0xFF }
    if (head == listOf(0xFF, 0xFE) || head == listOf(0xFE, 0xFF)) return String(bytes, Charsets.UTF_16)
    val strict = Charsets.UTF_8.newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT)
    val text = runCatching { strict.decode(ByteBuffer.wrap(bytes)).toString() }
        .getOrElse { String(bytes, charset("windows-1251")) }
    return text.removePrefix("\uFEFF")
}
