import { state } from '../state/appState.js';
import { slow } from '../state/slowState.js';
import ttsSpeak from '../native/ttsSpeak.js';

/** Озвучка текста голосом устройства; прерывает замедленный повтор по словам. */
export default function speakText(text, lang) {
  slow.run++;
  return ttsSpeak(text, lang, { rate: Number(state.prefs.ttsRate), pitch: Number(state.prefs.ttsPitch) });
}
