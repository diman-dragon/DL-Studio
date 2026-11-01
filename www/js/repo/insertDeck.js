import dbRun from '../native/dbRun.js';
/** Создаёт обычную карточную колоду. */
export default function insertDeck(languageId, name) {
  return dbRun('INSERT INTO decks(language_id,name,created,kind) VALUES(?,?,?,?)', [languageId, name, Date.now(), 'cards']);
}
