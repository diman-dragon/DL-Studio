package com.dlstudio.app.library

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import java.io.IOException

/** Читает файл и подгоняет под открытый уровень: 0 — проекты, 1 — колоды, 2 — карточки (блокирующая операция). */
fun readImport(context: Context, uri: Uri, level: Int): List<LibNode> {
    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: throw IOException("Файл не открыт")
    if (bytes.size > 10_000_000) throw IOException("Файл больше 10 МБ")
    val name = DocumentFile.fromSingleUri(context, uri)?.name.orEmpty().substringBeforeLast('.').ifBlank { "Import" }
    return ImportParser.parse(decodeText(bytes), name).descend(level)
}
