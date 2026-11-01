package com.example.lingocards.db;

import com.example.lingocards.core.AtomicPlugin;

/** Базовый класс SQLite-плагинов: запросы не блокируют главный поток Android. */
abstract class DbPlugin extends AtomicPlugin {
    @Override protected boolean runInBackground() { return true; }
}
