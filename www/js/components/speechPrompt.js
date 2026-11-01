import big from './big.js';
import button from './button.js';
import micIcon from './micIcon.js';
import speechResult from './speechResult.js';

export default function speechPrompt(card, speech) {
  const mic = button({ act: 'record', cls: 'speak xl mic', label: micIcon(), aria: 'Сказать', data: { lang: card.tts } });
  const tips = button({ act: 'showText', cls: 'sm showtext', label: 'Показать текст' })
    + button({ act: 'play', cls: 'sm', label: '🔈 Послушать', data: { t: card.front, lang: card.tts } });
  return `<div class="muted">Скажите вслух</div>${big(card.back)}${big(card.front, 'hidtext', ' hidden')}`
    + `${mic}<div id="speechres">${speechResult(speech)}</div><div class="row">${tips}</div>`;
}
