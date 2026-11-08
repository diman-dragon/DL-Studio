// Проверка правил проекта (локально: `node scripts/check.mjs`, в CI — первым шагом).
//  1. Один файл — одна декларация верхнего уровня; имя файла = имя декларации (с заглавной буквы).
//  2. Файл ≤ 60 строк (исключения — в LONG_OK).
//  3. Каждая декларация используется (в main-коде; тесты не считаются; точки входа манифеста — исключение).
//  4. Нет неиспользуемых импортов.
//  5. Нет дублирования: одинаковые блоки из 4 значимых строк в разных местах.
//  6. Версия: version.txt = X.Y.Z и совпадает с верхней записью CHANGELOG.md.
import { readdirSync, readFileSync, statSync } from 'node:fs';
import { join, basename, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

process.chdir(join(dirname(fileURLToPath(import.meta.url)), '..'));

const MAX_LINES = 60;
const DUP_WINDOW = 4;
const ENTRY_POINTS = new Set(['MainActivity', 'StudyBootReceiver']);
// Осознанные исключения из лимита строк: таблицы переводов, две модели с общим замком и тесты логики.
const LONG_OK = { Strings: 100, StringsRu: 130, StringsEn: 130, LibraryViewModel: 160, AppViewModel: 95, StudyViewModel: 70, VoiceName: 70, StudyLogicTest: 200, ModeWeightsTest: 100 };
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
  const limit = LONG_OK[basename(f, '.kt')] ?? MAX_LINES;
  if (lines > limit) bad.push(`${f}: ${lines} строк (> ${limit})`);
  const decls = declsOf(s);
  if (decls.length !== 1) bad.push(`${f}: деклараций верхнего уровня ${decls.length}, нужна 1 (${decls.join(', ')})`);
  else if (basename(f, '.kt') !== decls[0][0].toUpperCase() + decls[0].slice(1)) bad.push(`${f}: имя файла не совпадает с декларацией ${decls[0]}`);

  const body = s.replace(/^import .*$/gm, '');
  for (const m of s.matchAll(/^import ([\w.]+?)(?: as (\w+))?$/gm)) {
    const name = m[2] || m[1].split('.').pop();
    if (name === '*') continue;
    const delegated = (name === 'getValue' || name === 'setValue') && /\sby\s/.test(body);
    if (!delegated && !new RegExp(`\\b${name}\\b`).test(body)) bad.push(`${f}: неиспользуемый импорт ${m[1]}`);
  }
}

const main = kt.filter(f => f.includes('app/src/main'));
for (const f of main) {
  for (const name of declsOf(src[f])) {
    if (ENTRY_POINTS.has(name)) continue;
    const used = main.some(o => o !== f && new RegExp(`\\b${name}\\b`).test(src[o].replace(/^import .*$/gm, '')));
    if (!used) bad.push(`${f}: ${name} нигде не используется`);
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

if (bad.length) { console.error(`ПРОВЕРКА НЕ ПРОЙДЕНА (${bad.length}):\n` + bad.join('\n')); process.exit(1); }
console.log(`OK: ${kt.length} Kotlin-файлов, версия ${version}`);
