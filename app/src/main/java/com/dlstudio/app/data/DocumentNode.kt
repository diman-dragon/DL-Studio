package com.dlstudio.app.data

import android.content.Context
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import com.dlstudio.app.storage.StorageNode
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/** Узел дерева SAF: папка, которую пользователь выбрал системным выбором папки. */
class DocumentNode(private val context: Context, private val doc: DocumentFile) : StorageNode {
    private fun wrap(file: DocumentFile) = DocumentNode(context, file)

    override val id: String get() = "${doc.uri.authority}:${DocumentsContract.getDocumentId(doc.uri)}"
    override val name: String get() = doc.name ?: ""
    override val isDirectory: Boolean get() = doc.isDirectory
    override val size: Long get() = doc.length()
    override fun children(): List<StorageNode> = doc.listFiles().map(::wrap)
    override fun child(name: String): StorageNode? = doc.findFile(name)?.let(::wrap)
    override fun createDirectory(name: String): StorageNode =
        wrap(doc.createDirectory(name) ?: throw IOException("Не создана папка $name"))
    override fun createFile(name: String): StorageNode =
        wrap(doc.createFile(mimeOf(name), name) ?: throw IOException("Не создан файл $name"))
    override fun openInput(): InputStream =
        context.contentResolver.openInputStream(doc.uri) ?: throw IOException("Не открыт $name")
    override fun openOutput(): OutputStream =
        context.contentResolver.openOutputStream(doc.uri, "wt") ?: throw IOException("Не открыт $name")
    override fun delete(): Boolean = doc.delete()
}
