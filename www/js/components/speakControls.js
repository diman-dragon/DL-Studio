import speakButton from './speakButton.js';
import slowButton from './slowButton.js';
/** Озвучка на карточке: обычная (круглая) и два замедленных повтора по бокам. */
export default function speakControls(text, lang) {
  return `<div class="speak-row">${slowButton(text, lang, 1)}${speakButton(text, lang)}${slowButton(text, lang, 2)}</div>`;
}
