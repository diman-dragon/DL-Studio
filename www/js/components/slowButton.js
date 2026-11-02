import button from './button.js';
const LABELS = { 1: ['🐢', 'медленно', 'Повторить медленно'], 2: ['🐌', 'ещё медленнее', 'Повторить очень медленно, с паузами'] };
/** Кнопка замедленного повтора, level 1 | 2; относится только к этой карточке. */
export default function slowButton(text, lang, level) {
  const [icon, caption, aria] = LABELS[level];
  return button({ act: 'replaySlow', cls: 'slow', label: `<span>${icon}</span><small>${caption}</small>`, aria, data: { t: text, lang, level } });
}
