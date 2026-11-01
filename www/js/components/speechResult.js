import esc from '../util/esc.js';
import { SPEECH } from '../constants/SPEECH.js';
/** Итог попытки произношения: процент сходства (цветом), что услышано, номер попытки. null — попыток ещё не было. */
export default function speechResult(s) {
  if (!s) return '';
  const cls = s.score >= SPEECH.good ? 'good' : s.score >= SPEECH.hard ? 'mid' : 'bad';
  return `<div class="sres ${cls}"><b>${Math.round(s.score * 100)}%</b><span>Услышано: «${esc(s.heard || '—')}» · попытка ${s.attempts}</span></div>`;
}
