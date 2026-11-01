import dbQuery from '../native/dbQuery.js';
import dayStart from '../util/dayStart.js';
const COUNT = (where = '') => `(SELECT COUNT(*) FROM cards c JOIN decks d ON d.id=c.deck_id WHERE d.language_id=l.id${where ? ` AND ${where}` : ''})`;
/** n — колод, cards — всего, due — к повторению сейчас, overdue — просрочено, late — просрочено более двух суток. */
export default function listProjects() {
  return dbQuery(
    `SELECT l.id,l.name,l.tts,(SELECT COUNT(*) FROM decks d WHERE d.language_id=l.id) n,${COUNT()} cards,` +
    `${COUNT('c.due>0 AND c.due<=?')} due,${COUNT('c.due=0')} fresh,` +
    `${COUNT('c.due>0 AND c.due<?')} overdue,${COUNT('c.due>0 AND c.due<?')} late FROM languages l ORDER BY l.name`, [Date.now(), Date.now(), dayStart(-2)]);
}
