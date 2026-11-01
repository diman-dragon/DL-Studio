import dbQuery from '../native/dbQuery.js';
import scopeClause from './scopeClause.js';
/** Карточки области; tts — голос проекта, pname/dname — для подписей, ll — когда карточку последний раз тренировали в режиме mode (0 — ни разу). */
export default function listCardsInScope(scope, mode = 'srs') {
  const { where, params } = scopeClause(scope);
  return dbQuery(
    "SELECT c.*,l.tts tts,l.name pname,d.name dname,(SELECT COALESCE(MAX(r.ts),0) FROM reviews r WHERE r.card_id=c.id AND r.mode=?) ll " +
    `FROM cards c JOIN decks d ON d.id=c.deck_id JOIN languages l ON l.id=d.language_id WHERE ${where} ORDER BY l.name,d.id,c.ord,c.id`, [mode, ...params]);
}
