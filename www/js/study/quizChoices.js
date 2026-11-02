import shuffle from '../util/shuffle.js';
/** Правильный ответ + три чужих, перемешанные; null — разных значений слишком мало (задание станет вводом). */
export default function quizChoices(answer, values) {
  const pool = [...new Set(values)].filter(value => value && value !== answer);
  return pool.length < 3 ? null : shuffle([answer, ...shuffle(pool).slice(0, 3)]);
}
