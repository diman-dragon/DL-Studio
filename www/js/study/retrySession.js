import mistakesOf from './mistakesOf.js';
/** Короткий дополнительный круг по ошибкам сессии: расписание и история не меняются (gradeCard), задания строятся заново. */
export default function retrySession(session) {
  const order = mistakesOf(session);
  Object.assign(session, { order, plan: new Set(), planTotal: 0, total: order.length, pos: 0, log: [], tasks: {}, shown: {}, undo: null, retry: true });
}
