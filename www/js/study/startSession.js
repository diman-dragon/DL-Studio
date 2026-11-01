import listCardsInScope from '../repo/listCardsInScope.js';
import buildOrder from './buildOrder.js';
import loadCalibration from './loadCalibration.js';
import { NO_LIMITS } from '../constants/NO_LIMITS.js';
/**
 * session = { scope, mode, map(id→карточка), order[], plan(Set id), planTotal, pos, total, limits, calib, log[], dirs{}, undo }.
 * scope — 'all' | 'p:<проект>' | 'd:<колода>'; mode всегда 'srs'. Всё читается из базы при каждом запуске:
 * карточки и их расписание, лимиты, оценки поведения под человека. Позиция сессии нигде не хранится — очередь строится заново.
 * plan — часть очереди «по плану дня»; когда она пройдена, учёба идёт дальше без лимита.
 */
export default async function startSession(scope, mode, limits = NO_LIMITS) {
  const cards = await listCardsInScope(scope, mode);
  const { order, plan } = buildOrder(mode, cards, limits);
  return {
    scope, mode, map: new Map(cards.map(c => [c.id, c])), order, plan: new Set(plan), planTotal: plan.length, pos: 0, total: order.length,
    limits, calib: await loadCalibration(), log: [], dirs: {}, undo: null
  };
}
