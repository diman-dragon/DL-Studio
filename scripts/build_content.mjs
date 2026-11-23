// Собирает каталог: <папка>/topics/*.json (+ <папка>/media/<id темы>/…) → <папка>/course.json.
// Запуск: `node scripts/build_content.mjs <папка>`. Папку кладут на https-хостинг, её адрес вводят в настройках приложения.
// Формат и устройство каталога — docs/CATALOG.md.
import { createHash } from 'node:crypto';
import { existsSync, readdirSync, readFileSync, statSync, writeFileSync } from 'node:fs';
import { join, relative } from 'node:path';

const root = process.argv[2];
if (!root) throw new Error('usage: node scripts/build_content.mjs <папка с topics/>');
const sha = bytes => createHash('sha256').update(bytes).digest('hex');
const EXT = /\.(mp3|m4a|aac|ogg|oga|wav|mp4|webm|3gp|jpg|jpeg|png|webp|gif)$/i;
const MEDIA_REF = /^@(image|audio|video) (\S+)/;

/** Все файлы папки рекурсивно, пути относительно неё. */
const walk = (dir, base = dir) => !existsSync(dir) ? [] : readdirSync(dir).flatMap(name => {
  const full = join(dir, name);
  return statSync(full).isDirectory() ? walk(full, base) : [relative(base, full).split('\\').join('/')];
});

const topics = readdirSync(join(root, 'topics')).filter(f => f.endsWith('.json')).sort().map(file => {
  const bytes = readFileSync(join(root, 'topics', file));
  const t = JSON.parse(bytes.toString('utf8'));
  if (!/^[a-z0-9_-]{1,64}$/.test(t.id) || `${t.id}.json` !== file) throw new Error(`${file}: id должен совпадать с именем файла`);
  for (const q of t.quiz ?? []) if ((q.t ?? 'choice') === 'choice' && !q.options.includes(q.a)) throw new Error(`${file}: ответ «${q.a}» не входит в варианты`);
  // Медиа темы: всё, что лежит в media/<id>/, и каждая ссылка из теории должна указывать на существующий файл (или на https).
  const mediaDir = join(root, 'media', t.id);
  const files = walk(mediaDir).filter(p => EXT.test(p));
  for (const page of t.theory ?? []) for (const p of page.p ?? []) {
    const m = MEDIA_REF.exec(p.trim());
    if (m && !m[2].startsWith('https://') && !files.includes(m[2])) throw new Error(`${file}: нет файла media/${t.id}/${m[2]}`);
  }
  const media = files.sort().map(path => {
    const data = readFileSync(join(mediaDir, path));
    return { path, size: data.length, sha256: sha(data) };
  });
  return {
    id: t.id, course: t.course ?? '', group: t.group ?? '', n: t.n ?? 0, title: t.title, cards: (t.cards ?? []).length,
    pack: `topics/${file}`, size: bytes.length, sha256: sha(bytes), ...(media.length ? { media } : {})
  };
});

// Дерево курс → урок → темы для справки и будущих экранов (приложение пока строит его само по полям course и group).
const courses = [];
for (const t of [...topics].sort((a, b) => a.course.localeCompare(b.course) || a.n - b.n)) {
  let c = courses.find(x => x.title === t.course);
  if (!c) courses.push(c = { title: t.course, lessons: [] });
  let l = c.lessons.find(x => x.title === t.group);
  if (!l) c.lessons.push(l = { title: t.group, topics: [] });
  l.topics.push(t.id);
}
writeFileSync(join(root, 'course.json'), JSON.stringify({ version: 2, courses, topics }, null, 2) + '\n');
console.log(`course.json: ${courses.length} курсов, ${topics.length} тем, ${topics.reduce((n, t) => n + (t.media?.length ?? 0), 0)} медиафайлов`);
