import { track } from '../state/trackState.js';
import currentCard from './currentCard.js';
import peekLater from './peekLater.js';
import textLater from './textLater.js';
/** Новая карточка на экране: счётчики с нуля. revisit — пользователь вернулся к ней свайпом назад. */
export default function trackShow(session, revisit = false) {
  Object.assign(track, {
    n: track.n + 1, live: !!currentCard(session), side: 'front', since: Date.now(), front: 0, back: 0,
    think: null, flips: 0, revisit, away: false, correct: null, hinted: false, plays: 0
  });
  peekLater();
  textLater();
}
