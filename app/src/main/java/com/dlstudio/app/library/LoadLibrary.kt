package com.dlstudio.app.library

import com.dlstudio.app.storage.StorageNode

/** Читает библиотеку (блокирующая операция); файла нет — пустая. */
fun loadLibrary(parent: StorageNode): List<LibNode> {
    val file = libraryFile(parent, create = false) ?: return emptyList()
    return LibraryJson.decode(file.openInput().use { it.readBytes().toString(Charsets.UTF_8) })
}
