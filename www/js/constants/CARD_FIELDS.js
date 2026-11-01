/** id — поле формы, key — поле в приложении, col — столбец в БД. */
export const CARD_FIELDS = [
  { id: 'f', key: 'front', col: 'front', label: 'Слово / фраза', ph: 'apple' },
  { id: 'b', key: 'back', col: 'back', label: 'Перевод', ph: 'яблоко' },
  { id: 'e', key: 'example', col: 'example', label: 'Пример', ph: 'I eat an apple every day.', multiline: true },
  { id: 'et', key: 'exampleTr', col: 'example_tr', label: 'Перевод примера', ph: 'Я ем яблоко каждый день.', multiline: true },
  { id: 'cl', key: 'cloze', col: 'cloze', label: 'Фраза с пропуском', ph: 'I ___ a decision.', multiline: true },
  { id: 'ca', key: 'clozeAnswer', col: 'cloze_answer', label: 'Слово для пропуска', ph: 'made' },
  { id: 'ct', key: 'clozeTr', col: 'cloze_tr', label: 'Перевод фразы', ph: 'Я принял решение.', multiline: true }
];
