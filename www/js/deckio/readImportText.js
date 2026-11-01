import parseImport from './parseImport.js';
import parseJsonCards from './parseJsonCards.js';
import parseTextRows from './parseTextRows.js';
import rowsToCards from './rowsToCards.js';
import formatOf from './formatOf.js';
import baseName from './baseName.js';
const cardsOf = (format, name, cards) => ({ kind: 'cards', format, name: baseName(name), cards });
/**
 * Текст → { kind: 'library', library } (наш JSON) или { kind: 'cards', format, name, cards }.
 */
export default function readImportText(name, text) {
  if (/^\s*[[{]/.test(text)) {
    try { return { kind: 'library', format: 'LingvaDesk (JSON)', library: parseImport(text) }; } catch { /* чужой JSON — ниже */ }
    try { const cards = parseJsonCards(JSON.parse(text)); if (cards.length) return cardsOf('JSON', name, cards); } catch { /* не JSON — таблица */ }
  }
  const cards = rowsToCards(parseTextRows(text));
  if (!cards.length) throw new Error('Не нашёл карточек: нужен список «слово — перевод» или таблица (CSV, TSV, Anki, Quizlet)');
  return cardsOf(formatOf(text), name, cards);
}
