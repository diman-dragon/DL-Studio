import { slow } from '../state/slowState.js';
import ttsStop from '../native/ttsStop.js';
/** Прерывает озвучку и замедленный повтор по словам (смена карточки). */
export default function stopSlow() {
  slow.run++;
  return ttsStop().catch(() => {});
}
