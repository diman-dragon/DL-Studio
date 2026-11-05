package com.dlstudio.app.storage

import java.io.IOException

/**
 * Переносит `DL-Studio` из `oldParent` в `newParent`: создаёт структуру на новом месте, копирует всё,
 * сверяет и только после этого стирает старую папку. Любая ошибка — старые данные остаются нетронутыми.
 */
fun moveStorage(oldParent: StorageNode?, newParent: StorageNode): StorageNode {
    val old = oldParent?.child(StorageLayout.ROOT)
    if (old != null && (old.id == newParent.id || containsId(old, newParent.id))) throw InsideStorageException()
    val target = ensureLayout(newParent)
    if (old == null || old.id == target.id) return target
    copyTree(old, target)
    if (!coversTree(target, old)) throw IOException("Проверка копии не пройдена")
    if (!old.delete()) throw IOException("Не удалось удалить старую папку")
    return target
}
