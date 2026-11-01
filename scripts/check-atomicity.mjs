// Проверка архитектурных правил (запускается в CI):
//  JS:   ровно один export на файл (main.js — ни одного), ≤ 45 строк, все относительные импорты существуют,
//        именованный импорт соответствует именованному экспорту, default-импорт — default-экспорту.
//  Java: ≤ 60 строк на файл, один top-level тип на файл.
import { readdirSync, readFileSync, statSync, existsSync } from 'node:fs';
import { join, dirname, resolve } from 'node:path';

const walk = d => readdirSync(d).flatMap(n => { const p = join(d, n); return statSync(p).isDirectory() ? walk(p) : [p]; });
const bad = [];

const js = walk('www/js').filter(f => f.endsWith('.js'));
for (const p of js) {
  const s = readFileSync(p, 'utf8');
  const exports = (s.match(/^export /gm) || []).length;
  const want = p.endsWith('main.js') ? 0 : 1;
  if (exports !== want) bad.push(`${p}: export=${exports}, ожидается ${want}`);
  const lines = s.trimEnd().split('\n').length;
  if (lines > 45) bad.push(`${p}: ${lines} строк (>45)`);
  for (const m of s.matchAll(/^import\s+(.+?)\s+from\s+'(\.[^']+)'/gm)) {
    const target = resolve(dirname(p), m[2]);
    if (!existsSync(target)) { bad.push(`${p}: нет файла ${m[2]}`); continue; }
    const t = readFileSync(target, 'utf8');
    if (m[1].startsWith('{')) {
      for (const n of m[1].replace(/[{}]/g, '').split(',').map(x => x.trim()).filter(Boolean))
        if (!new RegExp(`^export (const|function|async function) ${n}\\b`, 'm').test(t)) bad.push(`${p}: ${m[2]} не экспортирует ${n}`);
    } else if (!/^export default /m.test(t)) bad.push(`${p}: ${m[2]} без default-экспорта`);
  }
}

// каждый модуль должен где-то импортироваться (main.js — точка входа)
const imported = new Set();
for (const p of js) for (const m of readFileSync(p, 'utf8').matchAll(/from '(\.[^']+)'/g)) imported.add(resolve(dirname(p), m[1]));
for (const p of js) if (!p.endsWith('main.js') && !imported.has(resolve(p))) bad.push(`${p}: не используется (никто не импортирует)`);

const java = walk('android/app/src/main/java').filter(f => f.endsWith('.java'));
for (const p of java) {
  const s = readFileSync(p, 'utf8');
  const lines = s.trimEnd().split('\n').length;
  if (lines > 60) bad.push(`${p}: ${lines} строк (>60)`);
  const types = (s.match(/^(public\s+)?(final\s+|abstract\s+)?(class|interface)\s/gm) || []).length;
  if (types !== 1) bad.push(`${p}: top-level типов ${types}, ожидается 1`);
}

if (bad.length) { console.error(bad.join('\n')); process.exit(1); }
console.log(`OK: ${js.length} JS, ${java.length} Java`);
