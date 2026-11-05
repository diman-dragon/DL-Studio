package com.dlstudio.app.storage

import java.io.File
import java.nio.file.Files

/** Новая пустая временная папка для теста. */
fun tempDir(): File = Files.createTempDirectory("dlstudio").toFile().also { it.deleteOnExit() }
