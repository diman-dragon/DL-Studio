import esc from '../util/esc.js';

export default function importPreview(draft) {
  const sample = draft.cards.slice(0, 3)
    .map(card => `${esc(card.front)} — ${esc(card.back)}`).join('<br>');
  return `<div class="muted note">Формат: ${esc(draft.format)}<br>`
    + `Найдено карточек: <b>${draft.cards.length}</b></div>`
    + `<div class="preview">${sample}</div>`;
}
