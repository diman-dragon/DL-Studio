/** Способ ответа: choice | type | speak | flip. Заданный вид (visual-choice…) определяет его сам; иначе — выбор в 4 раза чаще ввода, изредка произношение. */
export default function quizCheck(requested, fixedKind) {
  if (fixedKind) return fixedKind.endsWith('-choice') ? 'choice' : 'type';
  return requested || (Math.random() < 0.8 ? 'choice' : Math.random() < 0.25 ? 'speak' : 'type');
}
