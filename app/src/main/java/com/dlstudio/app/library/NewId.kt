package com.dlstudio.app.library

import java.util.UUID

/** Новый уникальный id узла. */
fun newId(): String = UUID.randomUUID().toString()
