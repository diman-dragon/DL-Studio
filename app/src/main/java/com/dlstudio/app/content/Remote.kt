package com.dlstudio.app.content

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/** Каталог и загрузка тем по адресу папки с `course.json`. Только https; каждая тема проверяется по SHA-256 из каталога. Вызывать не на главном потоке. */
object Remote {
    private const val TIMEOUT_MS = 15_000
    private const val MAX_BYTES = 5_000_000

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

    fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}
