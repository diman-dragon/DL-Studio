package com.dlstudio.app.storage

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MoveStorageTest {
    private fun filled(): File = tempDir().also {
        ensureLayout(FileNode(it))
        it.putFile("DL-Studio/library/a.txt", "альфа")
        it.putFile("DL-Studio/media/deep/er/b.bin", "бета-бета")
    }

    @Test fun movesEverythingAndRemovesOld() {
        val old = filled()
        val fresh = tempDir()
        moveStorage(FileNode(old), FileNode(fresh))
        assertEquals("альфа", File(fresh, "DL-Studio/library/a.txt").readText())
        assertEquals("бета-бета", File(fresh, "DL-Studio/media/deep/er/b.bin").readText())
        StorageLayout.FOLDERS.forEach { assertTrue(File(fresh, "DL-Studio/$it").isDirectory) }
        assertFalse(File(old, "DL-Studio").exists())
    }

    @Test fun firstChoiceWithoutOldJustCreatesLayout() {
        val fresh = tempDir()
        moveStorage(null, FileNode(fresh))
        assertTrue(File(fresh, "DL-Studio/backups").isDirectory)
    }

    @Test fun sameFolderIsNoOp() {
        val old = filled()
        moveStorage(FileNode(old), FileNode(old))
        assertEquals("альфа", File(old, "DL-Studio/library/a.txt").readText())
    }

    @Test fun existingFileAtTargetIsReplaced() {
        val old = filled()
        val fresh = tempDir()
        fresh.putFile("DL-Studio/library/a.txt", "старый")
        moveStorage(FileNode(old), FileNode(fresh))
        assertEquals("альфа", File(fresh, "DL-Studio/library/a.txt").readText())
    }
}
