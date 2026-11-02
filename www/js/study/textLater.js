import { track } from '../state/trackState.js';
import { BEHAVIOR } from '../constants/BEHAVIOR.js';
/** В задании «на слух» слово спрятано; если ответа нет textAfter мс, появляется кнопка «Показать текстом» (подсказка: ответ засчитается как «трудно»). */
export default function textLater() {
  const n = track.n;
  const timer = setTimeout(() => {
    if (track.n !== n || !track.live) return;
    globalThis.document?.querySelector('.showtext.wait')?.classList.remove('wait');
  }, BEHAVIOR.textAfter);
  timer.unref?.();
}
