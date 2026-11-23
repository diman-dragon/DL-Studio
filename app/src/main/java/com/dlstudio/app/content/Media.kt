package com.dlstudio.app.content

import java.io.File

/** Вид медиа в теории: картинка, звук, видео. Разметка абзаца — `@image путь | подпись`, `@audio …`, `@video …`. */
enum class MediaKind(val mark: String) { IMAGE("@image "), AUDIO("@audio "), VIDEO("@video ") }

/**
 * Медиа урока. Ссылка в теории — либо относительный путь (файл лежит в папке медиа темы: его кладёт zip-пакет),
 * либо адрес https (звук и видео проигрываются потоком, ничего не скачивая). Расширения ограничены списком, пути — безопасными символами.
 */
object Media {
    private val safeRel = Regex("[A-Za-z0-9_./-]{1,128}")
    val extensions = setOf("mp3", "m4a", "aac", "ogg", "oga", "wav", "mp4", "webm", "3gp", "jpg", "jpeg", "png", "webp", "gif")

    fun isUrl(src: String) = src.startsWith("https://")

    fun safePath(path: String) = safeRel.matches(path) && ".." !in path && !path.startsWith("/") && path.substringAfterLast('.', "").lowercase() in extensions

    /** Файл темы по относительному пути, если он безопасен и уже лежит на устройстве. */
    fun local(dir: File?, src: String): File? =
        if (dir == null || !safePath(src)) null else File(dir, src).takeIf { it.isFile }
}
