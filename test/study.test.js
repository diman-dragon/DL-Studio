import test from 'node:test';
import assert from 'node:assert/strict';
import installBridge from './helpers/installBridge.js';
import deckFile from './helpers/deckFile.js';
import importLibraryFromFile from './helpers/importLibraryFromFile.js';
import insertCards from '../www/js/repo/insertCards.js';
import insertDeck from '../www/js/repo/insertDeck.js';
import insertProject from '../www/js/repo/insertProject.js';
import listCardsInScope from '../www/js/repo/listCardsInScope.js';
import listDeckStats from '../www/js/repo/listDeckStats.js';
import listReviewsSince from '../www/js/repo/listReviewsSince.js';
import listProjects from '../www/js/repo/listProjects.js';
import listAllDecks from '../www/js/repo/listAllDecks.js';
import startSession from '../www/js/study/startSession.js';
import currentCard from '../www/js/study/currentCard.js';
import cyclePos from '../www/js/study/cyclePos.js';
import gradeCard from '../www/js/study/gradeCard.js';
import restartSession from '../www/js/study/restartSession.js';
import progressOf from '../www/js/study/progressOf.js';

const bridge = installBridge();
let projectId, deckId;

test.beforeEach(async () => {
  bridge.reset();
  bridge.io.picked = deckFile();
  await importLibraryFromFile();
  projectId = (await listProjects())[0].id;
  deckId = (await listAllDecks())[0].id;
});
test('область учёбы: вся библиотека / проект / колода; у карточек есть голос и подписи; счётчики по каждой колоде', async () => {
  const p2 = await insertProject('Даты', 'ru-RU');
  const d2 = await insertDeck(p2, 'XX век');
  await insertCards(d2, [{ front: '1917', back: 'революция' }, { front: '1945', back: 'победа' }]);
  assert.equal((await listCardsInScope('all')).length, 10);
  assert.equal((await listCardsInScope(`p:${projectId}`)).length, 8);
  assert.equal((await listCardsInScope(`d:${d2}`)).length, 2);
  const c = (await listCardsInScope(`d:${d2}`))[0];
  assert.deepEqual([c.tts, c.pname, c.dname, c.ll], ['ru-RU', 'Даты', 'XX век', 0]);
  const stats = await listDeckStats();
  assert.deepEqual(stats.map(d => [d.name, d.total, d.due, d.fresh, d.mature]), [['Basics', 8, 0, 8, 0], ['XX век', 2, 0, 2, 0]]);   // новые — не «к повторению»
});

test('листание по кругу в обе стороны', async () => {
  const s = await startSession('all', 'srs');
  assert.equal(s.order.length, 8);
  cyclePos(s, -1);                                     // перед первой — последняя
  assert.equal(s.pos, 7);
  cyclePos(s, 1);                                      // после последней — первая
  assert.equal(s.pos, 0);
  cyclePos(s, 1); cyclePos(s, 1);
  assert.equal(s.pos, 2);
});

test('интервальное повторение: оценка убирает карточку из сессии, «Снова» не возвращается (расписание нужно следующему плану), +10 минут в базе', async () => {
  const s = await startSession('all', 'srs');
  assert.deepEqual([s.order.length, s.total], [8, 8]);
  await gradeCard(s, 'good');
  assert.equal(s.order.length, 7);
  const c = currentCard(s), before = Date.now();
  await gradeCard(s, 'again');
  assert.equal(s.order.length, 6);
  assert.equal(s.order.includes(c.id), false);
  assert.ok(c.due >= before + 9.9 * 60000 && c.due <= Date.now() + 10.1 * 60000);
  assert.equal((await startSession('all', 'srs')).order.length, 6);   // good → завтра, again → через 10 минут: всё из базы
  while (s.order.length) await gradeCard(s, 'good');
  assert.equal(progressOf(s).text, '✓ План выполнен');
});

test('новые карточки: дневной лимит считается на каждую колоду отдельно, уже начатые сегодня учитываются', async () => {
  const p2 = await insertProject('Даты', 'ru-RU');
  const d2 = await insertDeck(p2, 'XX век');
  await insertCards(d2, [1, 2, 3, 4].map(n => ({ front: String(n), back: `б${n}` })));
  const per = (perDay, used) => startSession('all', 'srs', { perDay, used, max: 99 });
  const count = (s, id) => [...s.plan].filter(x => s.map.get(x).deck_id === id).length;   // в плане дня
  const a = await per(2, {});
  assert.deepEqual([count(a, deckId), count(a, d2)], [2, 2]);
  assert.equal(a.order.length, 4);                     // очередь = план, сверх него карточек нет
  const b = await per(2, { [deckId]: 1 });
  assert.deepEqual([count(b, deckId), count(b, d2)], [1, 2]);
  const c = await per(5, { [d2]: 5 });
  assert.deepEqual([count(c, deckId), count(c, d2)], [5, 0]);
});

test('restart: очередь и лимиты заново из базы', async () => {
  const s = await startSession('all', 'srs');
  await gradeCard(s, 'good'); await gradeCard(s, 'good');
  await restartSession(s);
  assert.deepEqual([s.order.length, s.total, s.pos, s.log.length], [6, 6, 0, 0]);
});

test('сессия — строго по плану дня: сверх плана карточек нет, план выполнен — сессия закончена', async () => {
  const s = await startSession('all', 'srs', { perDay: 2, used: {}, max: 99 });
  assert.deepEqual([s.planTotal, s.plan.size, s.order.length], [2, 2, 2]);
  assert.equal(progressOf(s).text, 'Осталось 2 из 2');
  await gradeCard(s, 'again');
  await gradeCard(s, 'good');
  assert.deepEqual([s.order.length, s.plan.size, currentCard(s)], [0, 0, null]);
  assert.equal((await listReviewsSince(0)).length, s.log.length);   // всё записано в базу
});

