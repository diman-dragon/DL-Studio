import { FIT_MIN } from '../constants/FIT_MIN.js';
import wordBroken from './wordBroken.js';
const TEXTS = '.gt, .tn, .big, .t small, .gtile small, .item .t, .opt, .mark, .tv, .tt, .sheet h3, header h1, .sub, .counter span, .muted';
/**
 * Слова не рвутся по буквам: если слово не помещается в строку, шрифт этого элемента уменьшается шагами по 5 % (не мельче FIT_MIN).
 * Остаётся перенос только у слов, которые не поместились бы никак (длинные строки без пробелов и дефисов).
 */
export default function fitWords() {
  for (const el of document.querySelectorAll?.(TEXTS) ?? []) {
    el.style.removeProperty('font-size');
    const base = parseFloat(getComputedStyle(el).fontSize);
    for (let k = 1; wordBroken(el) && k > FIT_MIN; ) {
      k = Math.round((k - 0.05) * 100) / 100;
      el.style.fontSize = `${base * k}px`;
    }
  }
}
