import dbRun from '../native/dbRun.js';
/** Переименование или перенос обычной колоды. */
export default function updateDeck(id, name, projectId) { return dbRun('UPDATE decks SET name=?,language_id=?,kind=? WHERE id=?', [name, projectId, 'cards', id]); }
