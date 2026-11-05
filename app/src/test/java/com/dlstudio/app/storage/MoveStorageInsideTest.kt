package com.dlstudio.app.storage

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class MoveStorageInsideTest {
    @Test fun targetInsideOldIsRejectedAndDataKept() {
        val old = tempDir()
        ensureLayout(FileNode(old))
        old.putFile("DL-Studio/library/a.txt", "альфа")
        val inside = File(old, "DL-Studio/library")
        try {
            moveStorage(FileNode(old), FileNode(inside))
            fail("ожидалось InsideStorageException")
        } catch (e: InsideStorageException) {
            assertEquals("альфа", File(old, "DL-Studio/library/a.txt").readText())
        }
    }
}
