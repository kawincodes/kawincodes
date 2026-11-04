#!/usr/bin/env python3
"""Build a mechanical cross-reference of every app class in the decompiled APK."""
import json, os, re, sys

SRC = sys.argv[1]   # .../jadx/sources/net/venussolutions
OUT = sys.argv[2]   # output json

RX = {
    'extends': re.compile(r'\bclass\s+\w+(?:<[^>]*>)?\s+extends\s+([\w.<>, ?]+?)(?:\s+implements\s+([\w.<>, ?]+?))?\s*\{'),
    'impl_only': re.compile(r'\bclass\s+\w+\s+implements\s+([\w.<>, ?]+?)\s*\{'),
    'iface': re.compile(r'\binterface\s+(\w+)'),
    'layout': re.compile(r'R\.layout\.(\w+)'),
    'menu': re.compile(r'R\.menu\.(\w+)'),
    'intent': re.compile(r'\(Class<\?>\)\s*(\w+)\.class'),
    'string': re.compile(r'"((?:[^"\\]|\\.)*)"'),
    'prefs_get': re.compile(r'\.get(?:String|Int|Boolean|Long|Float)\("([^"]+)"'),
    'prefs_put': re.compile(r'\.put(?:String|Int|Boolean|Long|Float)\("([^"]+)"'),
    'extra_put': re.compile(r'\.putExtra\("([^"]+)"'),
    'extra_get': re.compile(r'\.get(?:String|Int|Boolean|Double|Long|Serializable|Parcelable)Extra\("([^"]+)"'),
    'method': re.compile(r'^\s{4}(?:public|private|protected|static|final|synchronized|\s)*[\w<>\[\],. ?]+\s+(\w+)\s*\([^;{]*\)\s*(?:throws [\w., ]+)?\s*\{', re.M),
    'inner': re.compile(r'^\s{4,}(?:public |private |protected |static |final |abstract )*class\s+(\w+)', re.M),
    'navid': re.compile(r'R\.id\.(nav_\w+)'),
    'toast': re.compile(r'Toast\.makeText\([^,]+,\s*"((?:[^"\\]|\\.)*)"'),
}
SQLKW = re.compile(r'^\s*(select|insert|update|delete|exec|execute|create|drop|alter|with|declare|begin|if\s|truncate|merge|\{call|set\s)', re.I)
SQLANY = re.compile(r'\b(select\s.+\sfrom|insert\s+into|update\s+\w+\s+set|delete\s+from|exec(?:ute)?\s+\w+|\{call\s|create\s+table|drop\s+table)', re.I)
PROC = re.compile(r'(?:\bexec(?:ute)?|\{\s*(?:\?\s*=\s*)?call)\s+\[?(?:dbo\]?\.)?\[?(\w+)', re.I)
TABLE = re.compile(r'\b(?:from|join|into|update|table(?:\s+if\s+not\s+exists)?)\s+\[?(?:dbo\]?\.)?\[?([A-Za-z_]\w*)', re.I)
URL = re.compile(r'https?://[^\s"\']+')

res = {}
for root, _, files in os.walk(SRC):
    for f in files:
        if not f.endswith('.java'):
            continue
        p = os.path.join(root, f)
        rel = os.path.relpath(p, SRC)
        name = f[:-5]
        if name == 'R':
            continue
        txt = open(p, encoding='utf-8', errors='replace').read()
        d = {'file': rel, 'bytes': len(txt), 'lines': txt.count('\n')}
        m = RX['extends'].search(txt)
        if m:
            d['extends'] = m.group(1).strip(); d['implements'] = (m.group(2) or '').strip()
        else:
            m = RX['impl_only'].search(txt)
            d['extends'] = ''; d['implements'] = m.group(1).strip() if m else ''
            if not m and RX['iface'].search(txt):
                d['extends'] = 'interface'
        d['layouts'] = sorted(set(RX['layout'].findall(txt)))
        d['menus'] = sorted(set(RX['menu'].findall(txt)))
        d['intents'] = sorted(set(x for x in RX['intent'].findall(txt) if x != name))
        d['navids'] = sorted(set(RX['navid'].findall(txt)))
        strs = RX['string'].findall(txt)
        sql = []
        for s in strs:
            if len(s) > 12 and (SQLKW.match(s) or SQLANY.search(s)):
                sql.append(s)
        # concatenated SQL fragments also count
        d['sql_count'] = len(sql)
        d['procs'] = sorted(set(p_.lower() for s in sql for p_ in PROC.findall(s)))
        tabs = set()
        for s in sql:
            for t in TABLE.findall(s):
                if t.lower() not in ('select', 'where', 'set', 'values', 'the', 'if', 'not', 'exists', 'as', 'on'):
                    tabs.add(t)
        d['tables'] = sorted(tabs, key=str.lower)
        d['sql_samples'] = sql[:400]
        d['urls'] = sorted(set(u for s in strs for u in URL.findall(s)))
        d['prefs_get'] = sorted(set(RX['prefs_get'].findall(txt)))
        d['prefs_put'] = sorted(set(RX['prefs_put'].findall(txt)))
        d['extras_put'] = sorted(set(RX['extra_put'].findall(txt)))
        d['extras_get'] = sorted(set(RX['extra_get'].findall(txt)))
        d['methods'] = sorted(set(RX['method'].findall(txt)) - {'if', 'for', 'while', 'switch', 'catch', 'synchronized'})
        d['inner_classes'] = sorted(set(RX['inner'].findall(txt)))
        d['toasts'] = sorted(set(RX['toast'].findall(txt)))[:60]
        d['decompile_issue'] = bool(re.search(r'Code decompiled incorrectly|JADX ERROR|Method dump skipped', txt))
        res[name] = d

json.dump(res, open(OUT, 'w'), indent=1, ensure_ascii=False)
print('classes', len(res))
