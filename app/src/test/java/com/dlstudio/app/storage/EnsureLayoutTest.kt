package com.dlstudio.app.storage

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EnsureLayoutTest {
    @Test fun createsRootAndAllFolders() {
        val parent = tempDir()
        ensureLayout(FileNode(parent))
        assertTrue(File(parent, "DL-Studio").isDirectory)
        StorageLayout.FOLDERS.forEach { assertTrue(it, File(parent, "DL-Studio/$it").isDirectory) }
    }

    @Test fun repeatedCallKeepsFiles() {
        val parent = tempDir()
        ensureLayout(FileNode(parent))
        parent.putFile("DL-Studio/library/a.txt", "x")
        ensureLayout(FileNode(parent))
        assertEquals("x", File(parent, "DL-Studio/library/a.txt").readText())
    }
}
