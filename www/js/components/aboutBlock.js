export default function aboutBlock({ name, version, logo }) {
  return `<div class="about"><span class="brand">${logo}</span>`
    + `<div class="muted">Версия ${version}<br>Офлайн-интервальные повторения<br>© ${name}</div></div>`;
}
