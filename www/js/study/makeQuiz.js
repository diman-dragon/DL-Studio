import spokenText from './spokenText.js';
import quizAnswer from './quizAnswer.js';
import quizCheck from './quizCheck.js';
import quizChoices from './quizChoices.js';
/**
 * Задание SRS (вызывается один раз на карточку за сессию — см. study/taskFor). requested — способ (flip | choice | type | speak) или вид (audio-choice…); без него вид выбирает вызывающий (study/quizPick).
 * Ввод невозможен «на слух» и при показе слова изучаемого языка (направление fwd: ответ — перевод) — тогда выбор. Мало вариантов — ввод.
 */
export default function makeQuiz(session, card, dir, requested = null) {
  const fixed = requested?.includes('-') ? requested : null, check = quizCheck(requested, fixed);
  if (check === 'flip') return null;
  if (check === 'speak') return { id: card.id, kind: 'speak', answer: spokenText(card), result: null, hint: 0, typed: '' };
  const family = fixed ? fixed.split('-')[0] : 'visual';
  const typeable = check === 'type' && family !== 'audio' && !(family === 'visual' && dir === 'fwd');
  const answer = quizAnswer(card, family, dir);
  const base = { id: card.id, answer, result: null, hint: 0, typed: '', ...(family === 'cloze' ? { prompt: card.cloze } : {}) };
  if (typeable) return { ...base, kind: `${family}-type` };
  const values = [...session.map.values()].filter(c => c.id !== card.id).map(c => quizAnswer(c, family, dir));
  const options = quizChoices(answer, values);
  return options ? { ...base, kind: `${family}-choice`, options, picked: null } : { ...base, kind: `${family}-type` };
}
