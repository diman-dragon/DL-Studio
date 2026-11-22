package com.dlstudio.app.content

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

/**
 * Чтение файла с темами: обычный json — одна тема, zip — пакет с любым числом `*.json` (по теме в каждом).
 * Размеры ограничены, чтобы испорченный или «раздутый» архив не съел память; нечитаемые и слишком большие записи пропускаются.
 */
object TopicPack {
    private const val MAX_FILE = 30_000_000
    private const val MAX_ENTRY = 1_000_000
    private const val MAX_TOTAL = 60_000_000L
    private const val MAX_ENTRIES = 2_000

    fun texts(bytes: ByteArray): List<String> {
        if (bytes.size > MAX_FILE) return emptyList()
        val zip = bytes.size > 3 && bytes[0] == 'P'.code.toByte() && bytes[1] == 'K'.code.toByte()
        return if (zip) runCatching { unzip(bytes) }.getOrDefault(emptyList()) else listOf(bytes.toString(Charsets.UTF_8))
    }

    private fun unzip(bytes: ByteArray): List<String> {
        val result = ArrayList<String>()
        var total = 0L
        ZipInputStream(ByteArrayInputStream(bytes)).use { stream ->
            var entries = 0
            while (entries < MAX_ENTRIES && total < MAX_TOTAL) {
                val entry = stream.nextEntry ?: break
                entries++
                if (entry.isDirectory || !entry.name.endsWith(".json")) continue
                val data = readLimited(stream) ?: continue
                total += data.size
                result.add(data.toString(Charsets.UTF_8))
            }
        }
        return result
    }

    private fun readLimited(stream: ZipInputStream): ByteArray? {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val n = stream.read(buffer)
            if (n < 0) return out.toByteArray()
            out.write(buffer, 0, n)
            if (out.size() > MAX_ENTRY) return null
        }
    }
}
