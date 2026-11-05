package com.dlstudio.app.storage

import java.io.IOException

/** Подпапка `name`: существующая или новая. Если под этим именем лежит файл — ошибка. */
fun dirOrCreate(parent: StorageNode, name: String): StorageNode {
    val existing = parent.child(name) ?: return parent.createDirectory(name)
    if (!existing.isDirectory) throw IOException("$name — не папка")
    return existing
}
