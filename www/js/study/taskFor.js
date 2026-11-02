import makeQuiz from './makeQuiz.js';
/**
 * Задание карточки фиксируется на всю сессию (session.tasks): вернулись назад — тот же вид задания (на слух, выбор, ввод, произношение)
 * и тот же порядок вариантов. Ответ, подсказки и введённый текст каждый раз начинаются с нуля. null — обычная карточка с переворотом.
 */
export default function taskFor(session, card, dir) {
  const tasks = (session.tasks ??= {});
  if (!(card.id in tasks)) tasks[card.id] = makeQuiz(session, card, dir);
  const task = tasks[card.id];
  return task && { ...task, result: null, hint: 0, typed: '', ...(task.options ? { picked: null } : {}) };
}
