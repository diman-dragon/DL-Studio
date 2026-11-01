import esc from '../util/esc.js';
import { DAY_MS } from '../constants/DAY_MS.js';
/** Итог сессии: пройдено, трудных, сколько вернётся уже завтра, и самые трудные слова. */
export default function sessionSummary(session) {
  const n = session.log.length;
  if (!n) return '';
  const hard = session.log.filter(x => x.grade === 'again' || x.grade === 'hard');
  const soon = new Set(session.log.map(x => x.id).filter(id => session.map.get(id).due <= Date.now() + DAY_MS)).size;
  const words = [...new Set(hard.map(x => x.id))].slice(0, 5).map(id => esc(session.map.get(id).front)).join(' · ');
  const cell = (v, t) => `<div><b>${v}</b><span>${t}</span></div>`;
  return `<div class="sum">${cell(n, 'пройдено')}${cell(hard.length, 'трудных')}${cell(soon, 'вернутся завтра')}</div>` +
    (words ? `<div class="muted hardwords">Труднее всего: ${words}</div>` : '');
}
