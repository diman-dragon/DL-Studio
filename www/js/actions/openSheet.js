import { state } from '../state/appState.js';
import render from '../nav/render.js';
/** Открыть нижнее окно (data-id: 'json' | 'about' на экране настроек). */
export default function openSheet(d) { state.sheet = d.id; return render(); }
