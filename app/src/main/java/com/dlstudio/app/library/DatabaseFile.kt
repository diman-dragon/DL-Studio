package com.dlstudio.app.library

import com.dlstudio.app.storage.StorageNode
import com.dlstudio.app.storage.ensureLayout

fun databaseFile(parent: StorageNode, create: Boolean): StorageNode? {
    val dir = ensureLayout(parent).child("library") ?: return null
    return dir.child("library.db") ?: if (create) dir.createFile("library.db") else null
}
