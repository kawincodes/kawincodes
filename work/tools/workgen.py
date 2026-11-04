#!/usr/bin/env python3
"""Generate work.md + work/ (SQL catalogue, screen gallery, messages, prefs, extras, layouts, assets).

usage: workgen.py <scratchpad> <repo>
"""
import json, os, re, shutil, sys
from collections import Counter, defaultdict
S, REPO = sys.argv[1], sys.argv[2]
W = S + '/work'
WORK = REPO + '/work'
sql = json.load(open(W + '/sql_full.json'))
ui = json.load(open(W + '/ui_full.json'))
smap = json.load(open(W + '/screenmap.json'))
specs = json.load(open(W + '/render_specs.json'))
used_by_screen = json.load(open(W + '/screen_layouts.json'))
groups = json.load(open(W + '/groups_final.json'))
x = json.load(open(W + '/xref.json'))
didx = json.load(open(W + '/out_images/drawables/_index.json'))
applayouts = open(W + '/applayouts.txt').read().split()
man = open(S + '/out/apktool/AndroidManifest.xml').read()

def slug(h):
    s = h.strip().lower(); s = re.sub(r'[`*_~]', '', s); s = re.sub(r'[^\w\- ]', '', s, flags=re.U); return s.replace(' ', '-')
def esc(s):
    return s.replace('|', '\\|').replace('\n', ' ')

# ---------- module map from brain/modules ----------
MODDIR = REPO + '/brain/modules'
modules = []
cls_mod = {}      # class -> module index
cls_anchor = {}   # class -> (file, anchor, title)
for f in sorted(os.listdir(MODDIR)):
    t = open(os.path.join(MODDIR, f)).read()
    m = re.match(r'# (8\.\d+) (.+)', t)
    modules.append({'num': m.group(1), 'title': m.group(2).strip(), 'file': f, 'text': t})
for i, m in enumerate(modules):
    for h in re.finditer(r'^## (.+?) — `(\w+)`', m['text'], re.M):
        cls_anchor.setdefault(h.group(2), (m['file'], slug(h.group(0)[3:]), h.group(1)))
        cls_mod.setdefault(h.group(2), i)
# group -> module indices, replicating brain's assembly order (one module per H2 of each section file)
ORDER = ['G02_boot_registration_settings', 'G01_hub_navigation', 'G03_offline_download_upload', 'G04_offline_collection_entry',
         'G05_online_collection_editing', 'G06_collection_reports', 'G07_party_and_loan_creation', 'G08_loan_lists_closures_transfers',
         'G09_areas_maps_gps_attendance_and_accounts_reports', 'G10a_patti_reports_meeting', 'G10b_thittam_weekly_reports',
         'G11_accounts_cashbook', 'G13_st_of_loans_finance', 'G14_finance_ledgers_analysis', 'G15_admin_masters_employees',
         'G16_inventory', 'G17_printing', 'G18_data_layer_utils', 'G00_core_constants']
group_mods, k = {}, 0
for g in ORDER:
    sp = f'{W}/sections/{g}.md'
    if not os.path.exists(sp): continue
    n = len(re.findall(r'(?m)^## (?!#)', open(sp).read())) or 1
    group_mods[g] = list(range(k, k + n)); k += n
cls_group = {c: g for g, v in groups.items() for c in v['classes']}
for c in x:
    if c in cls_mod: continue
    cand = group_mods.get(cls_group.get(c), [])
    hit = next((i for i in cand if re.search(r'`' + re.escape(c) + r'`', modules[i]['text'])), cand[0] if cand else None)
    cls_mod[c] = hit if hit is not None else len(modules) - 1

if os.path.isdir(WORK): shutil.rmtree(WORK)
for d in ('sql', 'screens', 'img/drawables', 'img/swatches', 'img/assets', 'tools'):
    os.makedirs(f'{WORK}/{d}', exist_ok=True)

# ---------- images ----------
SCR = W + '/out_images/screens'
for f in os.listdir(SCR):
    if f.endswith('.png'):
        if f.startswith('dr_'):
            shutil.copy(f'{SCR}/{f}', f'{WORK}/img/swatches/{f[3:]}')
        else:
            shutil.copy(f'{SCR}/{f}', f'{WORK}/screens/{f}')
for f in os.listdir(W + '/out_images/drawables'):
    if not f.startswith('_'): shutil.copy(f'{W}/out_images/drawables/{f}', f'{WORK}/img/drawables/{f}')
for f in os.listdir(W + '/out_images/assets'):
    shutil.copy(f'{W}/out_images/assets/{f}', f'{WORK}/img/assets/{f}')
have = set(os.listdir(WORK + '/screens'))

# ---------- SQL formatting ----------
KW = ['LEFT OUTER JOIN', 'RIGHT OUTER JOIN', 'LEFT JOIN', 'RIGHT JOIN', 'INNER JOIN', 'CROSS JOIN', 'FULL JOIN', 'UNION ALL', 'UNION',
      'GROUP BY', 'ORDER BY', 'HAVING', 'WHERE', 'FROM', 'VALUES', 'JOIN']
KWRX = re.compile(r'\b(' + '|'.join(k.replace(' ', r'\s+') for k in KW) + r')\b', re.I)
def fmt_sql(s):
    s = s.strip()
    if len(s) < 110: return s
    out, i, q, dep = [], 0, False, 0
    while i < len(s):
        ch = s[i]
        if s.startswith('${', i): dep += 1
        elif ch == '}' and dep: dep -= 1
        elif ch == "'" and not dep: q = not q
        if not q and not dep and (i == 0 or s[i - 1] in ' \t'):
            m = KWRX.match(s, i)
            if m and i > 0:
                while out and out[-1] in ' \t': out.pop()
                out.append('\n')
        out.append(ch); i += 1
    return ''.join(out)

def where_link(c):
    if c in cls_anchor:
        f, a, _ = cls_anchor[c]; return f'../../brain/modules/{f}#{a}'
    return f'../../brain/modules/{modules[cls_mod[c]]["file"]}'

# ---------- SQL per module ----------
st = sql['stmts']
for s in st:
    s['mod'] = cls_mod.get(s['cls'], len(modules) - 1)
bymod = defaultdict(list)
for s in st: bymod[s['mod']].append(s)
sqlfiles = {}
for mi, m in enumerate(modules):
    items = bymod.get(mi, [])
    if not items: continue
    fn = m['file']
    L = [f'# SQL — {m["num"]} {m["title"]}', '',
         f'[← work.md](../../work.md) · [Module spec](../../brain/modules/{fn}) · [By table](by-table.md) · [By procedure](by-procedure.md)', '',
         f'Every SQL statement executed by the classes of this module: **{len(items)} statements** '
         f'({sum(1 for s in items if s["db"] == "local SQLite")} local SQLite, {sum(1 for s in items if s["db"] == "SQL Server")} SQL Server). '
         'Each entry gives the reconstructed SQL text (non-literal parts shown as `${expression}`), how it is executed, the parameters bound by index, OUT parameters, and the result columns the code reads. Line numbers refer to the JADX-decompiled source.', '']
    bycls = defaultdict(list)
    for s in items: bycls[s['cls']].append(s)
    order = sorted(bycls, key=lambda c: (0 if c in cls_anchor else 1, c.lower()))
    L.append('**Classes:** ' + ' · '.join(f'[`{c}`](#{slug(c)}) ({len(bycls[c])})' for c in order))
    L.append('')
    for c in order:
        L.append(f'## {c}'); L.append('')
        title = cls_anchor.get(c, (None, None, None))[2]
        L.append((f'Screen: **{title}** · ' if title else '') + f'[spec]({where_link(c)}) · {len(bycls[c])} statements'); L.append('')
        # merge identical statements in the same method
        seen = {}
        for s in sorted(bycls[c], key=lambda s: (s['inner'], s['method'], s['line'])):
            key = (s['inner'], s['method'], s['kind'], s['sql'])
            if key in seen:
                seen[key]['lines'].append(s['line']); continue
            s = dict(s); s['lines'] = [s['line']]; seen[key] = s
        for s in seen.values():
            meth = (s['inner'] + '.' if s['inner'] else '') + s['method']
            lines = ', '.join(str(n) for n in s['lines'])
            head = [f'**{s["db"]}**', f'`{s["kind"]}`']
            if s.get('conn'): head.append(s['conn'])
            head.append(s['op'].upper())
            if s['tables']: head.append('tables: ' + ', '.join(f'`{t}`' for t in s['tables']))
            if s['procs']: head.append('procedure: ' + ', '.join(f'`{p}`' for p in s['procs']))
            L.append(f'#### `{meth}` · line {lines}')
            L.append(' · '.join(head))
            L.append('```sql'); L.append(fmt_sql(s['sql'])); L.append('```')
            extra = []
            if s.get('args'): extra.append('Selection args: ' + ', '.join(f'`{a}`' for a in s['args']))
            if s['params']:
                extra.append('Params: ' + ' · '.join(f'{p["i"]} → `{p["expr"]}` ({p["set"]})' for p in s['params']))
            if s['outs']:
                extra.append('OUT: ' + ' · '.join(f'{o["i"]} ({o["type"]})' for o in s['outs']))
            if s['reads']:
                extra.append('Reads: ' + ', '.join(f'`{r}`' for r in s['reads']))
            if extra:
                L.append(''); L += ['- ' + e for e in extra]
            L.append('')
    open(f'{WORK}/sql/{fn}', 'w').write('\n'.join(L) + '\n')
    sqlfiles[mi] = (fn, len(items))

# by table
tab = defaultdict(list)
canon = {}
for s in st:
    for t in s['tables']:
        k = t.lower(); tab[k].append(s); canon.setdefault(k, Counter())[t] += 1
L = ['# SQL by table', '', '[← work.md](../../work.md)', '',
     f'Every table named in the {len(st)} extracted statements, with each statement that touches it. **L** = local SQLite, **R** = remote SQL Server.', '',
     '| Table | Local | Remote | Statements (operation · class.method:line) |', '|---|---|---|---|']
for k in sorted(tab, key=lambda k: canon[k].most_common(1)[0][0].lower()):
    name = canon[k].most_common(1)[0][0]
    loc = sum(1 for s in tab[k] if s['db'] == 'local SQLite'); rem = len(tab[k]) - loc
    refs = []
    for s in sorted(tab[k], key=lambda s: (s['cls'], s['line'])):
        fn = modules[s['mod']]['file']
        refs.append(f'{"L" if s["db"] == "local SQLite" else "R"} {s["op"].upper()} [`{s["cls"]}.{s["method"]}:{s["line"]}`]({fn}#{slug(s["cls"])})')
    L.append(f'| `{name}` | {loc} | {rem} | ' + '<br>'.join(refs[:40]) + (f'<br>… +{len(refs) - 40} more' if len(refs) > 40 else '') + ' |')
open(f'{WORK}/sql/by-table.md', 'w').write('\n'.join(L) + '\n')
# by procedure
proc = defaultdict(list)
for s in st:
    for p in s['procs']: proc[p].append(s)
L = ['# SQL Server stored procedures — every call site', '', '[← work.md](../../work.md)', '',
     f'**{len(proc)} procedures** called from the app. For each call: the call text with its parameters, the bound values, OUT parameters and columns read back. The procedure bodies live only on the server and are not in the APK.', '']
for p in sorted(proc):
    L.append(f'## `{p}`'); L.append('')
    for s in sorted(proc[p], key=lambda s: (s['cls'], s['line'])):
        fn = modules[s['mod']]['file']
        L.append(f'- [`{s["cls"]}.{(s["inner"] + "." if s["inner"] else "")}{s["method"]}:{s["line"]}`]({fn}#{slug(s["cls"])}) · `{s["kind"]}` · {s.get("conn", "")}')
        L.append(f'  ```sql\n  {s["sql"][:600].replace(chr(10), " ")}\n  ```')
        if s['params']: L.append('  Params: ' + ' · '.join(f'{q["i"]} → `{q["expr"]}`' for q in s['params']))
        if s['outs']: L.append('  OUT: ' + ' · '.join(f'{o["i"]} ({o["type"]})' for o in s['outs']))
        if s['reads']: L.append('  Reads: ' + ', '.join(f'`{r}`' for r in s['reads']))
    L.append('')
open(f'{WORK}/sql/by-procedure.md', 'w').write('\n'.join(L) + '\n')
# unlinked
L = ['# SQL text not linked to a single call site', '', '[← work.md](../../work.md)', '',
     f'**{len(sql["orphans"])} SQL literals** that are not passed straight to a database call in the same method. Most are SQL prefixes kept as data and completed elsewhere: for example, the analysis-report procedure calls held in `defaulttables.fetchanalysisreportsettings*`, which the analysis fragments execute later with date parameters appended. The rest are statements whose text is assembled across branches or methods.', '',
     '| Class | Method | Line | SQL text |', '|---|---|---|---|']
for o in sorted(sql['orphans'], key=lambda o: (o['cls'], o['line'])):
    L.append(f'| `{o["cls"]}` | `{o["method"]}` | {o["line"]} | `{esc(o["text"])}` |')
open(f'{WORK}/sql/unlinked.md', 'w').write('\n'.join(L) + '\n')

# ---------- messages ----------
msgs = ui['messages']
bym = defaultdict(list)
for m_ in msgs: bym[cls_mod.get(m_['cls'], len(modules) - 1)].append(m_)
L = ['# User-facing messages', '', '[← work.md](../work.md)', '',
     f'Every literal toast, snackbar, dialog title/message/button, field error and hint set in code: **{len(msgs)}**, grouped by module and class. `${{…}}` marks values computed at runtime, and `@string/…` marks localized resources (see brain Appendix D for the English and Tamil text).', '']
for mi in sorted(bym):
    L.append(f'## {modules[mi]["num"]} {modules[mi]["title"]}'); L.append('')
    L.append('| Class | Kind | Text | Method:line |'); L.append('|---|---|---|---|')
    for m_ in sorted(bym[mi], key=lambda m_: (m_['cls'], m_['line'])):
        L.append(f'| `{m_["cls"]}` | {m_["kind"]} | {esc(m_["text"])} | `{m_["method"]}:{m_["line"]}` |')
    L.append('')
open(f'{WORK}/messages.md', 'w').write('\n'.join(L) + '\n')

# ---------- prefs ----------
pk = defaultdict(list)
for p in ui['prefs']: pk[p['key']].append(p)
L = ['# SharedPreferences keys', '', '[← work.md](../work.md)', '',
     f'**{len(pk)} keys**, mostly in the prefs file `venusmicrofinpref011`, with every reader and writer. Attribution is limited to genuine SharedPreferences / Editor objects, so Bundle and JSON getters are excluded. Values that hold credentials (DB user, password and connection strings) are stored in plain text. See brain §9.', '',
     '| Key | Type | Written by (value) | Read by (default) |', '|---|---|---|---|']
for k in sorted(pk, key=str.lower):
    ops = pk[k]; types = sorted(set(o['type'] for o in ops))
    w = [f'`{o["cls"]}.{o["method"]}:{o["line"]}` (`{o.get("value", "")}`)' for o in ops if o['op'] == 'write']
    r = [f'`{o["cls"]}.{o["method"]}:{o["line"]}`' + (f' (`{o["default"]}`)' if o.get('default') else '') for o in ops if o['op'] == 'read']
    L.append(f'| `{k}` | {", ".join(types)} | {"<br>".join(w[:12]) + (f"<br>… +{len(w) - 12}" if len(w) > 12 else "") or "—"} | {"<br>".join(r[:12]) + (f"<br>… +{len(r) - 12}" if len(r) > 12 else "") or "—"} |')
open(f'{WORK}/prefs.md', 'w').write('\n'.join(L) + '\n')

# ---------- extras ----------
put_by = defaultdict(list); get_by = defaultdict(list)
for e in ui['extras_put']: put_by[e['cls']].append(e)
for e in ui['extras_get']: get_by[e['cls']].append(e)
L = ['# Intent extras (screen contracts)', '', '[← work.md](../work.md)', '',
     f'For every class: the extras it **receives** (`get*Extra` / `getExtras().get*`) and the extras it **sends** with each `putExtra`, with the target screen when the Intent is created in the same class. {len(ui["extras_put"])} put sites, {len(ui["extras_get"])} get sites, {len(set(e["key"] for e in ui["extras_put"] + ui["extras_get"]))} distinct keys.', '']
for c in sorted(set(put_by) | set(get_by), key=str.lower):
    L.append(f'## {c}'); L.append('')
    if c in get_by:
        L.append('Receives: ' + ', '.join(sorted(set(f'`{e["key"]}` ({e["type"]})' for e in get_by[c]))))
    if c in put_by:
        L.append(''); L.append('| Sends key | Value | Target | Method:line |'); L.append('|---|---|---|---|')
        for e in sorted(put_by[c], key=lambda e: e['line']):
            L.append(f'| `{e["key"]}` | `{esc(e["value"])}` | {("`" + e["target"] + "`") if e.get("target") else "—"} | `{e["method"]}:{e["line"]}` |')
    L.append('')
open(f'{WORK}/extras.md', 'w').write('\n'.join(L) + '\n')

# ---------- layouts gallery + coverage ----------
standalone = {s[1]: s[0] for s in specs if s[5] == 'layout'}
inscreen = defaultdict(set)
for c, ls in used_by_screen.items():
    for l in ls: inscreen[l].add(c)
users = defaultdict(list)
for c, v in x.items():
    for l in v['layouts']: users[l].append(c)
L = ['# Layout images and coverage', '', '[← work.md](../work.md)', '',
     f'All **{len(applayouts)} app layouts**. Content layouts appear composed into their screen images (see the gallery in work.md). Rows, dialogs, sheets, include fragments and unused layouts are rendered on their own below. Every layout appears in at least one image.', '',
     '| Layout | Image | Inflated by | Shown inside screen images |', '|---|---|---|---|']
missing = []
libl = [l for l in applayouts if not os.path.exists(f'{S}/render/screens/src/main/res/layout/{l}.xml')]
for l in sorted(applayouts):
    if l in libl: continue
    img = ''
    nm = standalone.get(l)
    if nm and f'{nm}.png' in have:
        img = f'<a href="screens/{nm}.png"><img src="screens/{nm}.png" width="220"></a>'
    shown = sorted(inscreen.get(l, []))
    if not img and not shown: missing.append(l)
    L.append(f'| `{l}.xml` | {img or "—"} | {", ".join(f"`{u}`" for u in sorted(users.get(l, []))) or "— (not referenced in code)"} | {", ".join(f"[`{c}`](screens/{c}.png)" for c in shown[:8]) + (f" … +{len(shown) - 8}" if len(shown) > 8 else "") or "—"} |')
if libl:
    L += ['', 'Also packaged in the APK but owned by the Material library (not app layouts, so supplied by the library during rendering): ' + ', '.join(f'`{l}.xml`' for l in libl) + '.']
open(f'{WORK}/layouts.md', 'w').write('\n'.join(L) + '\n')

# ---------- assets ----------
L = ['# Image assets', '', '[← work.md](../work.md)', '',
     '## App icon and bundled images', '',
     '| Launcher icon | Round icon | `assets/image/img.png` (app logo) | `assets/image/ngx.jpg` (NGX printer sample) |', '|---|---|---|---|',
     f'| <img src="img/drawables/ic_launcher.png" width="96"> | <img src="img/drawables/ic_launcher_round.png" width="96"> | <img src="img/assets/img.png" width="160"> | <img src="img/assets/ngx.jpg" width="160"> |', '',
     f'## App drawables ({len(didx["index"])})', '',
     'The app\'s own drawables. Library drawables (AppCompat, Material) are excluded. Rasters are shown at their highest packaged density, vectors are converted to SVG with the app\'s colours resolved, and shapes, selectors and layer-lists are rendered as 160×56 dp swatches by Android layoutlib.', '',
     '| Name | Kind | Preview | Densities / source |', '|---|---|---|---|']
sw = set(os.listdir(WORK + '/img/swatches'))
for d in sorted(didx['index'], key=lambda d: d['name'].lower()):
    if d.get('file'):
        prev = f'<img src="img/drawables/{d["file"]}" height="48">'
    elif d['name'] + '.png' in sw:
        prev = f'<img src="img/swatches/{d["name"]}.png" height="40">'
    else:
        prev = '—'
    L.append(f'| `{d["name"]}` | {d["kind"]} | {prev} | {", ".join(d.get("variants", [d["from"]]))} |')
open(f'{WORK}/assets.md', 'w').write('\n'.join(L) + '\n')

# ---------- tools ----------
for f in ('xref.py', 'sqlextract.py', 'uiextract.py', 'screenmap.py', 'compose.py', 'appres.py', 'postimg.py', 'drawables.py', 'workgen.py'):
    p = S + '/scripts/' + f
    if os.path.exists(p): shutil.copy(p, f'{WORK}/tools/{f}')
for f in ('build.gradle.kts', 'settings.gradle.kts', 'gradle.properties'):
    shutil.copy(S + '/render/' + f, f'{WORK}/tools/render-{f}')
shutil.copy(S + '/render/screens/build.gradle.kts', f'{WORK}/tools/render-screens-build.gradle.kts')
shutil.copy(S + '/render/screens/src/test/kotlin/render/RenderAll.kt', f'{WORK}/tools/RenderAll.kt')

# ---------- work.md ----------
kinds = Counter(s['kind'] for s in st); ops = Counter(s['op'] for s in st)
n_loc = sum(1 for s in st if s['db'] == 'local SQLite'); n_rem = len(st) - n_loc
screens = [s for s in specs if s[5] in ('activity', 'fragment', 'drawer') and s[4] == '']
n_img = len([f for f in os.listdir(WORK + '/screens') if f.endswith('.png')])
L = ['# work.md — FinSoft II full extraction: every SQL query, every screen image, assets and runtime keys', '',
     '> Companion to [brain.md](brain.md), which holds the functional specification. This file and [`work/`](work/) hold the raw extraction from `base1.apk`:',
     f'> **{len(st)} SQL statements** (every one, in full) · **{n_img} screen and layout images** rendered by Android\'s own layout engine (English and Tamil) · '
     f'**{len(didx["index"])} drawables** · **{len(msgs)} UI messages** · **{len(pk)} SharedPreferences keys** · **{len(set(e["key"] for e in ui["extras_put"] + ui["extras_get"]))} intent-extra keys** · **{len(set(u["url"] for u in ui["urls"]))} URLs**.', '',
     '## Contents', '',
     '1. [How this was produced](#1-how-this-was-produced)', '2. [Screen gallery (every screen)](#2-screen-gallery-every-screen)',
     '3. [SQL catalogue (every query)](#3-sql-catalogue-every-query)', '4. [Messages, preferences, intent extras, URLs](#4-messages-preferences-intent-extras-urls)',
     '5. [Layouts and assets](#5-layouts-and-assets)', '6. [Reproducing](#6-reproducing)', '',
     '## 1. How this was produced', '',
     '| Output | Method |', '|---|---|',
     '| SQL catalogue | Each decompiled class (JADX 1.5.5) was parsed into a Java syntax tree with tree-sitter. Every database call was located: SQLite `rawQuery`, `execSQL`, `compileStatement` and the ContentValues helpers `insert`/`insertOrThrow`/`update`/`delete`; JDBC `prepareStatement`, `prepareCall` and `Statement.execute*`. Each statement\'s full text was rebuilt by resolving string concatenation, local variables, `+=` and `StringBuilder.append`. Parts that are not literals are shown as `${expression}`. Bound parameters (`setString`/`bindString`…), `registerOutParameter` and the columns read back (`getColumnIndex`, `rs.getX("col")`) are linked to each statement. Only 1 of the statements could not be resolved to text |',
     '| Screen images | The app\'s own resources (3,514 app-owned resource entries, separated from library resources using every dependency AAR\'s `R.txt`) were compiled with the **same library versions as the APK** (Material 1.14.0, AppCompat 1.7.1, ConstraintLayout 2.2.1, RecyclerView 1.4.0). Every layout was rendered with **Android layoutlib** through Paparazzi 2.0 (AGP 9.0, Pixel 5 device profile). For each screen, `<include>`s are inlined, lists are filled with three sample rows of the real row layout, ViewPagers show their fragment layout, and screens with an action-bar theme get their toolbar with the title the code sets. Text is the layout\'s own placeholder text; no live data. Images are 540 px wide |',
     '| Messages / prefs / extras | The same syntax-tree pass: `Toast.makeText`, `Snackbar.make`, dialog `setTitle`/`setMessage`/buttons, `setError`, `setHint`; SharedPreferences get/put calls on real prefs and Editor objects; `putExtra` linked to the Intent\'s target class; `get*Extra` / `getExtras().get*` |',
     '| Drawables | Vectors converted to SVG (app colours resolved), rasters taken at the highest packaged density, shapes and selectors rendered by layoutlib |', '',
     'The app was never executed. The images show each layout as Android draws it, before the code fills in data. Some views that the code shows or hides at runtime may differ.', '',
     '## 2. Screen gallery (every screen)', '',
     f'**{len(screens)} screens** ({sum(1 for s in screens if s[5] == "activity")} activities, {sum(1 for s in screens if s[5] == "fragment")} fragments and the open navigation drawer), each shown in English and Tamil. Click an image for full size. **Spec** links to the screen\'s functional specification in brain.md, and **SQL** to its queries. Two screens, `executiveattendance_displayboard` and `weeklycollectionpatti_dashboard`, build their whole UI in code from server data and have no XML layout to render.', '']
spec_by_cls = {s[6] or s[0]: s for s in screens}
for mi, m in enumerate(modules):
    members = [c for c in spec_by_cls if cls_mod.get(c, -1) == mi]
    if mi == cls_mod.get('navigationdrawactivity') and 'navigation_drawer_open' in [s[0] for s in screens]:
        pass
    if not members: continue
    members.sort(key=lambda c: (cls_anchor.get(c, ('', '', c))[2] or c).lower())
    L.append(f'### {m["num"]} {m["title"]}'); L.append('')
    L.append('| Screen | English | தமிழ் (Tamil) |'); L.append('|---|---|---|')
    for c in members:
        s = spec_by_cls[c]; name = s[0]
        title = cls_anchor.get(c, (None, None, None))[2] or name
        if name == 'navigation_drawer_open': title = 'Navigation drawer (open)'
        en = f'<a href="work/screens/{name}.png"><img src="work/screens/{name}.png" width="230"></a>' if f'{name}.png' in have else '—'
        ta = f'<a href="work/screens/{name}.ta.png"><img src="work/screens/{name}.ta.png" width="230"></a>' if f'{name}.ta.png' in have else '—'
        lk = [f'[Spec]({where_link(c)[6:]})']
        if mi in sqlfiles and any(x_['cls'] == c for x_ in bymod[mi]): lk.append(f'[SQL](work/sql/{sqlfiles[mi][0]}#{slug(c)})')
        kind = smap.get(c, {}).get('kind', 'drawer')
        L.append(f'| **{title}**<br>`{c}`<br>{kind}<br>{" · ".join(lk)} | {en} | {ta} |')
    L.append('')
L += ['## 3. SQL catalogue (every query)', '',
      f'**{len(st)} statements**: {n_loc} on the local SQLite database `venusmicrofinv005` and {n_rem} on SQL Server (via jTDS), plus {len(sql["orphans"])} SQL fragments that are completed elsewhere ([unlinked](work/sql/unlinked.md)). '
      f'They touch {len(tab)} tables and call {len(proc)} stored procedures.', '',
      '| By execution method | Count | By operation | Count |', '|---|---|---|---|']
kk = kinds.most_common(); oo = ops.most_common()
for i in range(max(len(kk), len(oo))):
    a = kk[i] if i < len(kk) else ('', ''); b = oo[i] if i < len(oo) else ('', '')
    L.append(f'| {("`" + a[0] + "`") if a[0] else ""} | {a[1]} | {b[0].upper()} | {b[1]} |')
L += ['', '**Indexes:** [SQL by table](work/sql/by-table.md) · [Stored procedures, every call site](work/sql/by-procedure.md) · [Unlinked SQL fragments](work/sql/unlinked.md)', '',
      '**Per module** (same module numbering as brain.md):', '', '| Module | Statements | Local | Remote |', '|---|---|---|---|']
for mi, (fn, n) in sorted(sqlfiles.items()):
    items = bymod[mi]
    L.append(f'| [{modules[mi]["num"]} {modules[mi]["title"]}](work/sql/{fn}) | {n} | {sum(1 for s in items if s["db"] == "local SQLite")} | {sum(1 for s in items if s["db"] == "SQL Server")} |')
L += ['', '## 4. Messages, preferences, intent extras, URLs', '',
      f'- [**{len(msgs)} user-facing messages**](work/messages.md): toasts, dialogs, field errors and hints, per class',
      f'- [**{len(pk)} SharedPreferences keys**](work/prefs.md), with every reader and writer',
      f'- [**Intent extras**](work/extras.md): what each screen receives and sends ({len(ui["extras_put"])} put / {len(ui["extras_get"])} get sites)', '',
      '**URLs found in code**', '', '| URL | Used in |', '|---|---|']
uu = defaultdict(list)
for u in ui['urls']: uu[u['url']].append(f'`{u["cls"]}.{u["method"]}:{u["line"]}`')
for u in sorted(uu):
    L.append(f'| `{esc(u)}` | {", ".join(sorted(set(uu[u])))} |')
L += ['', '## 5. Layouts and assets', '',
      f'- [**Layout images and coverage**](work/layouts.md): all {len(applayouts) - len(libl)} app layouts. Rows, dialogs, sheets and includes are rendered on their own, and each layout is mapped to the screen images that contain it' + (f' ({len(missing)} not shown anywhere)' if missing else ' (every layout appears in at least one image)'),
      f'- [**Image assets**](work/assets.md): launcher icons, bundled images and all {len(didx["index"])} app drawables', '',
      '## 6. Reproducing', '',
      'The extraction scripts and the render project are in [`work/tools/`](work/tools/): `sqlextract.py` (SQL), `uiextract.py` (messages, prefs, extras, URLs), `screenmap.py` + `compose.py` (screen composition), `appres.py` (app-only resources), `RenderAll.kt` and the Gradle files (Paparazzi render), `postimg.py`, `drawables.py`, `workgen.py`. They need JADX and apktool output from the APK, Python 3 with tree-sitter-java and lxml, and an Android SDK (platform 36) with Gradle 9.1. Decompiled application source is not included in this repository.', '']
open(REPO + '/work.md', 'w').write('\n'.join(L) + '\n')
print('work.md', os.path.getsize(REPO + '/work.md') // 1024, 'KB | sql files', len(sqlfiles), '| images', n_img, '| layouts not shown anywhere:', missing)
