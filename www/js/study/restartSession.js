import listCardsInScope from '../repo/listCardsInScope.js';
import buildOrder from './buildOrder.js';
import loadLimits from './loadLimits.js';
/** Новая очередь из базы: свежие просроченные и актуальный остаток дневного лимита («план») либо следующие давно не тренированные. */
export default async function restartSession(session) {
  const cards = await listCardsInScope(session.scope, session.mode);
  session.map = new Map(cards.map(c => [c.id, c]));
  session.limits = await loadLimits();
  const { order, plan } = buildOrder(session.mode, cards, session.limits);
  Object.assign(session, { order, plan: new Set(plan), planTotal: plan.length, total: order.length, pos: 0, log: [], tasks: {}, undo: null });
}
