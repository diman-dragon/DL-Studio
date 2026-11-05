package com.dlstudio.app.storage

/** Создаёт `DL-Studio` и все подпапки (повторный вызов безопасен). Возвращает папку `DL-Studio`. */
fun ensureLayout(parent: StorageNode): StorageNode {
    val root = dirOrCreate(parent, StorageLayout.ROOT)
    StorageLayout.FOLDERS.forEach { dirOrCreate(root, it) }
    return root
}
