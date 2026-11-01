import dbRun from '../native/dbRun.js';
export default function updateCard(id, { front, back, example, exampleTr, cloze, clozeAnswer, clozeTr }) {
  return dbRun('UPDATE cards SET front=?,back=?,example=?,example_tr=?,cloze=?,cloze_answer=?,cloze_tr=? WHERE id=?', [front, back, example || null, exampleTr || null, cloze || null, clozeAnswer || null, clozeTr || null, id]);
}
