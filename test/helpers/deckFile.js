const TEST_DECK = {
  format: 'flashcards-deck', version: 1, language: { name: 'English', tts: 'en-US' }, deck: { name: 'Basics' },
  cards: ['apple — яблоко', 'water — вода', 'house — дом', 'learn — учить', 'friend — друг', 'time — время', 'beautiful — красивый', 'phone — телефон']
    .map(pair => { const [front, back] = pair.split(' — '); return { front, back }; })
};
/** Тестовая колода формируется в тесте, в приложении демо-данных нет. */
export default function deckFile() {
  return { name: 'deck.json', data: Buffer.from(JSON.stringify(TEST_DECK)).toString('base64') };
}
