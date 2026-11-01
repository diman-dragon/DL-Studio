import test from 'node:test';
import assert from 'node:assert/strict';
import installBridge from './helpers/installBridge.js';
import deckFile from './helpers/deckFile.js';
import importLibraryFromFile from './helpers/importLibraryFromFile.js';
import saveLibrary from '../www/js/deckio/saveLibrary.js';
import parseImport from '../www/js/deckio/parseImport.js';
import insertReview from '../www/js/repo/insertReview.js';
import listAllDecks from '../www/js/repo/listAllDecks.js';
import inferGrade from '../www/js/srs/inferGrade.js';
import trackShow from '../www/js/study/trackShow.js';
import trackFlip from '../www/js/study/trackFlip.js';
import trackSignals from '../www/js/study/trackSignals.js';
import trackPause from '../www/js/study/trackPause.js';
import trackResume from '../www/js/study/trackResume.js';
import leaveCard from '../www/js/study/leaveCard.js';
import startSession from '../www/js/study/startSession.js';
import gradeCard from '../www/js/study/gradeCard.js';
import currentCard from '../www/js/study/currentCard.js';
import loadToday from '../www/js/study/loadToday.js';
import dayKey from '../www/js/util/dayKey.js';

const bridge = installBridge();
const realNow = Date.now;
let t = 1e12;
const tick = ms => { t += ms; };

test.beforeEach(async () => {
  bridge.reset();
  bridge.io.picked = deckFile();
  await importLibraryFromFile();
  t = 1e12;
  Date.now = () => t;
});
test.afterEach(() => { Date.now = realNow; });

const sig = o => ({ think: null, look: 0, dwell: 0, flips: 0, revisit: false, away: false, ...o });
const fresh = { reps: 0 }, known = { reps: 3 };

test('оценка по поведению: просто пролистал или отвлёкся — расписание не трогаем', () => {
  assert.equal(inferGrade(sig({ dwell: 800 }), fresh), null);
  assert.equal(inferGrade(sig({ think: 600, look: 400, dwell: 1000, flips: 1 }), fresh), null);
  assert.equal(inferGrade(sig({ think: 2000, look: 2000, dwell: 4000, flips: 1, away: true }), fresh), null);
});

test('оценка по поведению: без переворота — знает; зрелая и быстро — легко; долго мялся — трудно', () => {
  assert.equal(inferGrade(sig({ dwell: 2500 }), fresh), 'good');
  assert.equal(inferGrade(sig({ dwell: 2500 }), known), 'easy');
  assert.equal(inferGrade(sig({ dwell: 6000 }), known), 'good');
  assert.equal(inferGrade(sig({ dwell: 10000 }), known), 'hard');
});

test('оценка по поведению: вспоминал, читал ответ, метался, возвращался', () => {
  const g = o => inferGrade(sig({ flips: 1, ...o }), fresh);
  assert.equal(g({ think: 2000, look: 2000, dwell: 4000 }), 'easy');             // вспомнил быстро, ответ глянул
  assert.equal(g({ think: 1000, look: 1000, dwell: 2000 }), 'good');             // перевернул рефлекторно — не «легко»
  assert.equal(g({ think: 3500, look: 2000, dwell: 5500 }), 'good');
  assert.equal(g({ think: 5000, look: 2000, dwell: 7000 }), 'good');             // чуть подумал
  assert.equal(g({ think: 5000, look: 6000, dwell: 11000 }), 'hard');            // думал и долго читал
  assert.equal(g({ think: 10000, look: 2000, dwell: 12000 }), 'hard');
  assert.equal(g({ think: 10000, look: 6000, dwell: 16000 }), 'again');          // мучился и учил ответ
  assert.equal(g({ think: 2000, look: 2000, dwell: 4000, flips: 4 }), 'good');   // метался туда-обратно
  assert.equal(g({ think: 2000, look: 2000, dwell: 4000, flips: 4, revisit: true }), 'hard');
  assert.equal(g({ think: 1000, look: 4000, dwell: 5000, revisit: true }), 'hard');   // не вспоминал, читал, вернулся
});

test('трекер: время до переворота и на ответе, переворот туда-обратно, пауза, «отвлёкся»', async () => {
  const s = await startSession('all', 'srs');
  trackShow(s); tick(2500); trackFlip(); tick(3500);
  let x = trackSignals();
  assert.deepEqual([x.think, x.look, x.dwell, x.flips, x.revisit, x.away], [2500, 3500, 6000, 1, false, false]);
  trackShow(s, true); tick(1000); trackFlip(); tick(1000); trackFlip(); tick(1000); trackFlip();
  x = trackSignals();
  assert.deepEqual([x.think, x.look, x.dwell, x.flips, x.revisit], [1000, 1000, 3000, 3, true]);
  trackShow(s); tick(1000); trackPause(); tick(60000); trackResume(); tick(1000);   // свернули на минуту
  x = trackSignals();
  assert.deepEqual([x.dwell, x.away], [2000, false]);
  trackShow(s); tick(50000);                                                     // положил телефон
  assert.equal(trackSignals().away, true);
});

test('уход с карточки: вперёд — оценка по поведению, назад и мелькнувшая — без оценки', async () => {
  const s = await startSession('all', 'srs');
  trackShow(s);
  const a = currentCard(s);
  tick(2000); trackFlip(); tick(2000);
  await leaveCard(s, 1);                                // вспомнил за 2 с, ответ глянул 2 с → легко
  assert.equal(s.order.length, 7);
  assert.deepEqual([a.interval, a.reps], [4, 1]);
  trackShow(s);
  const b = currentCard(s);
  tick(2000); trackFlip(); tick(2000);
  await leaveCard(s, -1);                               // назад — не оцениваем
  assert.deepEqual([s.order.length, b.reps, b.due], [7, 0, 0]);
  trackShow(s); tick(300);
  await leaveCard(s, 1);                                // мелькнула — не оцениваем
  assert.equal(s.order.length, 7);
  assert.equal((await loadToday(t)).n, 1);              // в «сегодня» попала одна оценка — читаем из истории
});

test('серии дней и сегодня считаются из истории (ничего не хранится отдельно)', async () => {
  const at = (m, d, h = 12) => new Date(2026, m - 1, d, h).getTime();
  assert.equal(dayKey(0, at(3, 5)), '2026-03-05');
  assert.equal(dayKey(-1, at(3, 1)), '2026-02-28');
  const card = currentCard(await startSession('all', 'srs'));
  assert.deepEqual(await loadToday(at(3, 5)), { n: 0, current: 0, best: 0 });
  for (const ts of [at(3, 1), at(3, 2), at(3, 3), at(3, 5), at(3, 5, 18), at(3, 6)]) await insertReview(card.id, 'good', {}, 0, 'srs', ts);
  let x = await loadToday(at(3, 6, 20));
  assert.deepEqual([x.n, x.current, x.best], [1, 2, 3]);   // 5–6 марта подряд, лучшая серия — 1–3 марта
  x = await loadToday(at(3, 7));                           // вчера учился — серия жива
  assert.deepEqual([x.n, x.current], [0, 2]);
  x = await loadToday(at(3, 9));                           // пропуск — серия прервалась
  assert.deepEqual([x.n, x.current, x.best], [0, 0, 3]);
});

test('оценка карточки попадает в «сегодня»', async () => {
  const s = await startSession('all', 'srs');
  await gradeCard(s, 'good'); await gradeCard(s, 'again');
  assert.equal((await loadToday(t)).n, 2);
});

test('импорт: null и не-объект — понятная ошибка, повторы внутри файла не дублируются', async () => {
  assert.throws(() => parseImport('null'), /не похож/);
  assert.throws(() => parseImport('42'), /не похож/);
  const r = await saveLibrary({ projects: [{ name: 'X', tts: 'en-US', decks: [{ name: 'D', cards: [{ front: 'a', back: 'b' }, { front: 'a', back: 'b' }, { front: 'c', back: 'd' }] }] }] });
  assert.equal(r.added, 2);
});
