import speakSlow from '../tts/speakSlow.js';
import pulse from '../dom/pulse.js';
/** «Медленно» (level 1) и «Ещё медленнее» (level 2) на карточке: повтор по словам с паузой, только для этой карточки. */
export default function replaySlow(d, el) {
  pulse(el);
  return speakSlow(d.t, d.lang || 'en-US', Number(d.level));
}
