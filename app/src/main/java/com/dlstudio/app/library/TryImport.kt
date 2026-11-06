package com.dlstudio.app.library

import android.content.Context
import android.net.Uri

/** Импорт без исключений: узлы для вставки на уровень `level` и отчёт; ошибка чтения — пусто и отчёт «0 карточек». */
fun tryImport(context: Context, uri: Uri, level: Int): Pair<List<LibNode>, ImportReport> {
    val nodes = runCatching { readImport(context, uri, level) }.getOrDefault(emptyList())
    return nodes to ImportReport(nodes.descend(2 - level).size)
}
