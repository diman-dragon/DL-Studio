package com.dlstudio.app.library

import com.dlstudio.app.storage.StorageNode
import com.dlstudio.app.storage.ensureLayout

/** Файл library.json в `DL-Studio/library`; `create` — создать, если его ещё нет. */
fun libraryFile(parent: StorageNode, create: Boolean): StorageNode? {
    val dir = ensureLayout(parent).child("library") ?: return null
    return dir.child("library.json") ?: if (create) dir.createFile("library.json") else null
}
