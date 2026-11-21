package com.dlstudio.app.content

import android.content.Context
import com.dlstudio.app.data.Prefs
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipInputStream

/**
 * Установленные темы: по файлу `<id>.json` в папке приложения. Тему можно положить из zip (внутри `*.json`) или из самого json.
 * При первом запуске копируются встроенные темы из assets (папка `content/topics` проекта).
 */
class TopicStore(private val context: Context, private val prefs: Prefs) {
    private val dir: File get() = File(context.filesDir, "topics").also { it.mkdirs() }

    fun installed(): List<Topic> = (dir.listFiles { f -> f.extension == "json" } ?: emptyArray())
        .mapNotNull { f -> runCatching { TopicJson.parse(f.readText()) }.getOrNull() }
        .sortedWith(compareBy({ it.course }, { it.n }, { it.title }))

    /** Ставит тему из байт файла; null — формат не подошёл. Одноимённая тема заменяется (прогресс сохраняется по `id`). */
    fun install(bytes: ByteArray): Topic? {
        val text = unpack(bytes) ?: return null
        val topic = runCatching { TopicJson.parse(text) }.getOrNull() ?: return null
        File(dir, "${topic.id}.json").writeText(text)
        return topic
    }

    fun remove(id: String) { File(dir, "$id.json").delete() }

    fun seed() {
        if (prefs.seeded) return
        context.assets.list("topics").orEmpty().filter { it.endsWith(".json") }
            .forEach { name -> context.assets.open("topics/$name").use { install(it.readBytes()) } }
        prefs.seeded = true
    }

    private fun unpack(bytes: ByteArray): String? {
        if (bytes.size > MAX_BYTES) return null
        val zip = bytes.size > 3 && bytes[0] == 'P'.code.toByte() && bytes[1] == 'K'.code.toByte()
        if (!zip) return bytes.toString(Charsets.UTF_8)
        return ZipInputStream(ByteArrayInputStream(bytes)).use { stream ->
            generateSequence { stream.nextEntry }.firstOrNull { !it.isDirectory && it.name.endsWith(".json") }
                ?.let { stream.readBytes().toString(Charsets.UTF_8) }
        }
    }

    private companion object { const val MAX_BYTES = 5_000_000 }
}
