#!/usr/bin/env python3
"""Map every screen class to its content layout, adapters (row layouts) and fragments, in code order."""
import json, os, re, sys
S = sys.argv[1]
SRC = S + '/out/jadx/sources/net/venussolutions/venusmicrofindailyweeklycollection'
x = json.load(open(S + '/work/xref.json'))
man = open(S + '/out/apktool/AndroidManifest.xml').read()
RL = r'(?:[\w.]*\.)?R\.layout\.(\w+)'
def src(c):
    p = os.path.join(SRC, c + '.java')
    return open(p, encoding='utf-8', errors='replace').read() if os.path.exists(p) else ''
kind = {}
for c, v in x.items():
    e = v['extends'].split('<')[0]
    if e in ('AppCompatActivity', 'Activity', 'FragmentActivity'): kind[c] = 'activity'
    elif 'Fragment' in e: kind[c] = 'fragment'
    elif 'Adapter' in e or 'Adapter' in v['implements'] or e in ('BaseAdapter', 'ArrayAdapter', 'BaseExpandableListAdapter', 'RecyclerView.Adapter'): kind[c] = 'adapter'
    elif e in ('Service', 'IntentService'): kind[c] = 'service'
    elif e == 'RelativeLayout': kind[c] = 'view'
adapters = [c for c, k in kind.items() if k == 'adapter']
fragments = [c for c, k in kind.items() if k == 'fragment']
out = {}
for c, k in kind.items():
    t = src(c)
    content = re.findall(r'setContentView\(\s*' + RL, t)
    inflates = re.findall(r'inflate\(\s*' + RL, t)
    row = inflates if k == 'adapter' else []
    uses_ad = []
    for a in adapters:
        m = re.search(r'new\s+' + re.escape(a) + r'\s*\(', t)
        if m and a != c: uses_ad.append((m.start(), a))
    uses_fr = []
    for f in fragments:
        m = re.search(r'\b' + re.escape(f) + r'\b\s*(\.newInstance|\()', t)
        if m and f != c: uses_fr.append((m.start(), f))
    theme = None; label = None; exported = None
    m = re.search(r'<activity[^>]*android:name="net\.venussolutions\.venusmicrofindailyweeklycollection\.' + re.escape(c) + r'"[^>]*>', man)
    if m:
        tag = m.group(0)
        tm = re.search(r'android:theme="@(?:style|android:style)/([^"]+)"', tag); theme = tm.group(1) if tm else None
        lm = re.search(r'android:label="([^"]+)"', tag); label = lm.group(1) if lm else None
    out[c] = {'kind': k, 'content': content[0] if content else (inflates[0] if k in ('fragment', 'view') and inflates else None),
              'inflates': sorted(set(inflates)), 'rows': row, 'adapters': [a for _, a in sorted(uses_ad)],
              'fragments': [f for _, f in sorted(uses_fr)], 'theme': theme, 'label': label, 'in_manifest': bool(m)}
json.dump(out, open(S + '/work/screenmap.json', 'w'), indent=1)
acts = [c for c in out if out[c]['kind'] == 'activity']
print('activities', len(acts), '| without setContentView layout:', [c for c in acts if not out[c]['content']])
print('fragments', len(fragments), '| adapters', len(adapters), '| adapters without row layout:', [a for a in adapters if not out[a]['rows']])
print('themes:', sorted(set(str(out[c]['theme']) for c in acts)))
