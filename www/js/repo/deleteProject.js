import dbBatch from '../native/dbBatch.js';
/** Проект (колоды и карточки уходят каскадом) и параметры повторения его колод (settings: deck:<id>:*). */
export default function deleteProject(id) {
  const own = "key LIKE 'deck:%' AND CAST(substr(key, 6) AS INTEGER) IN (SELECT id FROM decks WHERE language_id=?)";
  return dbBatch([{ sql: `DELETE FROM settings WHERE ${own}`, params: [id] }, { sql: 'DELETE FROM languages WHERE id=?', params: [id] }]);
}
