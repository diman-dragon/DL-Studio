package com.example.lingocards.db;

/** Схема 5 → 6: данные для заданий с пропуском слова в фразе. */
final class MigrateV6 {
    static final String[] SQL = {
        "ALTER TABLE cards ADD COLUMN cloze TEXT",
        "ALTER TABLE cards ADD COLUMN cloze_answer TEXT",
        "ALTER TABLE cards ADD COLUMN cloze_tr TEXT"
    };
}
