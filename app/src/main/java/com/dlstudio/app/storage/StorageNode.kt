package com.dlstudio.app.storage

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
