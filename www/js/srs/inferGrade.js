import { BEHAVIOR } from '../constants/BEHAVIOR.js';
import speechGrade from './speechGrade.js';
const level = (v, [a, b]) => +(v >= a) + +(v >= b);
/** Вопрос с выбором / вводом: верно → по времени ответа, подсказка — трудно, неверно — снова; без ответа — не оцениваем. */
const byAnswer = ({ think, correct, hinted, kind }, B) => {
  if (correct === null) return null;
  if (!correct) return 'again';
  const k = kind?.endsWith('-type') ? 2.5 : 1;
  return hinted ? 'hard' : think < B.think[0] * k ? 'easy' : think < B.think[1] * k ? 'good' : 'hard';
};
/**
 * Оценка по поведению. null — карточку просто пролистали или отвлеклись: расписание не трогаем.
 * Без переворота: ушёл быстро — знает (good, у зрелых easy), долго мялся — hard; произношение оценивается по сходству, без попытки оценки нет.
 * С переворотом «трудность» набирается из: долго вспоминал, долго читал ответ, метался туда-обратно, возвращался свайпом назад,
 * перевернул не вспоминая и читал ответ, подсматривал текст («На слух»), многократно переслушивал. 0 — лёгкая, 1 — хорошо, 2 — трудно, 3+ — снова.
 * B — пороги (по умолчанию общие; после калибровки — подстроенные под человека, srs/calibrate).
 */
export default function inferGrade(sig, card, B = BEHAVIOR) {
  const { think, look, dwell, flips, revisit, away, kind, hinted, plays } = sig;
  if (away) return null;
  if (sig.speak) return sig.score == null ? null : speechGrade(sig.score, sig.attempts, hinted);   // «Говорить»: по произношению
  if (kind) return byAnswer(sig, B);
  if (dwell < B.noise) return null;
  if (think === null) return dwell >= B.think[1] ? 'hard' : dwell < B.known && card.reps >= 2 ? 'easy' : 'good';
  const d = level(think, B.think) + level(look, B.look) + +(flips >= B.flips) + +revisit + +(think < B.reflex && look >= B.readAnswer) + +!!hinted + +(plays >= B.plays);
  if (d >= 3) return 'again';
  if (d === 2) return 'hard';
  return d === 0 && think >= B.reflex && think < B.easyThink && look < B.easyLook ? 'easy' : 'good';
}
