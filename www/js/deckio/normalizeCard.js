import clean from '../util/clean.js';
export default function normalizeCard(c) {
  return { front: clean(c.front), back: clean(c.back), example: c.example ?? null, exampleTr: c.example_translation ?? null };
}
