/** Способ ответа: choice | type | speak | flip. Заданный вид (visual-choice…) определяет его сам. */
export default function quizCheck(requested, fixedKind) {
  if (fixedKind) return fixedKind.endsWith('-choice') ? 'choice' : 'type';
  return requested;
}
