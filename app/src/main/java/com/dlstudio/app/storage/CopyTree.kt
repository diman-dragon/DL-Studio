package com.dlstudio.app.storage

/** Рекурсивно копирует содержимое папки `from` в папку `to`. */
fun copyTree(from: StorageNode, to: StorageNode) {
    from.children().forEach { node ->
        if (node.isDirectory) copyTree(node, dirOrCreate(to, node.name)) else copyFile(node, to)
    }
}
