import currentCard from './currentCard.js';
import nextSrs from '../srs/nextSrs.js';
import isNewCard from '../srs/isNewCard.js';
import updateCardSrs from '../repo/updateCardSrs.js';
import insertReview from '../repo/insertReview.js';
import snapshotGrade from './snapshotGrade.js';
import requeue from './requeue.js';
import checkLeech from './checkLeech.js';
import markStudied from './markStudied.js';
import paceOf from './paceOf.js';
import randomFuzz from './randomFuzz.js';
/**
 * Оценка карточки. В базу пишется запись истории (оценка, время, подсказка, прослушивания) и новое расписание карточки (SM-2, темп — свой у колоды,
 * небольшой разброс длинных интервалов). sig — сигналы поведения и произношения. В «повторе ошибок» (session.retry) ничего не пишется:
 * расписание и история остаются как были. Перед изменениями запоминается снимок — «↶» (study/undoGrade) вернёт всё как было.
 */
export default async function gradeCard(session, grade, sig = {}) {
  const card = currentCard(session);
  if (!card) return;
  const live = !session.retry, fresh = live && isNewCard(card);
  session.undo = snapshotGrade(session, card);
  if (live) {
    Object.assign(card, nextSrs(card, grade, Date.now(), paceOf(session, card), randomFuzz()));
    await updateCardSrs(card.id, card);
    session.undo.review = await insertReview(card.id, grade, sig, fresh, session.mode);
    markStudied();
  }
  requeue(session);
  session.plan.delete(card.id);   // карточка плана пройдена
  session.log.push({ id: card.id, grade });
  if (live && grade === 'again') await checkLeech(session, card);
}
