import dbBatch from '../native/dbBatch.js';
const SQL = 'INSERT INTO cards(deck_id,front,back,example,example_tr,ord,speaker) VALUES(?,?,?,?,?,?,?)';
/** cards: [{front, back, example?, exampleTr?}] — атомарно. */
export default function insertCards(deckId, cards, startOrd = 0) {
  return dbBatch(cards.map((c, i) => ({
    sql: SQL,
    params: [deckId, c.front, c.back, c.example || null, c.exampleTr || null, startOrd + i, c.speaker || 0]
  })));
}
