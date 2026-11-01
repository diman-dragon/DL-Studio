import plugin from './plugin.js';
/** opts — { rate, pitch }. */
export default function ttsSpeak(text, lang, opts = {}) { return plugin('TtsSpeak').exec({ text, lang, ...opts }); }
