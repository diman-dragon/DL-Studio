import test from 'node:test';
import assert from 'node:assert/strict';
import installBridge from './helpers/installBridge.js';
import deckFile from './helpers/deckFile.js';
import importLibraryFromFile from './helpers/importLibraryFromFile.js';
import { state } from '../www/js/state/appState.js';
import { BEHAVIOR } from '../www/js/constants/BEHAVIOR.js';
import startSession from '../www/js/study/startSession.js';
import gradeCard from '../www/js/study/gradeCard.js';
import undoGrade from '../www/js/study/undoGrade.js';
import leaveCard from '../www/js/study/leaveCard.js';
import trackShow from '../www/js/study/trackShow.js';
import trackFlip from '../www/js/study/trackFlip.js';
import currentCard from '../www/js/study/currentCard.js';
import makeQuiz from '../www/js/study/makeQuiz.js';
import cardDir from '../www/js/study/cardDir.js';
import loadToday from '../www/js/study/loadToday.js';
import dayBars from '../www/js/study/dayBars.js';
import retention from '../www/js/study/retention.js';
import updateCardSrs from '../www/js/repo/updateCardSrs.js';
import insertReview from '../www/js/repo/insertReview.js';
import listRecentReviews from '../www/js/repo/listRecentReviews.js';
import countNewByDeck from '../www/js/repo/countNewByDeck.js';
import listAllDecks from '../www/js/repo/listAllDecks.js';
import listDeckStats from '../www/js/repo/listDeckStats.js';
import listDeckReviewStats from '../www/js/repo/listDeckReviewStats.js';
import listHardCards from '../www/js/repo/listHardCards.js';
import listDueSoon from '../www/js/repo/listDueSoon.js';
import forecastBars from '../www/js/study/forecastBars.js';
import gradeSplit from '../www/js/study/gradeSplit.js';
import medianTimes from '../www/js/study/medianTimes.js';
import listReviewsSince from '../www/js/repo/listReviewsSince.js';
import resetProgress from '../www/js/repo/resetProgress.js';
import inferGrade from '../www/js/srs/inferGrade.js';
import calibrate from '../www/js/srs/calibrate.js';
import sameAnswer from '../www/js/util/sameAnswer.js';
import dayStart from '../www/js/util/dayStart.js';

const bridge = installBridge();
const newCount = async () => Object.values(await countNewByDeck(dayStart(0, t))).reduce((a, b) => a + b, 0);
const realNow = Date.now;
let t = 1e12;
test.beforeEach(async () => {
  bridge.reset();
  bridge.io.picked = deckFile();
  await importLibraryFromFile();
  t = 1e12;
  Date.now = () => t;
});
test.afterEach(() => { Date.now = realNow; state.prefs.dir = 'fwd'; });

test('отмена оценки возвращает расписание, историю, счётчик дня, очередь и итог сессии', async () => {
  const s = await startSession('all', 'srs');
  const card = currentCard(s), before = { due: card.due, interval: card.interval, ease: card.ease, reps: card.reps };
  trackShow(s); t += 2000; trackFlip(); t += 2000;
  await leaveCard(s, 1);
  assert.equal(s.order.length, 7);
  assert.equal(s.log.length, 1);
  assert.equal(await newCount(), 1);
  await undoGrade(s);
  assert.deepEqual({ due: card.due, interval: card.interval, ease: card.ease, reps: card.reps }, before);
  assert.deepEqual([s.order.length, s.log.length, s.undo, currentCard(s).id], [8, 0, null, card.id]);
  assert.equal(await newCount(), 0);
  assert.equal((await loadToday(t)).n, 0);
  assert.equal((await listRecentReviews(10)).length, 0);
});

test('интервальное повторение: просроченные первыми, новые — по дневному лимиту, всё — не больше размера сессии', async () => {
  const all = await startSession('all', 'srs');
  const dueId = all.order[4];
  await updateCardSrs(dueId, { due: 1000, interval: 1, ease: 2.5, reps: 1 });
  const lim = (perDay, max) => startSession('all', 'srs', { perDay, used: {}, max });
  const a = await lim(2, 99);
  assert.deepEqual([a.plan.size, a.order.length, a.order[0]], [3, 8, dueId]);   // план: просроченная + 2 новые; остальные — после плана
  assert.equal((await lim(0, 99)).plan.size, 1);                  // лимит новых исчерпан — в плане только просроченные
  assert.equal((await lim(5, 2)).plan.size, 2);                   // размер сессии
});

test('история: оценка пишет время; новые карточки считаются за день по колодам', async () => {
  const s = await startSession('all', 'srs');
  await gradeCard(s, 'good', { think: 2000, look: 1500 });
  assert.deepEqual((await listRecentReviews(10)).map(r => [r.think, r.look]), [[2000, 1500]]);
  assert.equal(await newCount(), 1);
});

test('«пиявка»: пятое «снова» откладывает карточку и убирает из очереди', async () => {
  const s = await startSession('all', 'srs');
  const card = currentCard(s);
  for (let i = 0; i < BEHAVIOR.leech - 1; i++) await insertReview(card.id, 'again');
  await gradeCard(s, 'again');
  assert.ok(card.due >= t + (BEHAVIOR.leechDays - 0.1) * 86400000);
  assert.ok(!s.order.includes(card.id));
});

test('калибровка: медленному читателю пороги длиннее, быстрому — короче, мало данных — общие', () => {
  const rows = n => Array.from({ length: n }, () => ({ think: 6000, look: 6000 }));
  assert.equal(calibrate(rows(10)), BEHAVIOR);
  const slow = calibrate(rows(40));
  assert.deepEqual(slow.think, [8000, 18000]);
  assert.equal(slow.readAnswer, 6000);
  const fast = calibrate(Array.from({ length: 40 }, () => ({ think: 1000, look: 1000 })));
  assert.equal(fast.think[0], 2400);
});

test('оценка по ответу: верно — по времени, подсказка — трудно, неверно — снова, без ответа — нет', () => {
  const g = o => inferGrade({ think: 2000, look: 0, dwell: 3000, flips: 0, revisit: false, away: false, kind: 'visual-choice', correct: true, hinted: false, ...o }, { reps: 0 });
  assert.equal(g({}), 'easy');
  assert.equal(g({ think: 5000 }), 'good');
  assert.equal(g({ think: 10000 }), 'hard');
  assert.equal(g({ hinted: true }), 'hard');
  assert.equal(g({ correct: false }), 'again');
  assert.equal(g({ correct: null }), null);
  assert.equal(g({ kind: 'visual-type', think: 8000 }), 'easy');  // ввод занимает дольше
  assert.equal(g({ think: 300, dwell: 400 }), 'easy');            // быстрый верный ответ не отбрасывается как «мелькнуло»
});

test('ввод ответа: регистр, пунктуация, ё/е, диакритика, варианты через запятую', () => {
  assert.ok(sameAnswer('  ЯБЛОКО. ', 'яблоко'));
  assert.ok(sameAnswer('елка', 'ёлка'));
  assert.ok(sameAnswer('cafe', 'café'));
  assert.ok(sameAnswer('плод', 'яблоко, плод / фрукт'));
  assert.ok(!sameAnswer('груша', 'яблоко'));
  assert.ok(!sameAnswer('', 'яблоко'));
  assert.ok(!sameAnswer('й', 'и'));                               // кириллическая «й» не сводится к «и»
});

test('вопросы: выбор — 4 разных варианта с ответом; ввод; переворот — без вопроса', async () => {
  const s = await startSession('all', 'srs');
  const card = currentCard(s);
  const c = makeQuiz(s, card, 'fwd', 'choice');
  assert.equal(c.kind.endsWith('-choice'), true);
  assert.equal(c.options.length, 4);
  assert.equal(new Set(c.options).size, 4);
  assert.ok(c.options.includes(card.back));
  assert.equal(makeQuiz(s, card, 'rev', 'choice').answer, card.front);
  assert.equal(makeQuiz(s, card, 'fwd', 'type').kind.endsWith('-type'), true);
  assert.equal(makeQuiz(s, card, 'fwd', 'flip'), null);
});

test('задания: аудио, пропуск в фразе и произношение имеют реальные ответы', async () => {
  const card = { id: 1, front: 'make', back: 'делать', cloze: 'I ___ a decision.', cloze_answer: 'made', cloze_tr: 'Я принял решение.' };
  const other = [2, 3, 4].map((id, i) => ({ id, front: `word${id}`, back: `слово${id}`, cloze: `We ___ ${id}.`, cloze_answer: `do${i}` }));
  const s = { map: new Map([card, ...other].map(c => [c.id, c])) };
  assert.equal(makeQuiz(s, card, 'fwd', 'audio-choice').answer, 'делать');
  assert.equal(makeQuiz(s, card, 'fwd', 'audio-type').kind, 'audio-type');
  assert.equal(makeQuiz(s, card, 'fwd', 'cloze-choice').answer, 'made');
  assert.equal(makeQuiz(s, card, 'fwd', 'cloze-type').prompt, 'I ___ a decision.');
  assert.equal(makeQuiz(s, card, 'fwd', 'speak').answer, 'I made a decision.');
});

test('статистика: столбики по дням и удержание', () => {
  const now = new Date(2026, 2, 10, 12).getTime(), day = 86400000;
  const rows = [{ ts: now, grade: 'good', fresh: 0, mode: 'srs' }, { ts: now - 1000, grade: 'again', fresh: 0, mode: 'srs' }, { ts: now - day, grade: 'easy', fresh: 1, mode: 'srs' }, { ts: now - 40 * day, grade: 'good', fresh: 0, mode: 'srs' }];
  const bars = dayBars(rows, 14, now);
  assert.equal(bars.length, 14);
  assert.deepEqual([bars.at(-1).n, bars.at(-2).n, bars.at(-1).today, bars.at(-1).label], [2, 1, true, '10']);
  assert.equal(bars.reduce((a, b) => a + b.n, 0), 3);             // старое за окном не считается
  assert.equal(retention(rows), 67);                              // изученных (интервальных) 3: без «снова» — 2
  assert.equal(retention([{ ts: now, grade: 'good', fresh: 1, mode: 'srs' }]), null);
});

test('статистика из базы: по колодам, трудные карточки, прогноз, сброс очищает историю', async () => {
  const s = await startSession('all', 'srs');
  const [a, b] = [s.order[0], s.order[1]];
  for (let i = 0; i < 3; i++) await insertReview(a, 'again', {}, 0, 'srs', t);
  await insertReview(a, 'good', {}, 0, 'srs', t);
  await insertReview(b, 'good', { think: 2000, look: 1000 }, 1, 'srs', t);
  const deck = (await listAllDecks())[0].id;
  assert.deepEqual((await listDeckReviewStats(0)).map(r => [r.id, Number(r.n), Number(r.ok)]), [[deck, 5, 2]]);
  assert.deepEqual((await listHardCards(5)).map(c => [Number(c.n), c.dname]), [[3, 'Basics']]);
  await updateCardSrs(b, { due: t + 2 * 86400000, interval: 2, ease: 2.5, reps: 1 });
  assert.equal((await listDueSoon(t + 5 * 86400000)).length, 1);
  const stats = (await listDeckStats())[0];
  assert.deepEqual([Number(stats.total), Number(stats.fresh), Number(stats.late)], [8, 7, 0]);   // срок впереди — не просрочено
  await updateCardSrs(b, { due: Date.now() - 2 * 86400000, interval: 2, ease: 2.5, reps: 1 });
  assert.deepEqual([Number((await listDeckStats())[0].late), Number((await listDeckStats())[0].due)], [1, 1]);   // срок был позавчера — просрочено
  await updateCardSrs(b, { due: t + 2 * 86400000, interval: 2, ease: 2.5, reps: 1 });
  await resetProgress();
  assert.equal((await listReviewsSince(0)).length, 0);
  assert.equal(Number((await listDeckStats())[0].fresh), 8);
});

test('прогноз, оценки и время для статистики', () => {
  const now = new Date(2026, 2, 10, 12).getTime(), day = 86400000;
  const bars = forecastBars([{ due: now - 3 * day }, { due: now + 1000 }, { due: now + day }, { due: now + 20 * day }], 7, now);
  assert.deepEqual([bars.length, bars[0].n, bars[1].n, bars[0].today, bars[0].label], [7, 2, 1, true, '10']);   // просроченные — в сегодня
  assert.deepEqual(gradeSplit([{ grade: 'again' }, { grade: 'good' }, { grade: 'good' }, { grade: 'x' }]), { again: 1, hard: 0, good: 2, easy: 0 });
  assert.deepEqual(medianTimes([{ think: 2000, look: 1000 }, { think: 4000, look: null }, { think: 3000, look: 3000 }]), { think: 3, look: 2 });
  assert.deepEqual(medianTimes([]), { think: null, look: null });
});
