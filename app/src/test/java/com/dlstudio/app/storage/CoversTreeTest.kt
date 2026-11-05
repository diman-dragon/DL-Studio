package com.dlstudio.app.storage

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoversTreeTest {
    @Test fun detectsMissingFileAndSizeMismatch() {
        val a = tempDir().apply { putFile("x/f.txt", "123") }
        val b = tempDir().apply { putFile("x/f.txt", "123") }
        assertTrue(coversTree(FileNode(b), FileNode(a)))
        b.putFile("x/f.txt", "1234")
        assertFalse(coversTree(FileNode(b), FileNode(a)))
        val c = tempDir()
        assertFalse(coversTree(FileNode(c), FileNode(a)))
    }
}
