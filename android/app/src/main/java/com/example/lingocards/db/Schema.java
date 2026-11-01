package com.example.lingocards.db;

final class Schema {
    static final String[] DDL = {
        "CREATE TABLE languages(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, tts TEXT NOT NULL DEFAULT 'en-US')",
        "CREATE TABLE decks(id INTEGER PRIMARY KEY AUTOINCREMENT, language_id INTEGER NOT NULL REFERENCES languages(id) ON DELETE CASCADE, name TEXT NOT NULL, created INTEGER NOT NULL, kind TEXT NOT NULL DEFAULT 'cards')",
        "CREATE TABLE cards(id INTEGER PRIMARY KEY AUTOINCREMENT, deck_id INTEGER NOT NULL REFERENCES decks(id) ON DELETE CASCADE, front TEXT NOT NULL, back TEXT NOT NULL, example TEXT, example_tr TEXT, ord INTEGER NOT NULL DEFAULT 0, due INTEGER NOT NULL DEFAULT 0, interval INTEGER NOT NULL DEFAULT 0, ease REAL NOT NULL DEFAULT 2.5, reps INTEGER NOT NULL DEFAULT 0, speaker INTEGER NOT NULL DEFAULT 0, cloze TEXT, cloze_answer TEXT, cloze_tr TEXT)",
        "CREATE INDEX idx_cards_deck ON cards(deck_id, ord)",
        "CREATE TABLE settings(key TEXT PRIMARY KEY, value TEXT)",
        "CREATE TABLE reviews(id INTEGER PRIMARY KEY AUTOINCREMENT, card_id INTEGER NOT NULL REFERENCES cards(id) ON DELETE CASCADE, ts INTEGER NOT NULL, grade TEXT NOT NULL, think INTEGER, look INTEGER, fresh INTEGER NOT NULL DEFAULT 0, mode TEXT NOT NULL DEFAULT 'srs', hinted INTEGER NOT NULL DEFAULT 0, plays INTEGER NOT NULL DEFAULT 0)",
        "CREATE INDEX idx_reviews_ts ON reviews(ts)",
        "CREATE INDEX idx_reviews_card ON reviews(card_id)",
        "CREATE TABLE pronunciations(id INTEGER PRIMARY KEY AUTOINCREMENT, card_id INTEGER NOT NULL REFERENCES cards(id) ON DELETE CASCADE, ts INTEGER NOT NULL, engine TEXT, transcript TEXT, score REAL, audio TEXT)",
        "CREATE INDEX idx_pron_card ON pronunciations(card_id, ts)",
        "CREATE TABLE games(id INTEGER PRIMARY KEY AUTOINCREMENT, ts INTEGER NOT NULL, scope TEXT NOT NULL, score INTEGER NOT NULL, correct INTEGER NOT NULL, wrong INTEGER NOT NULL, best_combo INTEGER NOT NULL DEFAULT 0, seconds INTEGER NOT NULL DEFAULT 0)",
        "CREATE INDEX idx_games_ts ON games(ts)"
    };
}
