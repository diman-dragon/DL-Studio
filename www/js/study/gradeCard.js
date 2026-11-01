import currentCard from './currentCard.js';
import nextSrs from '../srs/nextSrs.js';
import isNewCard from '../srs/isNewCard.js';
import updateCardSrs from '../repo/updateCardSrs.js';
import insertReview from '../repo/insertReview.js';
import snapshotGrade from './snapshotGrade.js';
import requeue from './requeue.js';
import checkLeech from './checkLeech.js';
import markStudied from './markStudied.js';
/**
 * Оценка карточки. Всё пишется в базу: запись истории (оценка, время, подсказка, прослушивания) и, в интервальном повторении, новое
 * расписание карточки (SM-2). sig — сигналы поведения и произношения.
 * Перед изменениями запоминается снимок — «↶» (study/undoGrade) вернёт всё как было. Счётчик дня и серия считаются из истории.
 */
export default async function gradeCard(session, grade, sig = {}) {
  const card = currentCard(session);
  if (!card) return;
  const srs = session.mode === 'srs', fresh = srs && isNewCard(card);
  session.undo = snapshotGrade(session, card);
  if (srs) {
    Object.assign(card, nextSrs(card, grade));
    await updateCardSrs(card.id, card);
  }
  session.undo.review = await insertReview(card.id, grade, sig, fresh, session.mode);
  markStudied();
  requeue(session, grade);
  if (grade !== 'again') session.plan.delete(card.id);   // карточка плана пройдена; «снова» остаётся в плане
  session.log.push({ id: card.id, grade });
  if (srs && grade === 'again') await checkLeech(session, card);
}
