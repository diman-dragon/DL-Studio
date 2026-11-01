import entry from './entry.js';

export default function statEntry({ stat, index }) {
  return entry({
    act: 'openStat', id: index, title: stat.title, sub: stat.sub,
    lead: `<span class="avatar ico">${stat.icon}</span>`,
    acts: `<span class="badge">${stat.value}</span>`
  });
}
