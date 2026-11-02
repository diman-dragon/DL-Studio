import shuffle from '../util/shuffle.js';
/** Вес задания: ввод с клавиатуры показываем в 4 раза реже остальных (у остальных вес 1). */
const weight = kind => (kind.endsWith('-type') ? 0.25 : 1);
/**
 * Какое задание дать карточке: перевод (visual), на слух (audio), пропуск (cloze, если он есть), произношение (speak) и ввод (visual-type только
 * когда нужно написать слово изучаемого языка, cloze-type). Выбирается то, чьих показов в сессии меньше всего с учётом веса, —
 * получается равномерно, а ввод ровно в 4 раза реже. Счётчики — session.shown.
 */
export default function quizPick(session, card, dir) {
  const kinds = ['visual-choice', 'audio-choice', 'speak', ...(card.cloze ? ['cloze-choice', 'cloze-type'] : []), ...(dir === 'rev' ? ['visual-type'] : [])];
  const shown = (session.shown ??= {});
  const score = kind => (shown[kind] ?? 0) * weight(kind) ** -1;
  const [pick] = shuffle(kinds).sort((a, b) => score(a) - score(b));
  shown[pick] = (shown[pick] ?? 0) + 1;
  return pick;
}
