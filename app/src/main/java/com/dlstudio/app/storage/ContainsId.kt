package com.dlstudio.app.storage

/** true, если внутри `root` (на любой глубине) есть узел с таким `id`. */
fun containsId(root: StorageNode, id: String): Boolean =
    root.children().any { it.id == id || (it.isDirectory && containsId(it, id)) }
