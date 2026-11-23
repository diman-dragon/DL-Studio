package com.dlstudio.app.content

import android.content.Context
import java.io.File

/**
 * Установленные темы: по файлу `<id>.json` в папке приложения. Встроенных тем нет: их ставят из файла
 * (json одной темы или zip-пакет с темами) или скачивают по каталогу.
 */
class TopicStore(private val context: Context) {
    private val dir: File get() = File(context.filesDir, "topics").also { it.mkdirs() }

    private val mediaRoot: File get() = File(context.filesDir, "media")

    /** Папка медиа темы (аудио, видео, картинки); создаётся при первой записи. */
    fun mediaDir(id: String): File = File(mediaRoot, id)

    fun installed(): List<Topic> = (dir.listFiles { f -> f.extension == "json" } ?: emptyArray())
        .mapNotNull { f -> runCatching { TopicJson.parse(f.readText()) }.getOrNull() }
        .sortedWith(compareBy({ it.course }, { it.n }, { it.title }))

    /** Ставит темы из байт файла (одна тема или zip-пакет); пустой список — формат не подошёл. Одноимённая тема заменяется (прогресс сохраняется по `id`). */
    fun install(bytes: ByteArray): List<Topic> {
        val topics = TopicPack.texts(bytes).mapNotNull { text ->
            runCatching { TopicJson.parse(text) }.getOrNull()?.also { File(dir, "${it.id}.json").writeText(text) }
        }
        if (topics.isNotEmpty()) {
            val ids = topics.map { it.id }.toSet()
            TopicPack.media(bytes) { id, rel, data -> if (id in ids) runCatching { File(mediaDir(id), rel).also { it.parentFile?.mkdirs() }.writeBytes(data) } }
        }
        return topics
    }

    fun remove(id: String) { File(dir, "$id.json").delete(); mediaDir(id).deleteRecursively() }

    fun removeAll() { dir.listFiles { f -> f.extension == "json" }?.forEach { it.delete() }; mediaRoot.deleteRecursively() }
}
