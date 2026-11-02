import shuffle from '../util/shuffle.js';
import quizPool from './quizPool.js';
/** Вес задания: ввод с клавиатуры показываем в 4 раза реже остальных (у остальных вес 1). */
const weight = kind => (kind.endsWith('-type') ? 0.25 : 1);
/**
 * Какое задание дать карточке: перевод (visual), на слух (audio), пропуск (cloze, если он есть), произношение (speak, если распознавание есть),
 * ввод (visual-type только когда нужно написать слово изучаемого языка, cloze-type). Если чужих ответов мало для выбора (маленькая колода), выбор
 * заменяет обычная карточка (flip). Выбирается то, чьих показов в сессии меньше всего с учётом веса — получается равномерно, а ввод в 4 раза реже.
 * Счётчики — session.shown.
 */
export default function quizPick(session, card, dir) {
  const room = family => quizPool(session, card, family, dir).length >= 3;
  const choices = [['visual-choice', 'visual'], ['audio-choice', 'audio'], ...(card.cloze ? [['cloze-choice', 'cloze']] : [])];
  const kinds = [...choices.filter(([, f]) => room(f)).map(([k]) => k), ...(session.noSpeech ? [] : ['speak']),
    ...(card.cloze ? ['cloze-type'] : []), ...(dir === 'rev' ? ['visual-type'] : [])];
  if (choices.some(([, f]) => !room(f))) kinds.push('flip');
  const shown = (session.shown ??= {});
  const [pick] = shuffle(kinds).sort((a, b) => (shown[a] ?? 0) / weight(a) - (shown[b] ?? 0) / weight(b));
  shown[pick] = (shown[pick] ?? 0) + 1;
  return pick;
}
