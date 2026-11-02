import { track } from '../state/trackState.js';
import fitFaces from '../dom/fitFaces.js';
import buzz from '../dom/buzz.js';
/** «Показать текстом» — подсказка в «На слух» и «Говорить»: слово появляется на карточке, ответ засчитывается как подсказанный. */
export default function showText() {
  const text = document.querySelector('.hidtext'), btn = document.querySelector('.showtext'), cue = document.querySelector('.audio-cue');
  if (text) text.hidden = false;
  if (btn) btn.hidden = true;
  if (cue) cue.hidden = true;
  fitFaces();
  track.hinted = true;
  buzz();
}
