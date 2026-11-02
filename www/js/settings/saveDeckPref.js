import setSetting from './setSetting.js';
export default function saveDeckPref(deckId, name, value) { return setSetting(`deck:${deckId}:${name}`, value); }
