import test from 'node:test';
import assert from 'node:assert/strict';
import { DatabaseSync } from 'node:sqlite';
import { readFileSync } from 'node:fs';

const J = 'android/app/src/main/java/com/example/lingocards/db';
const strings = f => [...readFileSync(`${J}/${f}`, 'utf8').matchAll(/"((?:[^"\\]|\\.)+)"/g)].map(m => m[1]);
// схема версии 1.2.x (до истории повторений) — как её создавали старые установки
const V1 = [
  "CREATE TABLE languages(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, tts TEXT NOT NULL DEFAULT 'en-US')",
  'CREATE TABLE decks(id INTEGER PRIMARY KEY AUTOINCREMENT, language_id INTEGER NOT NULL REFERENCES languages(id) ON DELETE CASCADE, name TEXT NOT NULL, created INTEGER NOT NULL)',
  'CREATE TABLE cards(id INTEGER PRIMARY KEY AUTOINCREMENT, deck_id INTEGER NOT NULL REFERENCES decks(id) ON DELETE CASCADE, front TEXT NOT NULL, back TEXT NOT NULL, example TEXT, example_tr TEXT, ord INTEGER NOT NULL DEFAULT 0, due INTEGER NOT NULL DEFAULT 0, interval INTEGER NOT NULL DEFAULT 0, ease REAL NOT NULL DEFAULT 2.5, reps INTEGER NOT NULL DEFAULT 0)',
  'CREATE INDEX idx_cards_deck ON cards(deck_id, ord)',
  'CREATE TABLE deck_state(deck_id INTEGER NOT NULL REFERENCES decks(id) ON DELETE CASCADE, mode TEXT NOT NULL, pos INTEGER NOT NULL DEFAULT 0, order_json TEXT, PRIMARY KEY(deck_id, mode))',
  'CREATE TABLE settings(key TEXT PRIMARY KEY, value TEXT)'
];
const shape = db => Object.fromEntries(db.prepare("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'").all()
  .map(t => [t.name, db.prepare(`PRAGMA table_info(${t.name})`).all().map(c => `${c.name}:${c.type}:${c.notnull}:${c.dflt_value}:${c.pk}`)]));

const migrate = (db, ...files) => files.forEach(f => strings(f).forEach(sql => db.exec(sql)));
const freshDb = () => {
  const db = new DatabaseSync(':memory:');
  strings('Schema.java').filter(s => s.startsWith('CREATE')).forEach(s => db.exec(s));
  return db;
};
const seed = db => db.exec("INSERT INTO languages(name) VALUES('EN'); INSERT INTO decks(language_id,name,created) VALUES(1,'D',1); INSERT INTO cards(deck_id,front,back,due,interval,reps) VALUES(1,'a','б',5,3,2); INSERT INTO settings VALUES('today','{}'),('session:all:srs:-','{}'),('scope','all'),('theme','dark')");

test('миграция 1 → 6 даёт ту же схему, что и чистая установка; данные сохраняются, deck_state удаляется', () => {
  const old = new DatabaseSync(':memory:');
  V1.forEach(s => old.exec(s));
  seed(old);
  migrate(old, 'MigrateV2.java', 'MigrateV3.java', 'MigrateV4.java', 'MigrateV5.java', 'MigrateV6.java');
  assert.deepEqual(shape(old), shape(freshDb()));
  assert.equal(old.prepare('SELECT interval,reps FROM cards').get().interval, 3);
  assert.deepEqual(old.prepare('SELECT key FROM settings').all().map(r => r.key), ['theme']);   // лишнее из settings ушло, настройки остались
  assert.equal(old.prepare("SELECT COUNT(*) n FROM sqlite_master WHERE name='deck_state'").get().n, 0);
  old.exec('PRAGMA foreign_keys=ON');
  old.exec("INSERT INTO reviews(card_id,ts,grade) VALUES(1,1,'good'); INSERT INTO pronunciations(card_id,ts,score) VALUES(1,1,0.9)");
  assert.deepEqual({ ...old.prepare('SELECT mode,hinted,plays FROM reviews').get() }, { mode: 'srs', hinted: 0, plays: 0 });
  old.exec('DELETE FROM cards');                                  // история и проверки произношения удаляются каскадом
  assert.equal(old.prepare('SELECT (SELECT COUNT(*) FROM reviews)+(SELECT COUNT(*) FROM pronunciations) n').get().n, 0);
});

test('миграция 2 → 3 (установка 1.3.x): история повторений сохраняется и получает режим по умолчанию', () => {
  const v2 = new DatabaseSync(':memory:');
  V1.filter(s => !s.includes('deck_state')).forEach(s => v2.exec(s));
  migrate(v2, 'MigrateV2.java');
  seed(v2);
  v2.exec("INSERT INTO reviews(card_id,ts,grade,think,look,fresh) VALUES(1,5,'easy',2000,1500,1)");
  migrate(v2, 'MigrateV3.java', 'MigrateV4.java', 'MigrateV5.java', 'MigrateV6.java');
  assert.deepEqual(shape(v2), shape(freshDb()));
  assert.deepEqual({ ...v2.prepare('SELECT grade,think,fresh,mode FROM reviews').get() }, { grade: 'easy', think: 2000, fresh: 1, mode: 'srs' });
});

test('версия схемы повышена, миграции подключены, проверка резервной копии принимает копии всех версий', () => {
  assert.match(readFileSync(`${J}/DbConfig.java`, 'utf8'), /VER = 6/);
  assert.match(readFileSync(`${J}/DbHelper.java`, 'utf8'), /Migrate\.run\(db, from\)/);
  const m = readFileSync(`${J}/Migrate.java`, 'utf8');
  assert.match(m, /from < 2/);
  assert.match(m, /from < 3/);
  assert.match(m, /from < 4/);
  assert.match(m, /from < 5/);
  assert.match(m, /from < 6/);
  const v = readFileSync('android/app/src/main/java/com/example/lingocards/backup/IsValidDb.java', 'utf8');
  assert.match(v, /IN \('languages','decks','cards','settings'\)/);   // копии v1 (есть deck_state) и v2 тоже подходят
  assert.match(v, /getInt\(0\) == 4/);
});

test('экран не гаснет и вибрация: плагин зарегистрирован, разрешение в манифесте', () => {
  assert.match(readFileSync('android/app/src/main/java/com/example/lingocards/PluginList.java', 'utf8'), /KeepAwakePlugin\.class/);
  assert.match(readFileSync('android/app/src/main/AndroidManifest.xml', 'utf8'), /android\.permission\.VIBRATE/);
  assert.match(readFileSync('android/app/src/main/java/com/example/lingocards/screen/KeepAwakePlugin.java', 'utf8'), /FLAG_KEEP_SCREEN_ON/);
  assert.match(readFileSync('android/app/src/main/java/com/example/lingocards/tts/TtsReady.java', 'utf8'), /Thread\.sleep/);   // ждём запуска движка
});

test('миграция 4 → 6: колоды остаются обычными, у карточек есть данные для фраз; новая схема совпадает с чистой', () => {
  const v4 = new DatabaseSync(':memory:');
  strings('Schema.java').filter(s => s.startsWith('CREATE')).forEach(s => v4.exec(s.replace(", kind TEXT NOT NULL DEFAULT 'cards'", '').replace(', speaker INTEGER NOT NULL DEFAULT 0, cloze TEXT, cloze_answer TEXT, cloze_tr TEXT', '')));
  seed(v4);
  assert.equal(v4.prepare("SELECT COUNT(*) n FROM pragma_table_info('decks') WHERE name='kind'").get().n, 0);
  migrate(v4, 'MigrateV5.java', 'MigrateV6.java');
  assert.deepEqual(shape(v4), shape(freshDb()));
  assert.deepEqual({ ...v4.prepare('SELECT d.kind,c.speaker,c.front FROM decks d JOIN cards c ON c.deck_id=d.id').get() }, { kind: 'cards', speaker: 0, front: 'a' });
});
