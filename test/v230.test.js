import test, { mock } from 'node:test';
import assert from 'node:assert/strict';
import installBridge from './helpers/installBridge.js';
import insertCards from '../www/js/repo/insertCards.js';
import insertDeck from '../www/js/repo/insertDeck.js';
import insertProject from '../www/js/repo/insertProject.js';
import startSession from '../www/js/study/startSession.js';
import currentCard from '../www/js/study/currentCard.js';
import gradeCard from '../www/js/study/gradeCard.js';
import makeQuiz from '../www/js/study/makeQuiz.js';
import mistakesOf from '../www/js/study/mistakesOf.js';
import retrySession from '../www/js/study/retrySession.js';
import orderFresh from '../www/js/study/orderFresh.js';
import listReviewsSince from '../www/js/repo/listReviewsSince.js';
import quizPick from '../www/js/study/quizPick.js';
import taskFor from '../www/js/study/taskFor.js';
import cardDir from '../www/js/study/cardDir.js';
import loadLimits from '../www/js/study/loadLimits.js';
import textLater from '../www/js/study/textLater.js';
import refreshCard from '../www/js/study/refreshCard.js';
import saveDeckPref from '../www/js/settings/saveDeckPref.js';
import loadDeckPrefs from '../www/js/settings/loadDeckPrefs.js';
import setSetting from '../www/js/settings/setSetting.js';
import nextSrs from '../www/js/srs/nextSrs.js';
import nextOption from '../www/js/util/nextOption.js';
import speakSlow from '../www/js/tts/speakSlow.js';
import speakText from '../www/js/tts/speakText.js';
import cyclePref from '../www/js/actions/cyclePref.js';
import cycleDeckPref from '../www/js/actions/cycleDeckPref.js';
import studyFace from '../www/js/components/studyFace.js';
import settingTiles from '../www/js/components/settingTiles.js';
import deckStudyMeta from '../www/js/components/deckStudyMeta.js';
import { state } from '../www/js/state/appState.js';
import { track } from '../www/js/state/trackState.js';
import { PREF_OPTIONS } from '../www/js/constants/PREF_OPTIONS.js';

const bridge = installBridge();
let a, b;
const words = n => Array.from({ length: n }, (_, i) => ({ front: `word${i}`, back: `слово${i}` }));
test.beforeEach(async () => {
  bridge.reset();
  bridge.io.said.length = 0;
  const p = await insertProject('EN', 'en-US');
  a = await insertDeck(p, 'A');
  b = await insertDeck(p, 'B');
  await insertCards(a, words(8));
  await insertCards(b, words(8).map(c => ({ ...c, front: `b-${c.front}`, back: `б-${c.back}` })));
});

test('единая сессия: карточка всегда получает то же задание (вид, ответ, порядок вариантов), даже после возврата назад', async () => {
  const s = await startSession('all', 'srs');
  for (const id of s.order) {
    const card = s.map.get(id), dir = cardDir(s, card);
    const first = taskFor(s, card, dir);
    for (let i = 0; i < 6; i++) {
      const again = taskFor(s, card, dir);
      assert.equal(again?.kind ?? null, first?.kind ?? null);
      assert.deepEqual(again?.options, first?.options);
      assert.equal(again?.answer, first?.answer);
    }
  }
});

test('задание после возврата начинается с нуля: ответ, подсказки и введённый текст не «залипают»', async () => {
  const s = await startSession('all', 'srs');
  const card = currentCard(s), dir = cardDir(s, card);
  const q = taskFor(s, card, dir);
  Object.assign(q, { result: 'ok', hint: 2, typed: 'x', picked: 1 });
  const fresh = taskFor(s, card, dir);
  assert.deepEqual([fresh.result, fresh.hint, fresh.typed], [null, 0, '']);
  if (fresh.options) assert.equal(fresh.picked, null);
});

test('правка карточки в сессии сбрасывает её задание (текст изменился)', async () => {
  const s = await startSession('all', 'srs');
  const card = currentCard(s);
  taskFor(s, card, cardDir(s, card));
  assert.ok(card.id in s.tasks);
  refreshCard(s, card.id, { front: 'new', back: 'новый' });
  assert.equal(card.id in s.tasks, false);
});

test('у каждой колоды свои новые в день и свой размер плана; у колоды без своих — общие', async () => {
  const limits = { perDay: 5, used: {}, max: 99, decks: { [a]: { perDay: 2, pace: 1 }, [b]: { max: 3, pace: 1 } } };
  const s = await startSession('all', 'srs', limits);
  const inPlan = id => [...s.plan].filter(x => s.map.get(x).deck_id === id).length;
  assert.deepEqual([inPlan(a), inPlan(b)], [2, 3]);   // A — свои 2 новых; B — общие 5, но в плане не больше 3
  assert.equal(s.order.length, 5);                     // очередь = план
});

test('параметры колоды хранятся в settings и попадают в лимиты сессии; мусор игнорируется', async () => {
  await saveDeckPref(a, 'newPerDay', '3');
  await saveDeckPref(a, 'pace', '1.3');
  await saveDeckPref(b, 'sessionMax', '999');          // нет такого значения в списке
  await setSetting('deck:x:pace', '2');
  const prefs = await loadDeckPrefs();
  assert.deepEqual(prefs[a], { newPerDay: '3', pace: '1.3' });
  assert.equal(prefs[b], undefined);
  const limits = await loadLimits();
  assert.deepEqual(limits.decks[a], { perDay: 3, max: undefined, inOrder: false, pace: 1.3 });
});

test('темп колоды: «Чаще» сокращает, «Реже» удлиняет интервал; «Снова» и минимум в сутки не страдают', () => {
  const card = { ease: 2.5, interval: 10, reps: 4, due: 1 }, now = 0;
  const days = pace => nextSrs(card, 'good', now, pace).interval;
  assert.deepEqual([days(1), days(0.75), days(1.3)], [25, 19, 33]);
  assert.equal(nextSrs({ ease: 2.5, interval: 0, reps: 0, due: 0 }, 'good', now, 0.5).interval, 1);
  assert.equal(nextSrs(card, 'again', now, 1.7).interval, nextSrs(card, 'again', now).interval);
});

test('gradeCard применяет темп своей колоды', async () => {
  const limits = { perDay: 99, used: {}, max: 99, decks: { [a]: { pace: 1.7 }, [b]: { pace: 1 } } };
  const s = await startSession('all', 'srs', limits), card = currentCard(s);
  const base = nextSrs(card, 'good', Date.now(), 1).interval;
  await gradeCard(s, 'good');
  assert.equal(card.interval, Math.max(1, Math.round(base * (card.deck_id === a ? 1.7 : 1))));
});

test('плитки-переключатели идут по кругу и возвращаются к началу', () => {
  const opts = PREF_OPTIONS.theme;
  let v = 'auto';
  const seen = [];
  for (let i = 0; i < opts.length; i++) { v = nextOption(opts, v); seen.push(v); }
  assert.deepEqual(seen, ['light', 'dark', 'auto']);
  assert.equal(nextOption(opts, 'неизвестно'), 'auto');
});

test('cyclePref: значение меняется по кругу и сохраняется в базе; cycleDeckPref — то же для колоды', async () => {
  globalThis.document = { documentElement: { dataset: {} } };
  const label = { textContent: '' }, el = { querySelector: () => label };
  state.prefs.font = 's';
  await cyclePref({ key: 'font' }, el);
  assert.deepEqual([state.prefs.font, label.textContent], ['m', 'Средний']);
  await cyclePref({ key: 'font' }, el); await cyclePref({ key: 'font' }, el);
  assert.equal(state.prefs.font, 's');
  await cycleDeckPref({ id: String(a), key: 'pace' }, el);
  assert.deepEqual([(await loadDeckPrefs())[a].pace, label.textContent], ['0.75', 'Чаще']);
  for (let i = 0; i < 3; i++) await cycleDeckPref({ id: String(a), key: 'pace' }, el);
  assert.equal((await loadDeckPrefs())[a].pace, '1');   // четыре значения — круг замкнулся
  delete globalThis.document;
});

test('настройки одним экраном: все плитки с действием cyclePref, без сегментов', () => {
  const html = settingTiles();
  assert.equal((html.match(/data-act="cyclePref"/g) || []).length, 10);
  assert.doesNotMatch(html, /class="seg"/);
});

test('замедленный повтор: по словам, две градации, пауза только между словами; общая скорость не меняется', async () => {
  state.prefs.ttsRate = '1';
  const t0 = Date.now();
  await speakSlow('one two', 'en-US', 1);
  assert.deepEqual(bridge.io.said.map(x => [x.text, x.rate, x.wait]), [['one', 0.7, true], ['two', 0.7, true]]);
  assert.ok(Date.now() - t0 >= 790 && Date.now() - t0 < 1500);   // одна пауза 0,8 с, после последнего слова паузы нет
  bridge.io.said.length = 0;
  await speakSlow('go', 'en-US', 2);
  assert.equal(bridge.io.said[0].rate, 0.5);
  assert.equal(state.prefs.ttsRate, '1');
});

test('обычная озвучка прерывает замедленный повтор', async () => {
  const run = speakSlow('a b c', 'en-US', 1);
  await new Promise(r => setTimeout(r, 50));
  speakText('stop', 'en-US');
  await run;
  assert.deepEqual(bridge.io.said.map(x => x.text), ['a', 'stop']);
});

test('«На слух»: слово скрыто, есть два замедленных повтора и кнопка «Показать текстом» (скрыта до таймера, место занято)', async () => {
  const s = await startSession('all', 'srs'), card = currentCard(s);
  const html = studyFace(card, false, 'fwd', { id: card.id, kind: 'audio-choice', answer: card.back, options: [card.back], result: null });
  assert.match(html, /class="big hidtext" hidden/);
  assert.match(html, /data-level="1"/);
  assert.match(html, /data-level="2"/);
  assert.match(html, /class="sm showtext wait"/);
});

test('textLater: через textAfter мс кнопка «Показать текстом» становится видимой', () => {
  mock.timers.enable({ apis: ['setTimeout'] });
  const classes = new Set(['wait']);
  globalThis.document = { querySelector: sel => (sel === '.showtext.wait' && classes.has('wait') ? { classList: { remove: c => classes.delete(c) } } : null) };
  Object.assign(track, { n: 5, live: true });
  textLater();
  mock.timers.tick(5900);
  assert.equal(classes.has('wait'), true);
  mock.timers.tick(200);
  assert.equal(classes.has('wait'), false);
  delete globalThis.document;
  mock.timers.reset();
});

test('сводка колоды на экране «Учёбы»: карточки, к повторению и собственные параметры', () => {
  state.prefs.newPerDay = '10'; state.prefs.sessionMax = '50';
  const deck = { id: 7, n: 12, due: 3 };
  assert.equal(deckStudyMeta(deck, {}), '12 карт. · 3 повторить\n10/день · 50 в сессии · обычный');
  assert.equal(deckStudyMeta(deck, { 7: { newPerDay: '3', sessionMax: '9999', pace: '1.3' } }), '12 карт. · 3 повторить\n3/день · все в сессии · реже');
});

test('удаление колоды и проекта убирает их собственные параметры повторения, чужие остаются', async () => {
  const deleteDeck = (await import('../www/js/repo/deleteDeck.js')).default;
  const deleteProject = (await import('../www/js/repo/deleteProject.js')).default;
  const p2 = await insertProject('DE', 'de-DE'), d2 = await insertDeck(p2, 'X');
  for (const id of [a, b, d2]) await saveDeckPref(id, 'pace', '1.3');
  await setSetting('theme', 'dark');
  await deleteDeck(a);
  assert.deepEqual(Object.keys(await loadDeckPrefs()).sort(), [String(b), String(d2)].sort());
  await deleteProject(p2);
  assert.deepEqual(Object.keys(await loadDeckPrefs()), [String(b)]);
  assert.ok((await (await import('../www/js/repo/listSettings.js')).default()).some(r => r.key === 'theme'));
});

test('показы заданий равномерны, ввод с клавиатуры — в 4 раза реже каждого другого', async () => {
  const s = await startSession('all', 'srs');
  for (const c of s.map.values()) c.cloze_answer = `w${c.id}`;
  const card = { id: 1, cloze: 'I ___ it.', cloze_answer: 'made' };
  const n = { 'visual-choice': 0 };
  for (let i = 0; i < 400; i++) { const k = quizPick(s, card, 'rev'); n[k] = (n[k] ?? 0) + 1; }
  const choice = ['visual-choice', 'audio-choice', 'cloze-choice', 'speak'].map(k => n[k]);
  assert.ok(Math.max(...choice) - Math.min(...choice) <= 1, JSON.stringify(n));
  for (const k of ['visual-type', 'cloze-type']) assert.ok(Math.abs(n[k] * 4 - n['audio-choice']) <= 4, JSON.stringify(n));
});

test('ввод слова изучаемого языка предлагается только когда нужно написать его (направление rev); без пропуска нет cloze', async () => {
  const s = await startSession('all', 'srs');
  const seen = new Set();
  for (let i = 0; i < 200; i++) seen.add(quizPick(s, { id: 2 }, 'fwd'));
  assert.deepEqual([...seen].sort(), ['audio-choice', 'speak', 'visual-choice']);
});

test('старая очередь не продолжается: «Снова» не возвращается, сверх плана ничего нет', async () => {
  const s = await startSession('all', 'srs', { perDay: 3, used: {}, max: 99 });
  const ids = [...s.order];
  for (let i = 0; i < ids.length; i++) await gradeCard(s, i === 0 ? 'again' : 'good');
  assert.equal(s.order.length, 0);
  assert.equal(new Set(s.log.map(l => l.id)).size, ids.length);   // каждая карточка плана — один раз
});

test('«Повторить ошибки»: круг по карточкам с «Снова», расписание и история не меняются', async () => {
  const s = await startSession('all', 'srs', { perDay: 2, used: {}, max: 99 });
  const ids = [...s.order];
  await gradeCard(s, 'again'); await gradeCard(s, 'good'); await gradeCard(s, 'again'); await gradeCard(s, 'good');
  assert.deepEqual(mistakesOf(s).sort(), [ids[0], ids[2]].sort());
  const before = JSON.stringify([...s.map.values()].map(c => [c.id, c.due, c.interval, c.ease, c.reps]));
  const reviews = (await listReviewsSince(0)).length;
  retrySession(s);
  assert.deepEqual([s.order.length, s.retry, s.log.length], [2, true, 0]);
  await gradeCard(s, 'good'); await gradeCard(s, 'again');
  assert.equal(JSON.stringify([...s.map.values()].map(c => [c.id, c.due, c.interval, c.ease, c.reps])), before);
  assert.equal((await listReviewsSince(0)).length, reviews);
  assert.equal(mistakesOf(s).length, 1);   // можно повторять, пока не останется ошибок
});

test('порядок новых карточек колоды «По порядку»: как добавлены; у остальных — вперемешку', async () => {
  const cards = [...(await startSession('d:' + a, 'srs')).map.values()];
  const shuffled = [...cards].reverse();
  const out = orderFresh(shuffled, { decks: { [a]: { inOrder: true } } });
  assert.deepEqual(out.map(c => c.ord), [...cards].map(c => c.ord).sort((x, y) => x - y));
  assert.deepEqual(orderFresh(shuffled, { decks: {} }), shuffled);
  const s = await startSession('all', 'srs', { perDay: 3, used: {}, max: 99, decks: { [a]: { inOrder: true } } });
  const ofA = s.order.filter(id => s.map.get(id).deck_id === a).map(id => s.map.get(id).ord);
  assert.deepEqual(ofA, [...ofA].sort((x, y) => x - y));
});

test('разброс интервалов: только от недели и до ±5 %, без него — как раньше', () => {
  const card = { ease: 2.5, interval: 10, reps: 4, due: 1 };
  assert.deepEqual([1, -1, 0].map(f => nextSrs(card, 'good', 0, 1, f).interval), [26, 24, 25]);
  const young = { ease: 2.5, interval: 1, reps: 2, due: 1 };
  assert.equal(nextSrs(young, 'good', 0, 1, 1).interval, nextSrs(young, 'good', 0, 1, -1).interval);
});

test('маленькая колода: выбор невозможен — обычная карточка (flip), пропуск вписывают; без распознавания речи нет «произношения»', async () => {
  const s = { map: new Map([[1, { id: 1, front: 'a', back: 'а' }], [2, { id: 2, front: 'b', back: 'б' }]]), noSpeech: true };
  const kinds = new Set();
  for (let i = 0; i < 60; i++) kinds.add(quizPick(s, s.map.get(1), 'rev'));
  assert.deepEqual([...kinds].sort(), ['flip', 'visual-type']);
  assert.equal(makeQuiz(s, s.map.get(1), 'fwd', 'visual-choice'), null);
  s.map.get(1).cloze_answer = 'x'; s.map.get(1).cloze = '_ x';
  assert.equal(makeQuiz(s, s.map.get(1), 'fwd', 'cloze-choice').kind, 'cloze-type');
});
