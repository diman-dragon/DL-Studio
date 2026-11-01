import { state } from '../state/appState.js';
import { BEHAVIOR } from '../constants/BEHAVIOR.js';
import getDeckWithProject from '../repo/getDeckWithProject.js';
import listCards from '../repo/listCards.js';
import header from '../components/header.js';
import backButton from '../components/backButton.js';
import button from '../components/button.js';
import entry from '../components/entry.js';
import pagedList from '../components/pagedList.js';
import layoutToggle from '../components/layoutToggle.js';
import itemActions from '../components/itemActions.js';
import cardMark from '../components/cardMark.js';
import emptyState from '../components/emptyState.js';
import row from '../components/row.js';
/** Колода: карточки (метка срока, ✎ изменить, 🗑 удалить); тап по карточке — тоже правка. */
export default async function deckView() {
  const [deck, cards] = await Promise.all([getDeckWithProject(state.nav.id), listCards(state.nav.id)]);
  const due = cards.filter(c => c.due > 0 && c.due <= Date.now()).length, fresh = cards.filter(c => !c.due).length;
  const items = cards.map(c => entry({
    act: 'editCard', id: c.id, title: (c.agains >= BEHAVIOR.leech ? '🐛 ' : '') + c.front, sub: c.back, mark: cardMark(c), acts: itemActions('editCard', 'delCard', c.id)
  }));
  const body = items.length ? pagedList(items) : emptyState({ icon: '🃏', title: 'Карточек нет', text: 'Добавьте первую карточку.' });
  return header(deck.name, { left: backButton('goProject', deck.language_id), right: layoutToggle() }) +
    `<div class="sub">${deck.pname} · ${cards.length} карточек · повторить ${due} · новых ${fresh}</div>` + body + row(button({ act: 'newCard', cls: 'p', label: '+ Карточка' }), 'foot');
}
