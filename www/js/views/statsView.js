import { state } from '../state/appState.js';
import listReviewsSince from '../repo/listReviewsSince.js';
import countReviewsSince from '../repo/countReviewsSince.js';
import listDeckStats from '../repo/listDeckStats.js';
import listDeckReviewStats from '../repo/listDeckReviewStats.js';
import listHardCards from '../repo/listHardCards.js';
import listDueSoon from '../repo/listDueSoon.js';
import listWeakWords from '../repo/listWeakWords.js';
import listPronunciationsSince from '../repo/listPronunciationsSince.js';
import loadToday from '../study/loadToday.js';
import dayStart from '../util/dayStart.js';
import header from '../components/header.js';
import pagedList from '../components/pagedList.js';
import layoutToggle from '../components/layoutToggle.js';
import statEntry from '../components/statEntry.js';
import statSheet from '../components/statSheet.js';
import PARAMS from '../stats/index.js';
/**
 * Статистика: параметры — элементы списка / сетки, как в библиотеке и учёбе (значок, название, значение, пояснение), страницами.
 * Нажатие открывает подробное окно параметра (state.sheet = 'stat:<номер>'). Всё из истории интервальных повторений.
 */
export default async function statsView() {
  const since = dayStart(-29);
  const [rows, total, today, decks, perDeck, hard, dues, speech, weak] = await Promise.all([
    listReviewsSince(since), countReviewsSince(0), loadToday(), listDeckStats(), listDeckReviewStats(since), listHardCards(8), listDueSoon(dayStart(7)),
    listPronunciationsSince(since), listWeakWords(since, 6)]);
  const params = PARAMS.map(f => f({ rows, total, today, decks, perDeck, hard, dues, speech, weak, since7: dayStart(-6) }));
  const open = /^stat:(\d+)$/.exec(state.sheet ?? '')?.[1];
  const items = params.map((stat, index) => statEntry({ stat, index }));
  return header('Статистика', { right: layoutToggle() }) + pagedList(items) + (params[open] ? statSheet(params[open]) : '');
}
