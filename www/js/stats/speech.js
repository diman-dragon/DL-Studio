import { SPEECH } from '../constants/SPEECH.js';
import statCard from '../components/statCard.js';
import barChart from '../components/barChart.js';
import row from '../components/row.js';
import dayBars from '../study/dayBars.js';

/** Произношение остаётся частью интервального повторения: попытки и средняя оценка. */
export default function speech(d) {
  const n = d.speech.length, avg = n ? Math.round(100 * d.speech.reduce((a, r) => a + Number(r.score), 0) / n) : null;
  const ok = d.speech.filter(r => Number(r.score) >= SPEECH.good).length, v = avg === null ? '–' : `${avg}%`;
  return {
    icon: '🎤', title: 'Произношение', value: n, sub: `средняя оценка ${v} · удачных ${ok}`,
    detail: row(statCard(n, 'попыток') + statCard(v, 'средняя оценка') + statCard(ok, 'удачных'), 'stats s3') + '<h4>Попыток по дням</h4>' + barChart(dayBars(d.speech))
  };
}
