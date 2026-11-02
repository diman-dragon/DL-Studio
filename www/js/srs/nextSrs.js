import { DAY_MS } from '../constants/DAY_MS.js';
import { AGAIN_MS } from '../constants/AGAIN_MS.js';
import { LAPSE_KEEP } from '../constants/LAPSE_KEEP.js';
import nextInterval from './nextInterval.js';
import nextEase from './nextEase.js';
/**
 * Упрощённый SM-2: again → через 10 минут, остальное — через N дней. due — мс.
 * «Снова» мягкое: сохраняется 25% интервала, ease падает на 0,1 (при повторном провале подряд — на 0,2).
 * pace — темп колоды: множитель интервала (меньше 1 — повторять чаще, больше — реже); интервал не короче суток.
 */
export default function nextSrs(card, grade, now = Date.now(), pace = 1) {
  if (grade === 'again') {
    const lapsing = card.reps === 0 && card.due > 0;
    const interval = lapsing ? card.interval : Math.floor(card.interval * LAPSE_KEEP);
    return { ease: nextEase(card.ease, grade, lapsing), interval, reps: 0, due: now + AGAIN_MS };
  }
  const interval = Math.max(1, Math.round(nextInterval(card, grade) * pace));
  return { ease: nextEase(card.ease, grade), interval, reps: card.reps + 1, due: now + interval * DAY_MS };
}
