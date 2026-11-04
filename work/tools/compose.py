#!/usr/bin/env python3
"""Prepare render-ready layouts for Paparazzi and write the render spec.

* fixes custom/3rd-party view tags in every app layout of the render project (in place)
* composes one zz_<screen>.xml per activity / fragment: includes inlined, lists replaced by 3 sample
  rows of the adapter's row layout (adapters in code order), ViewPagers filled with the fragment layout,
  and a synthetic action bar for screens whose theme has one
* writes specs.tsv: name, layout, theme, rendering mode, locale, kind, class
usage: compose.py <scratchpad>
"""
import copy, json, os, re, sys
from lxml import etree
S = sys.argv[1]
RES = S + '/render/screens/src/main/res'
LAY = RES + '/layout'
APK = S + '/out/apktool'
AN = 'http://schemas.android.com/apk/res/android'
AP = 'http://schemas.android.com/apk/res-auto'
def A(n): return '{%s}%s' % (AN, n)
def P(n): return '{%s}%s' % (AP, n)
smap = json.load(open(S + '/work/screenmap.json'))
applayouts = open(S + '/work/applayouts.txt').read().split()
strings = {}
for s in etree.parse(APK + '/res/values/strings.xml').getroot():
    if s.tag == 'string':
        strings[s.get('name')] = ''.join(s.itertext())

LISTS = ('RecyclerView', 'ListView', 'ExpandableListView', 'GridView', 'CustomListView')
PAGERS = ('ViewPager', 'ViewPager2')
def local(tag):
    return tag.split('.')[-1] if isinstance(tag, str) else ''

# ---- 1. fix custom tags in place (all app layouts, so includes and rows render too) ----
REPL = {'com.mikhaellopez.circularimageview.CircularImageView': 'ImageView',
        'com.jsibbold.zoomage.ZoomageView': 'ImageView',
        'net.venussolutions.venusmicrofindailyweeklycollection.CustomListView': 'ListView'}
fixed = 0
for l in applayouts:
    p = f'{LAY}/{l}.xml'
    if not os.path.exists(p):
        continue
    tree = etree.parse(p); changed = False
    for el in tree.getroot().iter():
        if not isinstance(el.tag, str):
            continue
        if el.tag in REPL:
            el.tag = REPL[el.tag]; changed = True
        elif el.tag == 'fragment':
            el.tag = 'FrameLayout'
            for k in list(el.attrib):
                if k in (A('name'), 'class', A('tag')):
                    del el.attrib[k]
            el.set(A('background'), '#FFDCE3E8')
            tv = etree.SubElement(el, 'TextView')
            tv.set(A('layout_width'), 'wrap_content'); tv.set(A('layout_height'), 'wrap_content')
            tv.set(A('layout_gravity'), 'center'); tv.set(A('text'), 'Google Map (SupportMapFragment)')
            tv.set(A('textColor'), '#FF455A64'); tv.set(A('textSize'), '16sp')
            changed = True
    if changed:
        tree.write(p, encoding='utf-8', xml_declaration=True); fixed += 1
print('layouts with custom tags fixed:', fixed)

USED = set()
def load(name):
    p = f'{LAY}/{name}.xml'
    if os.path.exists(p):
        USED.add(name)
        return etree.parse(p).getroot()
    return None

def inline(el, depth=0):
    if depth > 6:
        return
    for ch in list(el):
        if not isinstance(ch.tag, str):
            continue
        if ch.tag == 'include':
            ref = (ch.get('layout') or '').replace('@layout/', '')
            root = load(ref)
            if root is None:
                continue
            parent = ch.getparent(); idx = parent.index(ch)
            if root.tag == 'merge':
                parent.remove(ch)
                for k, sub in enumerate([c for c in root if isinstance(c.tag, str)]):
                    c2 = copy.deepcopy(sub); parent.insert(idx + k, c2); inline(c2, depth + 1)
            else:
                c2 = copy.deepcopy(root)
                for k, v in ch.attrib.items():
                    if k.startswith(A('layout_')) or k == A('id') or k == A('visibility'):
                        c2.set(k, v)
                parent.replace(ch, c2); inline(c2, depth + 1)
        else:
            inline(ch, depth)

def rows_for(cls, seen=None):
    """row layouts of the adapters a class uses, in code order"""
    out = []
    for a in smap.get(cls, {}).get('adapters', []):
        out += smap.get(a, {}).get('rows', [])
    return out

def make_list(el, row):
    lin = etree.Element('LinearLayout', nsmap={'android': AN, 'app': AP})
    for k, v in el.attrib.items():
        if k.startswith(A('layout_')) or k in (A('id'), A('background'), A('padding'), A('visibility')):
            lin.set(k, v)
    lin.set(A('orientation'), 'vertical')
    if row:
        for _ in range(3):
            inc = etree.SubElement(lin, 'include'); inc.set('layout', '@layout/' + row)
            inc.set(A('layout_width'), 'match_parent'); inc.set(A('layout_height'), 'wrap_content')
    return lin

def compose(cls, base, frag_layouts, title, toolbar):
    root = load(base)
    if root is None:
        return None
    root = copy.deepcopy(root)
    if root.tag == 'merge':
        fr = etree.Element('FrameLayout', nsmap={'android': AN, 'app': AP})
        fr.set(A('layout_width'), 'match_parent'); fr.set(A('layout_height'), 'match_parent')
        for c in list(root): fr.append(c)
        root = fr
    holder = etree.Element('FrameLayout', nsmap={'android': AN, 'app': AP}); holder.append(root)
    inline(holder)
    rows = rows_for(cls); ri = 0
    fr_i = 0
    for el in list(holder.iter()):
        if not isinstance(el.tag, str) or el is holder:
            continue
        t = local(el.tag)
        if t in LISTS:
            row = rows[min(ri, len(rows) - 1)] if rows else None; ri += 1
            new = make_list(el, row)
            el.getparent().replace(el, new)
        elif t in PAGERS and frag_layouts:
            fl = frag_layouts[min(fr_i, len(frag_layouts) - 1)]; fr_i += 1
            new = etree.Element('FrameLayout', nsmap={'android': AN, 'app': AP})
            for k, v in el.attrib.items():
                if k.startswith(A('layout_')) or k == A('id'):
                    new.set(k, v)
            sub = load(fl)
            if sub is not None:
                sub = copy.deepcopy(sub)
                if sub.tag == 'merge':
                    for c in list(sub): new.append(c)
                else:
                    sub.set(A('layout_width'), 'match_parent'); sub.set(A('layout_height'), 'match_parent'); new.append(sub)
                inline(new)
                # lists inside the fragment layout use the fragment's adapters
                frag_cls = next((f for f in smap if smap[f].get('content') == fl and smap[f]['kind'] == 'fragment'), None)
                frows = rows_for(frag_cls) if frag_cls else rows_for(cls)   # inner-class fragments use the activity's adapters
                fi = 0
                for e2 in list(new.iter()):
                    if isinstance(e2.tag, str) and local(e2.tag) in LISTS:
                        row = frows[min(fi, len(frows) - 1)] if frows else None; fi += 1
                        e2.getparent().replace(e2, make_list(e2, row))
            el.getparent().replace(el, new)
    content = holder[0]
    if toolbar:
        lin = etree.Element('LinearLayout', nsmap={'android': AN, 'app': AP})
        lin.set(A('layout_width'), 'match_parent'); lin.set(A('layout_height'), 'match_parent'); lin.set(A('orientation'), 'vertical')
        tb = etree.SubElement(lin, 'androidx.appcompat.widget.Toolbar')
        tb.set(A('layout_width'), 'match_parent'); tb.set(A('layout_height'), '?attr/actionBarSize')
        tb.set(A('background'), '?attr/colorPrimary'); tb.set(P('title'), title); tb.set(P('titleTextColor'), '@android:color/white')
        tb.set(A('elevation'), '4dp')
        content.set(A('layout_width'), 'match_parent'); content.set(A('layout_height'), '0dp'); content.set(A('layout_weight'), '1')
        lin.append(content)
        content = lin
    else:
        content.set(A('layout_width'), 'match_parent')
    return content

SRCDIR = S + '/out/jadx/sources/net/venussolutions/venusmicrofindailyweeklycollection'
def code_title(cls):
    pth = f'{SRCDIR}/{cls}.java'
    if not os.path.exists(pth):
        return None
    src = open(pth, encoding='utf-8', errors='replace').read()
    m = re.search(r'setTitle\(\s*"([^"]{2,60})"', src)
    if m:
        return m.group(1)
    m = re.search(r'setTitle\(\s*(?:getString|getResources\(\)\.getString)\(\s*[\w.]*R\.string\.(\w+)', src)
    if m:
        return strings.get(m.group(1))
    return None

def set_toolbar_titles(root, title):
    for el in root.iter():
        if isinstance(el.tag, str) and local(el.tag) in ('Toolbar', 'MaterialToolbar') and not el.get(P('title')):
            el.set(P('title'), title)
            if not el.get(P('titleTextColor')):
                el.set(P('titleTextColor'), '@android:color/white')

def label_text(lab):
    if not lab:
        return strings.get('app_name', 'FinSoft II')
    if lab.startswith('@string/'):
        return strings.get(lab[8:], lab[8:])
    return lab.strip()

specs = []; composed = set(); used_by_screen = {}
for cls, m in sorted(smap.items()):
    if m['kind'] not in ('activity', 'fragment') or not m.get('content'):
        continue
    theme = m.get('theme') or 'AppTheme'
    if theme == 'Theme.NoTitleBar':
        theme = 'AppTheme.NoActionBar'
    toolbar = m['kind'] == 'activity' and theme == 'AppTheme'
    frag_layouts = [smap[f]['content'] for f in m.get('fragments', []) if smap.get(f, {}).get('content')]
    if not frag_layouts:
        own_rows = set(rows_for(cls))
        frag_layouts = [l for l in m.get('inflates', []) if l.startswith('fragment_') and l not in own_rows]
    title = code_title(cls) or label_text(m.get('label'))
    USED.clear()
    root = compose(cls, m['content'], frag_layouts, title, toolbar)
    used_by_screen[cls] = sorted(USED | set(re.findall(r'@layout/(\w+)', etree.tostring(root).decode()) if root is not None else []))
    if root is None:
        continue
    set_toolbar_titles(root, title)
    name = 'zz_' + cls.lower()
    etree.ElementTree(root).write(f'{LAY}/{name}.xml', encoding='utf-8', xml_declaration=True, pretty_print=True)
    rtheme = 'AppTheme.NoActionBar' if toolbar else theme
    has_drawer = any(isinstance(e.tag, str) and local(e.tag) == 'DrawerLayout' for e in root.iter())
    specs.append((cls, name, rtheme, 'NORMAL' if has_drawer else 'V_SCROLL', '', m['kind'], cls))
    composed.add(m['content'])
# every app layout on its own (rows / dialogs / sheets / includes / unused) — screens' content layouts already covered
for l in applayouts:
    if l in composed or not os.path.exists(f'{LAY}/{l}.xml'):
        continue
    root = load(l)
    h = root.get(A('layout_height')) if root is not None else None
    mode = 'SHRINK' if h == 'wrap_content' or root is None or root.tag == 'merge' else 'NORMAL'
    if root is not None and root.tag == 'merge':
        continue
    specs.append(('layout_' + l, l, 'AppTheme.NoActionBar', mode, '', 'layout', ''))
# synthetic: navigation drawer open
nav = etree.Element('FrameLayout', nsmap={'android': AN, 'app': AP})
nav.set(A('layout_width'), 'match_parent'); nav.set(A('layout_height'), 'match_parent'); nav.set(A('background'), '#99000000')
nv = etree.SubElement(nav, 'com.google.android.material.navigation.NavigationView')
nv.set(A('layout_width'), '300dp'); nv.set(A('layout_height'), 'match_parent'); nv.set(A('background'), '@android:color/white')
nv.set(P('headerLayout'), '@layout/nav_header_main2'); nv.set(P('menu'), '@menu/activity_navigation_drawer')
etree.ElementTree(nav).write(f'{LAY}/zz_navigation_drawer_open.xml', encoding='utf-8', xml_declaration=True, pretty_print=True)
specs.append(('navigation_drawer_open', 'zz_navigation_drawer_open', 'AppTheme.NoActionBar', 'V_SCROLL', '', 'drawer', 'navigationdrawactivity'))
# drawable swatches for XML drawables that are not plain vectors/rasters
didx = json.load(open(S + '/work/out_images/drawables/_index.json'))
for d in didx['swatch']:
    nm = d['name']
    sw = etree.Element('LinearLayout', nsmap={'android': AN, 'app': AP})
    sw.set(A('layout_width'), 'wrap_content'); sw.set(A('layout_height'), 'wrap_content'); sw.set(A('padding'), '8dp'); sw.set(A('background'), '#FFFFFFFF')
    v = etree.SubElement(sw, 'View'); v.set(A('layout_width'), '160dp'); v.set(A('layout_height'), '56dp'); v.set(A('background'), '@drawable/' + nm)
    etree.ElementTree(sw).write(f'{LAY}/zz_dr_{nm.lower()}.xml', encoding='utf-8', xml_declaration=True, pretty_print=True)
    specs.append(('dr_' + nm, 'zz_dr_' + nm.lower(), 'AppTheme.NoActionBar', 'SHRINK', '', 'drawable', ''))
json.dump(used_by_screen, open(S + '/work/screen_layouts.json', 'w'), indent=1)
# Tamil variants of every composed screen + drawer
ta = [(n + '__ta', l, t, md, 'ta', k, c) for (n, l, t, md, loc, k, c) in specs if k in ('activity', 'fragment', 'drawer')]
allspecs = specs + ta
os.makedirs(S + '/render/screens/src/test/resources', exist_ok=True)
with open(S + '/render/screens/src/test/resources/specs.tsv', 'w') as f:
    for s in allspecs:
        f.write('\t'.join(s) + '\n')
json.dump(allspecs, open(S + '/work/render_specs.json', 'w'), indent=1)
from collections import Counter
print('specs:', len(allspecs), Counter(s[5] for s in specs), '+', len(ta), 'Tamil')
