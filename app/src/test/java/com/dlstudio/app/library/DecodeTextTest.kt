package com.dlstudio.app.library

import org.junit.Assert.assertEquals
import org.junit.Test

class DecodeTextTest {
    @Test fun utf8WithBomLosesBom() {
        assertEquals("кот", decodeText(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + "кот".toByteArray()))
    }

    @Test fun windows1251IsRecognized() {
        assertEquals("кот;cat", decodeText("кот;cat".toByteArray(charset("windows-1251"))))
    }

    @Test fun utf16WithBomIsRead() {
        assertEquals("кот", decodeText(byteArrayOf(0xFF.toByte(), 0xFE.toByte()) + "кот".toByteArray(Charsets.UTF_16LE)))
    }
}
