import dbBatch from '../native/dbBatch.js';
/** Колода и её собственные параметры повторения (settings: deck:<id>:*). */
export default function deleteDeck(id) {
  return dbBatch([{ sql: 'DELETE FROM settings WHERE key LIKE ?', params: [`deck:${id}:%`] }, { sql: 'DELETE FROM decks WHERE id=?', params: [id] }]);
}
