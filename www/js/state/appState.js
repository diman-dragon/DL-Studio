import { DEFAULT_PREFS } from '../constants/DEFAULT_PREFS.js';
/**
 * nav — текущий экран; stack — пройденные экраны («Назад» снимает верхний); session — SRS-сессия;
 * speechChecked — языки, для которых уже проверен офлайн-пакет распознавания; iconLevel — уровень значка приложения (0–3, null — ещё не знаем); importDraft — разобранный файл; sheet — временное окно статистики; quiz — текущее задание; speech — последняя попытка произношения
 * { id, heard, score, attempts }; listening — микрофон слушает; awake — экран удерживается включённым;
 * anim — анимация ближайшей отрисовки; page/landing/pager — постраничный вывод.
 */
export const state = {
  nav: { v: 'study' }, stack: [], session: null, quiz: null, speechChecked: {}, iconLevel: null, importDraft: null, sheet: null, speech: null, listening: false, revealed: false, awake: false, anim: '',
  page: 0, landing: null, pager: { items: [], titles: [], fixed: false },
  prefs: { ...DEFAULT_PREFS }
};
