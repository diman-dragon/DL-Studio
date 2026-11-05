package com.dlstudio.app.storage

/** true, если каждый файл и папка из `source` есть в `target` (файлы — того же размера). */
fun coversTree(target: StorageNode, source: StorageNode): Boolean =
    source.children().all { node ->
        val twin = target.child(node.name) ?: return@all false
        if (node.isDirectory) twin.isDirectory && coversTree(twin, node) else !twin.isDirectory && twin.size == node.size
    }
