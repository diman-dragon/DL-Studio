import { DatabaseSync } from 'node:sqlite';
import { readFileSync } from 'node:fs';

const SCHEMA = 'android/app/src/main/java/com/example/lingocards/db/Schema.java';
const TABLES = ['reviews', 'cards', 'decks', 'languages', 'settings'];

/** Подменяет Capacitor-мост: SQLite в памяти (схема берётся из Schema.java) + заглушки SAF. */
export default function installBridge() {
  const db = new DatabaseSync(':memory:');
  db.exec('PRAGMA foreign_keys=ON');
  readFileSync(SCHEMA, 'utf8').match(/"(CREATE[^"]+)"/g).forEach(s => db.exec(s.slice(1, -1)));
  const io = { picked: null, saved: null, spoken: null, said: [], awake: null, listened: null, heard: null, icon: null, notes: [] };
  const lastId = () => Number(db.prepare('SELECT last_insert_rowid() i').get().i);
  const handlers = {
    // как в Java: rawQuery принимает только строки
    DbQuery: a => ({ rows: db.prepare(a.sql).all(...a.params.map(String)).map(r => ({ ...r })) }),
    DbRun: a => { db.prepare(a.sql).run(...a.params); return { id: lastId() }; },
    DbBatch: a => { db.exec('BEGIN'); a.statements.forEach(s => db.prepare(s.sql).run(...s.params)); db.exec('COMMIT'); return {}; },
    SafOpen: () => { if (!io.picked) throw new Error('cancelled'); return io.picked; }, // null = пользователь закрыл диалог
    SafSave: a => { io.saved = a; return {}; },
    BackupCreate: () => ({ data: 'AAAA' }),
    BackupRestore: () => ({ safety: 'pre-restore-1.db' }),
    TtsSpeak: a => { io.spoken = a; io.said.push(a); return {}; },
    TtsStop: () => ({}),
    SpeechListen: a => { io.listened = a; if (io.heard instanceof Error) throw io.heard; return io.heard; },
    IconLevel: a => { io.icon = a; return { level: a.level ?? 0 }; },
    AnkiNotes: () => ({ notes: io.notes }),
    KeepAwake: a => { io.awake = a.on; return {}; }
  };
  // как в реальном WebView без бандлера: есть nativePromise, но НЕТ registerPlugin
  globalThis.Capacitor = { nativePromise: async (name, method, a) => handlers[name](a) };
  const reset = () => TABLES.forEach(t => db.exec(`DELETE FROM ${t}`));
  return { db, io, reset };
}
