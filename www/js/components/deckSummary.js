export default function deckSummary({ project, cards, due, fresh }) {
  return `<div class="sub">${project} · ${cards} карточек · повторить ${due} · новых ${fresh}</div>`;
}
