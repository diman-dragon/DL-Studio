package com.dlstudio.app.ui.library

import com.dlstudio.app.library.LibNode

/** Открытый диалог: `node` null — создание; `delete` — подтверждение удаления, иначе правка. */
data class LibDialog(val node: LibNode?, val delete: Boolean = false)
