import big from './big.js';
import button from './button.js';
import speakControls from './speakControls.js';
import spokenText from '../study/spokenText.js';
/** Лицо задания «на слух»: звук без текста; через время кнопка «Показать текстом» становится видимой (study/textLater; место под неё занято сразу), слово — подсказка. */
export default function audioPrompt(c) {
  const text = spokenText(c);
  return `<div class="audio-cue">${big('🔊')}<div class="muted">Прослушайте карточку</div></div>${big(text, 'hidtext', ' hidden')}` +
    speakControls(text, c.tts) + button({ act: 'showText', cls: 'sm showtext wait', label: 'Показать текстом' });
}
