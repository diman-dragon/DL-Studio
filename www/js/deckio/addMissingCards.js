import listCards from '../repo/listCards.js';
import nextCardOrd from '../repo/nextCardOrd.js';
import insertCards from '../repo/insertCards.js';
const keyOf = c => `${c.front}\u0000${c.back}\u0000${c.cloze || ''}\u0000${c.speaker || 0}`;
/**
 * Добавляет только те карточки, которых в колоде ещё нет — повторный импорт ничего не дублирует. Обычная колода: ключ «слово + перевод», повторы внутри файла
 * Повторы карточек внутри импорта отбрасываются.
 */
export default async function addMissingCards(deckId, cards, kind = 'cards') {
  const have = new Map();
  for (const c of await listCards(deckId)) have.set(keyOf(c), (have.get(keyOf(c)) ?? 0) + 1);
  const fresh = cards.filter(c => {
    const key = keyOf(c), n = have.get(key) ?? 0;
    have.set(key, n + 1);
    return n === 0;
  });
  if (fresh.length) await insertCards(deckId, fresh, await nextCardOrd(deckId));
  return fresh.length;
}
