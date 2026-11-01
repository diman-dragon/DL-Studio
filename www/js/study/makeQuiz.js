import shuffle from '../util/shuffle.js';
import spokenText from './spokenText.js';

const choiceOf = (answer, values) => {
  const pool = [...new Set(values)].filter(value => value && value !== answer);
  return pool.length < 3 ? null : shuffle([answer, ...shuffle(pool).slice(0, 3)]);
};
const kindOf = (choice, card, fixed = false) => {
  if (fixed) return `visual-${choice ? 'choice' : 'type'}`;
  const family = card.cloze ? ['visual', 'audio', 'cloze'][Math.floor(Math.random() * 3)] : ['visual', 'audio'][Math.floor(Math.random() * 2)];
  return `${family}-${choice ? 'choice' : 'type'}`;
};
/** Задание SRS: выбор показывается примерно в 4 раза чаще ввода. */
export default function makeQuiz(session, card, dir, requested = null) {
  const fixedKind = requested?.includes('-') ? requested : null;
  const check = fixedKind ? (fixedKind.endsWith('-choice') ? 'choice' : 'type') : requested || (Math.random() < 0.8 ? 'choice' : Math.random() < 0.25 ? 'speak' : 'type');
  if (check === 'flip') return null;
  if (check === 'speak') return { id: card.id, kind: 'speak', answer: spokenText(card), result: null, hint: 0, typed: '' };
  let kind = fixedKind || kindOf(check === 'choice', card, !!requested), family = kind.split('-')[0], mode = check;
  if (mode === 'type' && (family === 'audio' || (family === 'visual' && dir === 'fwd'))) {
    mode = 'choice';
    kind = `${family}-choice`;
  }
  const answer = family === 'cloze' ? card.cloze_answer : family === 'audio' || dir === 'fwd' ? card.back : card.front;
  const base = { id: card.id, answer, result: null, hint: 0, typed: '' };
  if (family === 'cloze') base.prompt = card.cloze;
  if (mode === 'type') return { ...base, kind };
  const values = [...session.map.values()].filter(c => c.id !== card.id).map(c => family === 'cloze' ? c.cloze_answer : family === 'audio' || dir === 'fwd' ? c.back : c.front);
  const options = choiceOf(answer, values);
  return options ? { ...base, kind, options, picked: null } : { ...base, kind: `${family}-type` };
}
