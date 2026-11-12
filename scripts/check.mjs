// Проверка правил проекта (локально: `node scripts/check.mjs`, в CI — первым шагом).
//  1. Файл ≤ 350 строк. Файл — тематическая группа (несколько небольших связанных деклараций — нормально).
//  2. Каждая декларация используется (в main-коде; тесты не считаются; точки входа манифеста — исключение).
//  3. Нет неиспользуемых импортов.
//  4. Нет дублирования: одинаковые блоки из 6 значимых строк в разных файлах.
//  5. Версия: version.txt = X.Y.Z и совпадает с верхней записью CHANGELOG.md и README.md.
import { readdirSync, readFileSync, statSync } from 'node:fs';
import { join, basename, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

process.chdir(join(dirname(fileURLToPath(import.meta.url)), '..'));

const MAX_LINES = 350;
const DUP_WINDOW = 6;
const ENTRY_POINTS = new Set(['MainActivity', 'StudyBootReceiver']);
const bad = [];

const walk = d => readdirSync(d).flatMap(n => {
  const p = join(d, n).replace(/\\/g, '/');
  return statSync(p).isDirectory() ? walk(p) : [p];
});
const kt = ['app/src/main', 'app/src/test'].flatMap(walk).filter(f => f.endsWith('.kt'));
const read = f => readFileSync(f, 'utf8');
const src = Object.fromEntries(kt.map(f => [f, read(f)]));

const DECL = /^(?:(?:private|internal|public|inline|suspend|tailrec|operator|infix|sealed|data|enum|abstract|open|const|override)\s+)*(?:class|object|interface|fun|val|var|typealias)\s+(?:<[^>]+>\s*)?(?:[\w.<>?, ]+\.)?(\w+)/gm;
const declsOf = s => [...s.replace(/^import .*$/gm, '').matchAll(DECL)].map(m => m[1]);

for (const f of kt) {
  const s = src[f];
  const lines = s.trimEnd().split('\n').length;
  const limit = MAX_LINES;
  if (lines > limit) bad.push(`${f}: ${lines} строк (> ${limit})`);

  const body = s.replace(/^import .*$/gm, '');
  for (const m of s.matchAll(/^import ([\w.]+?)(?: as (\w+))?$/gm)) {
    const name = m[2] || m[1].split('.').pop();
    if (name === '*') continue;
    const delegated = (name === 'getValue' || name === 'setValue') && /\sby\s/.test(body);
    if (!delegated && !new RegExp(`\\b${name}\\b`).test(body)) bad.push(`${f}: неиспользуемый импорт ${m[1]}`);
  }
}

const main = kt.filter(f => f.includes('app/src/main'));
const all = main.map(f => src[f].replace(/^import .*$/gm, '')).join('\n');
for (const f of main) {
  for (const name of new Set(declsOf(src[f]))) {
    if (ENTRY_POINTS.has(name)) continue;
    const count = (all.match(new RegExp(`\\b${name}\\b`, 'g')) || []).length;
    if (count < 2) bad.push(`${f}: ${name} нигде не используется`);
  }
}

const seen = new Map();
for (const f of main) {
  const sig = src[f].split('\n').map(l => l.trim()).filter(l => l.length > 3 && !/^(import|package|[{}()\])]+,?)\b/.test(l) && !/^(import|package) /.test(l));
  for (let i = 0; i + DUP_WINDOW <= sig.length; i++) {
    const key = sig.slice(i, i + DUP_WINDOW).join('\n');
    if (seen.has(key) && seen.get(key) !== f) bad.push(`дубль: ${f} и ${seen.get(key)}:\n    ${sig.slice(i, i + DUP_WINDOW).join('\n    ')}`);
    else seen.set(key, f);
  }
}

const version = read('version.txt').trim();
if (!/^\d+\.\d+\.\d+$/.test(version)) bad.push(`version.txt: "${version}" — не X.Y.Z`);
const top = read('CHANGELOG.md').match(/^## (\S+)/m)?.[1];
if (top !== version) bad.push(`CHANGELOG.md: верхняя запись ${top}, version.txt ${version}`);
const readmeVersion = read('README.md').match(/Версия — в `version\.txt` \(сейчас (\S+)\)/)?.[1];
if (readmeVersion !== version) bad.push(`README.md: версия ${readmeVersion}, version.txt ${version}`);

if (bad.length) { console.error(`ПРОВЕРКА НЕ ПРОЙДЕНА (${bad.length}):\n` + bad.join('\n')); process.exit(1); }
console.log(`OK: ${kt.length} Kotlin-файлов, версия ${version}`);
