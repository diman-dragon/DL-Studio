// Собирает каталог тем: content/topics/*.json → content/course.json (размер и SHA-256 каждой темы).
// Запуск: `node scripts/build_content.mjs`. Папку content/ кладут на https-хостинг.
import { createHash } from 'node:crypto';
import { readdirSync, readFileSync, writeFileSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = join(dirname(fileURLToPath(import.meta.url)), '..', 'content');
const topics = readdirSync(join(root, 'topics')).filter(f => f.endsWith('.json')).sort().map(file => {
  const bytes = readFileSync(join(root, 'topics', file));
  const t = JSON.parse(bytes.toString('utf8'));
  if (!/^[a-z0-9_-]{1,64}$/.test(t.id) || `${t.id}.json` !== file) throw new Error(`${file}: id должен совпадать с именем файла`);
  for (const q of t.quiz ?? []) if (!q.options.includes(q.a)) throw new Error(`${file}: ответ «${q.a}» не входит в варианты`);
  return {
    id: t.id, course: t.course ?? '', group: t.group ?? '', n: t.n ?? 0, title: t.title, cards: (t.cards ?? []).length,
    pack: `topics/${file}`, size: bytes.length, sha256: createHash('sha256').update(bytes).digest('hex')
  };
});
writeFileSync(join(root, 'course.json'), JSON.stringify({ topics }, null, 2) + '\n');
console.log(`course.json: ${topics.length} тем`);
