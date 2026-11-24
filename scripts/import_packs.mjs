// Переводит большие плоские наборы (курс → урок → карточки name/back/…) в темы приложения: content/topics/<id>.json.
// Запуск: `node scripts/import_packs.mjs <папка с наборами> <папка для тем>`.
// Урок набора режется на темы по ~28 карточек; у темы есть карточки и практика (из тех же карточек), а у первой темы группы ещё и теория.
import { readdirSync, readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { join } from 'node:path';
import { THEORY, phrasesTheory } from './pack_theory.mjs';

const CHUNK = 28;
const QUIZ = 8;
const OPTIONS = 4;
const PREFIX = { 'Идиомы': 'idm', 'Фразовые глаголы': 'phv', 'Фразы по ситуациям': 'phr', 'Мир вокруг': 'wld', 'Правописание и формы': 'spl' };
const [src, out] = process.argv.slice(2);
if (!src || !out) throw new Error('usage: node scripts/import_packs.mjs <папка с наборами> <папка для тем>');

// Детерминированный генератор: одни и те же данные всегда дают одни и те же файлы.
const rng = seed => { let s = [...seed].reduce((a, c) => (a * 31 + c.charCodeAt(0)) >>> 0, 7); return () => (s = (s * 1664525 + 1013904223) >>> 0) / 2 ** 32; };
const shuffle = (a, r) => { const b = [...a]; for (let i = b.length - 1; i > 0; i--) { const j = Math.floor(r() * (i + 1)); [b[i], b[j]] = [b[j], b[i]]; } return b; };

const ipa = s => s.split(/\s*,\s*(?=\[)/).map(p => p.replace(/^\[(.*)\]$/, '/$1/')).join(', ');
const MARK = /(?<![\w])([\\/])(?=\w)/g;
const arrows = s => s.replace(MARK, (_, m) => (m === '\\' ? '↘' : '↗'));

function card(it) {
  const front = it.name.replace(MARK, '').replace(/\s+/g, ' ').trim();
  const c = { front, back: it.back.trim() };
  if (it.ipa) c.ipa = ipa(it.ipa);
  const marked = arrows(it.name).replace(/\s+/g, ' ').trim();
  const example = (it.example || '').trim() || (marked !== front ? marked : '');
  if (example) c.example = example;
  const note = [(it.explanation || '').trim(), (it.hint || '').trim()].filter(Boolean).join('\n');
  if (note) c.note = note;
  return c;
}

// «Группа · подназвание»; хвост вида «1/5» — просто номер части урока.
function names(name) {
  const [head, tail] = name.split(' · ');
  return { group: head, label: tail && !/^\d+\/\d+$/.test(tail) ? tail : '' };
}

function quiz(chunk, pool, r) {
  const picked = shuffle(chunk, r).slice(0, QUIZ);
  return picked.map(c => {
    const near = pool.filter(o => o.back !== c.back).sort((a, b) => Math.abs(a.back.length - c.back.length) - Math.abs(b.back.length - c.back.length));
    const wrong = [];
    for (const o of shuffle(near.slice(0, 14), r)) if (!wrong.includes(o.back) && wrong.length < OPTIONS - 1) wrong.push(o.back);
    return { q: `Что означает «${c.front}»?`, a: c.back, options: shuffle([c.back, ...wrong], r) };
  }).filter(q => q.options.length === OPTIONS);
}

let total = 0;
mkdirSync(out, { recursive: true });
for (const file of readdirSync(src).filter(f => f.endsWith('.json')).sort()) {
  const pack = JSON.parse(readFileSync(join(src, file), 'utf8'));
  const pre = PREFIX[pack.name];
  if (!pre) throw new Error(`${file}: неизвестный курс «${pack.name}»`);
  const lessons = pack.items.map(l => ({ ...names(l.name), cards: l.items.map(card) }));
  const lessonsOf = g => lessons.filter(l => l.group === g);
  const seenGroup = new Set();
  const counter = {};
  let n = 0;
  lessons.forEach((l, li) => {
    const siblings = lessonsOf(l.group);
    const running = siblings.length > 1 && new Set(siblings.map(s => s.label)).size < siblings.length;
    const parts = Math.max(1, Math.round(l.cards.length / CHUNK));
    const pool = l.cards.length >= 12 ? l.cards : siblings.flatMap(s => s.cards);
    for (let p = 0; p < parts; p++) {
      const chunk = l.cards.slice(Math.floor(p * l.cards.length / parts), Math.floor((p + 1) * l.cards.length / parts));
      const id = `${pre}-${String(li + 1).padStart(2, '0')}-${String(p + 1).padStart(2, '0')}`;
      const base = l.label || l.group;
      const title = running ? `${base} · ${counter[l.group] = (counter[l.group] ?? 0) + 1}` : parts > 1 ? `${base} · ${p + 1}/${parts}` : base;
      const topic = { id, course: pack.name, group: l.group, n: ++n, title, tag: 'en-US' };
      if (!seenGroup.has(l.group)) {
        seenGroup.add(l.group);
        topic.theory = THEORY[l.group] ?? (pre === 'phr' ? phrasesTheory(l.group) : null);
        if (!topic.theory) throw new Error(`нет теории для группы «${l.group}»`);
      }
      topic.cards = chunk;
      topic.quiz = quiz(chunk, pool, rng(id));
      writeFileSync(join(out, `${id}.json`), JSON.stringify(topic, null, 2) + '\n');
      total++;
    }
  });
  console.log(`${pack.name}: ${lessons.length} уроков, ${n} тем`);
}
console.log(`всего ${total} тем`);
