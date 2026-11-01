import dbQuery from '../native/dbQuery.js';
import dayStart from '../util/dayStart.js';
/** Все колоды со счётчиками интервального повторения. */
export default function listDeckStats() {
  return dbQuery(
    'SELECT d.id,d.name,d.kind,l.id project_id,l.name pname,COUNT(c.id) total,COALESCE(SUM(c.due>0 AND c.due<=?),0) due,' +
    'COALESCE(SUM(c.due=0),0) fresh,COALESCE(SUM(c.due>0 AND c.due<?),0) late,COALESCE(SUM(c.interval>=21),0) mature FROM decks d JOIN languages l ON l.id=d.language_id ' +
    'LEFT JOIN cards c ON c.deck_id=d.id GROUP BY d.id ORDER BY l.name,d.id', [Date.now(), dayStart()]);
}
