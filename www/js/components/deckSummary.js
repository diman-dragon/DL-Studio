export default function deckSummary({ project, cards, due, fresh }) {
  return `<div class="sub">${project}<br>${cards} карточек<br>${due} к повторению<br>${fresh} новых</div>`;
}
