import shuffle from '../util/shuffle.js';
import isNewCard from '../srs/isNewCard.js';
import takePlan from './takePlan.js';
import { NO_LIMITS } from '../constants/NO_LIMITS.js';
/**
 * { order, plan } — очередь id и «план дня» (его начало). Очередь = план: сессия идёт строго по нему.
 * srs — только план по study/takePlan (у каждой колоды свои пределы): сессия идёт строго по нему, дальше алгоритм не применяется. listen / speak — давно не тренированные первыми,
 * план — первые limits.max. cards — все карточки без плана. Порядок внутри групп СЛУЧАЙНЫЙ (карточки сначала перемешиваются, сортировка устойчива)
 * и дальше в рамках сессии не меняется (позиция — session.pos, возврат назад даёт ту же очередь).
 */
export default function buildOrder(mode, all, limits = NO_LIMITS) {
  const cards = shuffle(all);
  const ids = list => list.map(c => c.id);
  if (mode === 'cards') return { order: ids(cards), plan: [] };
  if (mode !== 'srs') {
    const sorted = [...cards].sort((a, b) => a.ll - b.ll);
    return { order: ids(sorted), plan: ids(sorted.slice(0, limits.max)) };
  }
  const now = Date.now();
  const due = cards.filter(c => c.due > 0 && c.due <= now).sort((a, b) => a.due - b.due);
  const fresh = cards.filter(isNewCard);
  const plan = ids(takePlan(due, fresh, limits));
  return { order: [...plan], plan };
}
