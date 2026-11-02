import { PREF_OPTIONS } from '../constants/PREF_OPTIONS.js';
import systemLanguage from '../util/systemLanguage.js';
/** Подпись значения настройки; у «Системного» языка — какой это язык. */
export default function prefLabel(key, value) {
  const label = PREF_OPTIONS[key].find(([v]) => v === String(value))?.[1] ?? String(value);
  return key === 'nativeLang' && value === 'system' ? `${label} (${systemLanguage()})` : label;
}
