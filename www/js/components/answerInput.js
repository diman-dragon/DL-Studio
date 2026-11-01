import esc from '../util/esc.js';

export default function answerInput({ hint, answer }) {
  const placeholder = hint ? `${answer.slice(0, hint)}…` : 'Ваш ответ';
  return `<input id="ans" class="ans" data-enter="check" autocomplete="off" `
    + `autocapitalize="off" spellcheck="false" placeholder="${esc(placeholder)}" `
    + `value="${esc(answer.typed)}">`;
}
