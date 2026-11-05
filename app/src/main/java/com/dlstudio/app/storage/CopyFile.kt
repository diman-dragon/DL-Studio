package com.dlstudio.app.storage

/** Копирует содержимое файла в одноимённый файл папки `to` (существующий заменяется). */
fun copyFile(file: StorageNode, to: StorageNode) {
    to.child(file.name)?.delete()
    val target = to.createFile(file.name)
    file.openInput().use { input -> target.openOutput().use { output -> input.copyTo(output) } }
}
