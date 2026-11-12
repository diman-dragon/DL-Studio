package com.dlstudio.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import com.dlstudio.app.settings.StorageStatus
import com.dlstudio.app.storage.FileNode
import com.dlstudio.app.storage.InsideStorageException
import com.dlstudio.app.storage.StorageNode
import com.dlstudio.app.storage.moveStorage
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Блокирующая операция (вызывать вне главного потока): переносит DL-Studio в выбранную папку и запоминает её. */
fun applyFolder(context: Context, prefs: Prefs, tree: Uri) {
    val rw = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
    context.contentResolver.takePersistableUriPermission(tree, rw)
    val picked = DocumentFile.fromTreeUri(context, tree) ?: throw IOException("Папка недоступна")
    moveStorage(openParent(context, prefs), DocumentNode(context, picked))
    prefs.storageUri?.takeIf { it != tree.toString() }?.let {
        runCatching { context.contentResolver.releasePersistableUriPermission(Uri.parse(it), rw) }
    }
    prefs.storageUri = tree.toString()
}

/** Папка по умолчанию — личная папка приложения: разрешения не нужны, пока пользователь не выбрал свою. */
fun defaultParent(context: Context): StorageNode = FileNode(context.getExternalFilesDir(null) ?: context.filesDir)

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

/** Имя выбранной пользователем папки; null — папка по умолчанию. */
fun folderName(context: Context, prefs: Prefs): String? =
    prefs.storageUri?.let { DocumentFile.fromTreeUri(context, Uri.parse(it))?.name }

/** MIME по расширению имени; неизвестное — octet-stream (провайдер оставит имя как есть). */
fun mimeOf(name: String): String {
    val extension = name.substringAfterLast('.', "").lowercase()
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
}

/** Перенос в фоне; результат — статус для показа пользователю. Старые данные при ошибке не страдают. */
suspend fun moveToFolder(context: Context, prefs: Prefs, tree: Uri): StorageStatus =
    withContext(Dispatchers.IO) {
        try {
            applyFolder(context, prefs, tree)
            StorageStatus.MOVED
        } catch (e: InsideStorageException) {
            StorageStatus.ERROR_INSIDE
        } catch (e: Exception) {
            StorageStatus.ERROR_IO
        }
    }

/** Папка, внутри которой лежит DL-Studio: выбранная пользователем (если ещё доступна) или по умолчанию. */
fun openParent(context: Context, prefs: Prefs): StorageNode {
    val tree = prefs.storageUri?.let { DocumentFile.fromTreeUri(context, Uri.parse(it)) }
    return if (tree != null && tree.exists() && tree.canWrite()) DocumentNode(context, tree) else defaultParent(context)
}
