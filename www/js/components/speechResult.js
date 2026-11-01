import esc from '../util/esc.js';
import { SPEECH } from '../constants/SPEECH.js';
/** Итог попытки произношения: процент сходства (цветом), что услышано, номер попытки. null — попыток ещё не было. */
export default function speechResult(s) {
  if (!s) return '';
  const cls = s.score >= SPEECH.good ? 'good' : s.score >= SPEECH.hard ? 'mid' : 'bad';
  const words = String(s.heard || '—').split(/\s+/).filter(Boolean).map(word => `<span class="speech-word">${esc(word)}</span>`).join(' ');
  const retry = cls === 'bad' ? '<span>Затруднение — покажите текст и повторите.</span>' : '';
  return `<div class="sres ${cls}"><b>${Math.round(s.score * 100)}%</b><span>Услышано:</span><span class="speech-words">${words}</span>${retry}<span>Попытка ${s.attempts}</span></div>`;
}
