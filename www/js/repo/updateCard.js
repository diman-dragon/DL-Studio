import dbRun from '../native/dbRun.js';
export default function updateCard(id, { front, back, example, exampleTr }) {
  return dbRun('UPDATE cards SET front=?,back=?,example=?,example_tr=? WHERE id=?', [front, back, example || null, exampleTr || null, id]);
}
