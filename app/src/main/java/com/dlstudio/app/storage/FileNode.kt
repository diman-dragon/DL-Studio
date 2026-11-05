package com.dlstudio.app.storage

import java.io.File
import java.io.InputStream
import java.io.OutputStream

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
