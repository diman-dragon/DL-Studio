package com.dlstudio.app.storage

/** Выбранная папка находится внутри текущей папки DL-Studio: переносить её саму в себя нельзя. */
class InsideStorageException : Exception("Папка находится внутри DL-Studio")
