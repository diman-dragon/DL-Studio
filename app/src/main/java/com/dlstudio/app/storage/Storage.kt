package com.dlstudio.app.storage

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/** Узел хранилища (папка или файл). Реализации: java.io.File и дерево SAF; логика переноса от них не зависит. */
interface StorageNode {
    /** Стабильный идентификатор места: одно и то же место → один и тот же id. */
    val id: String
    val name: String
    val isDirectory: Boolean
    val size: Long
    fun children(): List<StorageNode>
    fun child(name: String): StorageNode?
    fun createDirectory(name: String): StorageNode
    fun createFile(name: String): StorageNode
    fun openInput(): InputStream
    fun openOutput(): OutputStream
    fun delete(): Boolean
}

/** Узел на java.io.File: папка приложения по умолчанию. */
class FileNode(private val file: File) : StorageNode {
    override val id: String get() = file.canonicalPath
    override val name: String get() = file.name
    override val isDirectory: Boolean get() = file.isDirectory
    override val size: Long get() = file.length()
    override fun children(): List<StorageNode> = file.listFiles().orEmpty().sortedBy { it.name }.map(::FileNode)
    override fun child(name: String): StorageNode? = File(file, name).takeIf { it.exists() }?.let(::FileNode)
    override fun createDirectory(name: String): StorageNode = FileNode(File(file, name).also { check(it.mkdirs()) })
    override fun createFile(name: String): StorageNode = FileNode(File(file, name).also { check(it.createNewFile()) })
    override fun openInput(): InputStream = file.inputStream()
    override fun openOutput(): OutputStream = file.outputStream()
    override fun delete(): Boolean = file.deleteRecursively()
}

/** Выбранная папка находится внутри текущей папки DL-Studio: переносить её саму в себя нельзя. */
class InsideStorageException : Exception("Папка находится внутри DL-Studio")

/** Структура папки приложения внутри выбранной пользователем папки. */
object StorageLayout {
    const val ROOT = "DL-Studio"
    val FOLDERS = listOf("library", "media", "audio", "backups", "exports")
}

/** Создаёт `DL-Studio` и все подпапки (повторный вызов безопасен). Возвращает папку `DL-Studio`. */
fun ensureLayout(parent: StorageNode): StorageNode {
    val root = dirOrCreate(parent, StorageLayout.ROOT)
    StorageLayout.FOLDERS.forEach { dirOrCreate(root, it) }
    return root
}

/** true, если каждый файл и папка из `source` есть в `target` (файлы — того же размера). */
fun coversTree(target: StorageNode, source: StorageNode): Boolean =
    source.children().all { node ->
        val twin = target.child(node.name) ?: return@all false
        if (node.isDirectory) twin.isDirectory && coversTree(twin, node) else !twin.isDirectory && twin.size == node.size
    }

/** true, если внутри `root` (на любой глубине) есть узел с таким `id`. */
fun containsId(root: StorageNode, id: String): Boolean =
    root.children().any { it.id == id || (it.isDirectory && containsId(it, id)) }

/** Подпапка `name`: существующая или новая. Если под этим именем лежит файл — ошибка. */
fun dirOrCreate(parent: StorageNode, name: String): StorageNode {
    val existing = parent.child(name) ?: return parent.createDirectory(name)
    if (!existing.isDirectory) throw IOException("$name — не папка")
    return existing
}

/** Копирует содержимое файла в одноимённый файл папки `to` (существующий заменяется). */
fun copyFile(file: StorageNode, to: StorageNode) {
    to.child(file.name)?.delete()
    val target = to.createFile(file.name)
    file.openInput().use { input -> target.openOutput().use { output -> input.copyTo(output) } }
}

/** Рекурсивно копирует содержимое папки `from` в папку `to`. */
fun copyTree(from: StorageNode, to: StorageNode) {
    from.children().forEach { node ->
        if (node.isDirectory) copyTree(node, dirOrCreate(to, node.name)) else copyFile(node, to)
    }
}
