import { state } from '../state/appState.js';
/** Направление карточки выбирается автоматически и фиксируется до конца текущей сессии. */
export default function cardDir(session, card) {
  return (session.dirs[card.id] ??= Math.random() < 0.5 ? 'fwd' : 'rev');
}
