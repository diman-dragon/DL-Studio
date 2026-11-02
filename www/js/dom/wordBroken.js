/** Есть ли в элементе слово, разорванное переносом по буквам (кроме законного переноса по дефису): слово занимает больше одной строки. */
export default function wordBroken(el) {
  const range = document.createRange();
  for (const node of el.childNodes) {
    if (node.nodeType !== 3) continue;
    for (const m of node.textContent.matchAll(/[^\s-]{4,}/g)) {
      range.setStart(node, m.index);
      range.setEnd(node, m.index + m[0].length);
      if (range.getClientRects().length > 1) return true;
    }
  }
  return false;
}
