import shuffle from '../util/shuffle.js';
/** Правильный ответ + три чужих из pool (study/quizPool), перемешанные; null — вариантов мало (меньше трёх чужих). */
export default function quizChoices(answer, pool) { return pool.length < 3 ? null : shuffle([answer, ...shuffle(pool).slice(0, 3)]); }
