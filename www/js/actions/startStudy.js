import { state } from '../state/appState.js';
import openStudy from '../study/openStudy.js';
/** Запускает единственный режим — интервальное повторение. scope: all | p:<id> | d:<id>. */
export default function startStudy(d) {
  state.sheet = null;
  return openStudy('srs', d.scope || d.id);
}
