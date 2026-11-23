package com.dlstudio.app.content

import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/** Каталог и загрузка тем по адресу папки с `course.json`. Только https; каждая тема проверяется по SHA-256 из каталога. Вызывать не на главном потоке. */
object Remote {
    private const val TIMEOUT_MS = 15_000
    private const val MAX_BYTES = 5_000_000
    private const val MAX_MEDIA_BYTES = 200_000_000L

    private fun folder(url: String) = url.trim().trimEnd('/')

    private fun get(url: String): ByteArray {
        if (!url.startsWith("https://")) throw IOException("https only")
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            if (connection.responseCode !in 200..299) throw IOException("HTTP ${connection.responseCode}")
            val bytes = connection.inputStream.use { it.readBytes() }
            if (bytes.size > MAX_BYTES) throw IOException("too large")
            return bytes
        } finally {
            connection.disconnect()
        }
    }

    fun catalog(url: String): List<CatalogEntry> = TopicJson.parseCatalog(get("${folder(url)}/course.json").toString(Charsets.UTF_8))

    fun download(url: String, entry: CatalogEntry): ByteArray {
        val bytes = get("${folder(url)}/${entry.path}")
        if (sha256(bytes) != entry.sha256.lowercase()) throw IOException("sha256")
        return bytes
    }

    /**
     * Скачивает медиа темы в её папку: файл пишется потоком (видео не лежит в памяти целиком), SHA-256 сверяется с каталогом,
     * уже скачанный и совпавший файл пропускается. Любая ошибка — исключение; недокачанные файлы не остаются.
     */
    fun downloadMedia(url: String, entry: CatalogEntry, dir: File) {
        for (file in entry.media) {
            val target = File(dir, file.path)
            if (target.isFile && file.sha256.isNotBlank() && sha256(target) == file.sha256.lowercase()) continue
            target.parentFile?.mkdirs()
            val part = File(dir, "${file.path}.part")
            part.parentFile?.mkdirs()
            try {
                fetchTo("${folder(url)}/media/${entry.id}/${file.path}", part)
                if (file.sha256.isNotBlank() && sha256(part) != file.sha256.lowercase()) throw IOException("sha256")
                if (!part.renameTo(target)) throw IOException("rename")
            } finally {
                part.delete()
            }
        }
    }

    private fun fetchTo(url: String, out: File) {
        if (!url.startsWith("https://")) throw IOException("https only")
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            if (connection.responseCode !in 200..299) throw IOException("HTTP ${connection.responseCode}")
            var total = 0L
            connection.inputStream.use { input ->
                out.outputStream().use { output ->
                    val buffer = ByteArray(16_384)
                    while (true) {
                        val n = input.read(buffer)
                        if (n < 0) break
                        total += n
                        if (total > MAX_MEDIA_BYTES) throw IOException("too large")
                        output.write(buffer, 0, n)
                    }
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(16_384)
            while (true) { val n = input.read(buffer); if (n < 0) break; digest.update(buffer, 0, n) }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}
