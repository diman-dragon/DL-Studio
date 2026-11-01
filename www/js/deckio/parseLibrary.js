import { DEFAULT_TTS } from '../constants/DEFAULT_TTS.js';
import clean from '../util/clean.js';
import isValidCard from './isValidCard.js';
import normalizeCard from './normalizeCard.js';
const parseDeckOf = d => {
  if (!clean(d?.name) || !Array.isArray(d.cards)) throw new Error('У колоды нет name или cards[]');
  return { name: clean(d.name), kind: 'cards', cards: d.cards.filter(isValidCard).map(normalizeCard) };
};
/** Файл всей библиотеки (lingvadesk-library v1): проекты → колоды → карточки. */
export default function parseLibrary(j) {
  if (!Array.isArray(j.projects) || !j.projects.length) throw new Error('В файле нет projects[]');
  return {
    projects: j.projects.map(p => {
      if (!clean(p?.name) || !Array.isArray(p.decks)) throw new Error('У проекта нет name или decks[]');
      return { name: clean(p.name), tts: p.tts || DEFAULT_TTS, decks: p.decks.map(parseDeckOf) };
    })
  };
}
