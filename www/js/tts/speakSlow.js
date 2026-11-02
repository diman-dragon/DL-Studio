import { state } from '../state/appState.js';
import { slow } from '../state/slowState.js';
import { SLOW } from '../constants/SLOW.js';
import ttsSpeak from '../native/ttsSpeak.js';
import sleep from '../util/sleep.js';
/**
 * Замедленный повтор по словам: level 1 | 2 — скорость и пауза между словами (constants/SLOW).
 * Только для этой озвучки: общая скорость речи не меняется (если она ещё медленнее, берётся она). Новая озвучка или уход с карточки прерывают повтор.
 */
export default async function speakSlow(text, lang, level) {
  const { rate, pause } = SLOW[level - 1] ?? SLOW[0], run = ++slow.run;
  const opts = { rate: Math.min(rate, Number(state.prefs.ttsRate)), pitch: Number(state.prefs.ttsPitch), wait: true };
  const words = text.split(/\s+/).filter(w => /[\p{L}\p{N}]/u.test(w));
  for (const [i, word] of words.entries()) {
    if (run !== slow.run) return;
    await ttsSpeak(word, lang, opts);
    if (i < words.length - 1 && run === slow.run) await sleep(pause);   // пауза только между словами
  }
}
