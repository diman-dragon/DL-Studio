import os
from playwright.sync_api import sync_playwright

INIT = """
window.Capacitor = { nativePromise: async (name, m, a) => {
  const r = await fetch('/__bridge', {method:'POST', body: JSON.stringify({name, a})}); const j = await r.json();
  if (j.err) throw new Error(j.err); return j.r;
} };
"""
CHECK = """
() => {
  const out = [], vw = innerWidth, vh = innerHeight;
  const path = e => { const p=[]; while(e && e.id!=='app' && p.length<5){ p.unshift(e.tagName.toLowerCase()+(e.className&&typeof e.className==='string'?'.'+e.className.trim().split(/\\s+/).join('.'):'')); e=e.parentElement;} return p.join('>'); };
  const vis = e => { const r=e.getBoundingClientRect(), cs=getComputedStyle(e); return r.width>0 && r.height>0 && cs.visibility!=='hidden' && cs.display!=='none' && !e.closest('[hidden]'); };
  for (const e of document.querySelectorAll('#app *')) {
    if (!vis(e) || e.closest('svg')) continue;
    const r=e.getBoundingClientRect(), cs=getComputedStyle(e), txt=[...e.childNodes].some(n=>n.nodeType===3&&n.textContent.trim());
    if (r.right>vw+1 || r.left<-1) out.push(['offscreen-x',path(e),Math.round(r.left),Math.round(r.right)]);
    if (r.bottom>vh+1 && !e.closest('.sheet-list') && !e.closest('.view.form')) out.push(['offscreen-y',path(e),Math.round(r.bottom),vh]);
    if (txt && e.scrollWidth>e.clientWidth+1 && cs.display!=='inline') out.push(['scroll-x',path(e),e.scrollWidth,e.clientWidth]);
    if (txt && cs.overflow==='hidden' && e.scrollHeight>e.clientHeight+1) out.push(['scroll-y',path(e),e.scrollHeight,e.clientHeight]);
  }
  return out;
}
"""

def run(vw, vh, font, shots):
    probs = []
    with sync_playwright() as p:
        browser = p.chromium.launch(executable_path=os.environ.get('CHROMIUM', '/opt/pw-browsers/chromium'), args=['--no-sandbox'])
        ctx = browser.new_context(viewport={'width': vw, 'height': vh}, device_scale_factor=1, has_touch=True, is_mobile=True)
        ctx.add_init_script(INIT)
        page = ctx.new_page(); errors = []
        page.on('pageerror', lambda e: errors.append(str(e)))
        page.goto('http://localhost:8765/'); page.wait_for_selector('.tabs')

        def chk(label):
            page.wait_for_timeout(250)
            for result in page.evaluate(CHECK): probs.append((label, result))
            if shots: page.screenshot(path=f'shots/{vw}x{vh}-{font}-{label}.png')

        def act(name, **data):
            selector = f'[data-act="{name}"]' + ''.join(f'[data-{key}="{value}"]' for key, value in data.items())
            if page.locator(selector).count(): page.locator(selector).first.click(timeout=3000)
            else: page.evaluate("async ([name, data]) => (await import('/js/actions/' + name + '.js')).default(data)", [name, {k: str(v) for k, v in data.items()}])
            page.wait_for_timeout(250)

        def pages(label, count=None):
            n = count or page.evaluate("document.querySelectorAll('#dots .dot').length || 1")
            for i in range(n):
                if i: act('gotoPage', i=i)
                chk(f'{label}-p{i}')
            if n > 1: act('gotoPage', i=0)

        for layout in ('list', 'grid'):
            if layout == 'grid': act('toggleLayout')
            act('goStudy'); pages(f'study-{layout}')
            act('goStats'); pages(f'stats-{layout}', 11)
            act('goLibrary'); pages(f'library-{layout}')
            act('goProject', id=1); pages(f'project-{layout}')
            act('goDeck', id=1); pages(f'deck-{layout}')
        act('toggleLayout'); act('goSettings'); pages('settings', 6)
        act('goLibrary'); act('newProject'); chk('project-form'); act('back')
        act('goProject', id=1); act('newDeck'); chk('deck-form'); act('back')
        act('goDeck', id=1); act('newCard'); chk('card-form'); act('back')
        act('goStudy'); act('startStudy', id='p:1'); page.wait_for_timeout(300)
        for i in range(7):
            chk(f'srs-{i}')
            if page.locator('[data-act="giveUp"]').count(): act('giveUp')
            if page.locator('[data-act="skip"]').count(): act('skip')
        browser.close()
    return probs, errors

CONF = {(360,640,'m'), (360,640,'l'), (320,568,'m')}
if __name__ == '__main__':
    seen = set()
    for (vw, vh) in [(360, 640), (320, 568), (412, 915)]:
        for font in ('m', 'l'):
            if (vw, vh, font) not in CONF: continue
            probs, errors = run(vw, vh, font, shots=(vw == 360 and font == 'm'))
            print(f'== {vw}x{vh} font={font}: {len(probs)} проблем, js-ошибок: {errors[:3]}')
            for label, result in probs:
                key = (result[0], result[1])
                if key in seen: continue
                seen.add(key); print('  ', label, result)
