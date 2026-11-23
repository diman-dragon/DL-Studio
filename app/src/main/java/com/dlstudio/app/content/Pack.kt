package com.dlstudio.app.content

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream

/**
 * Чтение файла с темами: обычный json — одна тема, zip — пакет с любым числом `*.json` (по теме в каждом) и папкой `media/<id темы>/` с аудио, видео и картинками.
 * Размеры ограничены, чтобы испорченный или «раздутый» архив не съел память; нечитаемые и слишком большие записи пропускаются.
 */
object TopicPack {
    private const val MAX_FILE = 60_000_000
    private const val MAX_ENTRY = 1_000_000
    private const val MAX_MEDIA = 25_000_000
    private const val MAX_TOTAL = 60_000_000L
    private const val MAX_ENTRIES = 2_000

    fun texts(bytes: ByteArray): List<String> {
        if (bytes.size > MAX_FILE) return emptyList()
        val zip = bytes.size > 3 && bytes[0] == 'P'.code.toByte() && bytes[1] == 'K'.code.toByte()
        return if (zip) runCatching { unzip(bytes) }.getOrDefault(emptyList()) else listOf(bytes.toString(Charsets.UTF_8))
    }

    /**
     * Медиа из zip-пакета: записи `media/<id темы>/<путь>` с разрешённым расширением и безопасным путём. Каждая отдаётся в `onFile(id, путь, байты)`.
     * Запись крупнее 25 МБ пропускается (большое медиа лучше давать ссылкой https).
     */
    fun media(bytes: ByteArray, onFile: (String, String, ByteArray) -> Unit) {
        if (bytes.size > MAX_FILE || bytes.size < 4 || bytes[0] != 'P'.code.toByte() || bytes[1] != 'K'.code.toByte()) return
        var total = 0L
        runCatching {
            ZipInputStream(ByteArrayInputStream(bytes)).use { stream ->
                var entries = 0
                while (entries < MAX_ENTRIES && total < MAX_TOTAL) {
                    val entry = stream.nextEntry ?: break
                    entries++
                    val name = entry.name
                    if (entry.isDirectory || !name.startsWith("media/")) continue
                    val id = name.removePrefix("media/").substringBefore('/')
                    val rel = name.removePrefix("media/").substringAfter('/', "")
                    if (!Regex("[a-z0-9_-]{1,64}").matches(id) || !Media.safePath(rel)) continue
                    val data = readLimited(stream, MAX_MEDIA) ?: continue
                    total += data.size
                    onFile(id, rel, data)
                }
            }
        }
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

    private fun readLimited(stream: ZipInputStream, limit: Int = MAX_ENTRY): ByteArray? {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val n = stream.read(buffer)
            if (n < 0) return out.toByteArray()
            out.write(buffer, 0, n)
            if (out.size() > limit) return null
        }
    }
}
