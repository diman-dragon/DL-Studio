import { state } from '../state/appState.js';
import ttsSpeak from '../native/ttsSpeak.js';

/** Озвучка текста голосом устройства, без выбора отдельных голосов. */
export default function speakText(text, lang) {
  return ttsSpeak(text, lang, { rate: Number(state.prefs.ttsRate), pitch: Number(state.prefs.ttsPitch) });
}
