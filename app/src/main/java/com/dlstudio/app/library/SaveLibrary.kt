package com.dlstudio.app.library

import com.dlstudio.app.storage.StorageNode
import java.io.IOException

/** Записывает библиотеку целиком (блокирующая операция). */
fun saveLibrary(parent: StorageNode, nodes: List<LibNode>) {
    val file = libraryFile(parent, create = true) ?: throw IOException("library.json")
    file.openOutput().use { it.write(LibraryJson.encode(nodes).toByteArray(Charsets.UTF_8)) }
}
