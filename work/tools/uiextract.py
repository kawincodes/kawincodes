#!/usr/bin/env python3
"""AST extraction of user-facing messages, SharedPreferences keys, intent extras and URLs.

usage: uiextract.py <app-source-root> <out.json>
"""
import json, os, re, sys
import tree_sitter_java as tsj
from tree_sitter import Language, Parser
PARSER = Parser(Language(tsj.language()))
SRC, OUT = sys.argv[1], sys.argv[2]

def txt(n): return n.text.decode('utf-8', 'replace')
def unescape(lit):
    s = lit[1:-1]; out = []; i = 0
    while i < len(s):
        if s[i] == '\\' and i + 1 < len(s):
            nx = s[i + 1]
            if nx == 'u':
                try: out.append(chr(int(s[i + 2:i + 6], 16))); i += 6; continue
                except ValueError: pass
            out.append({'n': '\n', 't': '\t', '"': '"', "'": "'", '\\': '\\'}.get(nx, nx)); i += 2; continue
        out.append(s[i]); i += 1
    return ''.join(out)
def compact(s, n=70):
    s = re.sub(r'\s+', ' ', s); s = re.sub(r'\b[\w$]+\.this\.', '', s).replace('this.', '')
    s = re.sub(r'net\.venussolutions\.venusmicrofindailyweeklycollection\.(clone\.)?', '', s)
    return s if len(s) <= n else s[:n - 1] + '…'
def render(n, depth=0):
    if n is None: return ''
    t = n.type
    if t == 'string_literal': return unescape(txt(n))
    if t == 'binary_expression' and n.child_by_field_name('operator') is not None and txt(n.child_by_field_name('operator')) == '+':
        return render(n.child_by_field_name('left'), depth + 1) + render(n.child_by_field_name('right'), depth + 1)
    if t == 'parenthesized_expression':
        c = [x for x in n.children if x.is_named]; return render(c[0], depth + 1) if c else ''
    if t == 'method_invocation':
        nm = n.child_by_field_name('name')
        if nm is not None and txt(nm) in ('getString', 'getText') and 'R.string.' in txt(n):
            m = re.search(r'R\.string\.(\w+)', txt(n)); return '@string/' + m.group(1)
    return '${' + compact(txt(n)) + '}'
def call_parts(inv):
    obj = inv.child_by_field_name('object'); name = inv.child_by_field_name('name'); args = inv.child_by_field_name('arguments')
    return obj, (txt(name) if name is not None else ''), ([c for c in args.children if c.is_named and 'comment' not in c.type] if args is not None else [])
def enclosing_method(n):
    p = n.parent
    while p is not None:
        if p.type in ('method_declaration', 'constructor_declaration'):
            nm = p.child_by_field_name('name'); return txt(nm) if nm is not None else '<init>'
        p = p.parent
    return '<field>'

MSG_FIRST = {'setTitle', 'setMessage', 'setError', 'setPositiveButton', 'setNegativeButton', 'setNeutralButton', 'setHint'}
messages, prefs, extras_put, extras_get, urls = [], [], [], [], []
PREFS_GET = {'getString', 'getInt', 'getBoolean', 'getLong', 'getFloat', 'getStringSet', 'contains'}
PREFS_PUT = {'putString', 'putInt', 'putBoolean', 'putLong', 'putFloat', 'putStringSet', 'remove'}
EXTRA_GET = {'getStringExtra', 'getIntExtra', 'getBooleanExtra', 'getDoubleExtra', 'getLongExtra', 'getFloatExtra',
             'getSerializableExtra', 'getParcelableExtra', 'getStringArrayListExtra', 'getIntegerArrayListExtra', 'getParcelableArrayListExtra'}
BUNDLE_GET = {'getString', 'getInt', 'getBoolean', 'getDouble', 'getLong', 'getFloat', 'getSerializable', 'getParcelable', 'getStringArrayList'}
URLRX = re.compile(r'https?://[^\s"\'<>]+')
for root, _, files in os.walk(SRC):
    for f in sorted(files):
        if not f.endswith('.java') or f == 'R.java': continue
        cls = f[:-5]
        src = open(os.path.join(root, f), 'rb').read()
        tree = PARSER.parse(src)
        # variables that hold SharedPreferences / Editor / Bundle-from-extras / Intents
        prefvars, editvars, bundlevars, intents = set(['appsettings']), set(), set(), {}
        stack = [tree.root_node]
        allnodes = []
        while stack:
            n = stack.pop(); allnodes.append(n); stack.extend(reversed(n.children))
        for n in allnodes:
            if n.type in ('variable_declarator', 'assignment_expression'):
                if n.type == 'variable_declarator':
                    nm = n.child_by_field_name('name'); val = n.child_by_field_name('value')
                    var = txt(nm) if nm is not None else None
                else:
                    l = n.child_by_field_name('left'); val = n.child_by_field_name('right')
                    var = txt(l).split('.')[-1] if l is not None else None
                if var is None or val is None: continue
                vt = txt(val)
                if 'getSharedPreferences(' in vt and '.edit()' not in vt: prefvars.add(var)
                if vt.endswith('.edit()'): editvars.add(var)
                if 'getExtras()' in vt: bundlevars.add(var)
                if val.type == 'object_creation_expression' and re.search(r'new\s+Intent\s*\(', vt):
                    m = re.search(r'\(Class<\?>\)\s*(\w+)\.class', vt) or re.search(r'\b(\w+)\.class\b', vt)
                    intents[var] = m.group(1) if m else None
        for n in allnodes:
            if n.type == 'string_literal':
                for u in URLRX.findall(unescape(txt(n))):
                    urls.append({'cls': cls, 'method': enclosing_method(n), 'line': n.start_point[0] + 1, 'url': u})
            if n.type != 'method_invocation': continue
            obj, name, args = call_parts(n)
            ot = txt(obj) if obj is not None else ''
            otail = ot.split('.')[-1]
            line = n.start_point[0] + 1; meth = None
            def M(): return enclosing_method(n)
            if name == 'makeText' and 'Toast' in ot and len(args) >= 2:
                messages.append({'cls': cls, 'method': M(), 'line': line, 'kind': 'Toast', 'text': render(args[1])})
            elif name == 'make' and 'Snackbar' in ot and len(args) >= 2:
                messages.append({'cls': cls, 'method': M(), 'line': line, 'kind': 'Snackbar', 'text': render(args[1])})
            elif name in MSG_FIRST and args and args[0].type in ('string_literal', 'binary_expression'):
                kind = {'setTitle': 'Dialog/screen title', 'setMessage': 'Dialog message', 'setError': 'Field error',
                        'setHint': 'Hint'}.get(name, 'Dialog button')
                messages.append({'cls': cls, 'method': M(), 'line': line, 'kind': kind, 'text': render(args[0])})
            elif name in PREFS_GET and args and args[0].type == 'string_literal' and (otail in prefvars or 'getSharedPreferences(' in ot or otail.lower() in ('appsettings', 'sharedpreferences', 'prefs', 'pref', 'msp', 'sp')):
                prefs.append({'cls': cls, 'method': M(), 'line': line, 'op': 'read', 'type': name.replace('get', '') or 'contains', 'key': unescape(txt(args[0])),
                              'default': compact(txt(args[1])) if len(args) > 1 else ''})
            elif name in PREFS_PUT and args and args[0].type == 'string_literal' and (otail in editvars or '.edit()' in ot or otail.lower() in ('appsettingseditor', 'editor', 'edit', 'prefseditor')):
                prefs.append({'cls': cls, 'method': M(), 'line': line, 'op': 'write', 'type': name.replace('put', ''), 'key': unescape(txt(args[0])),
                              'value': compact(txt(args[1])) if len(args) > 1 else ''})
            elif name == 'putExtra' and args and args[0].type == 'string_literal':
                target = intents.get(otail)
                extras_put.append({'cls': cls, 'method': M(), 'line': line, 'key': unescape(txt(args[0])), 'value': compact(txt(args[1])) if len(args) > 1 else '', 'target': target})
            elif name in EXTRA_GET and args and args[0].type == 'string_literal':
                extras_get.append({'cls': cls, 'method': M(), 'line': line, 'key': unescape(txt(args[0])), 'type': name.replace('get', '').replace('Extra', '')})
            elif name in BUNDLE_GET and args and args[0].type == 'string_literal' and (otail in bundlevars or 'getExtras()' in ot):
                extras_get.append({'cls': cls, 'method': M(), 'line': line, 'key': unescape(txt(args[0])), 'type': name.replace('get', '')})
json.dump({'messages': messages, 'prefs': prefs, 'extras_put': extras_put, 'extras_get': extras_get, 'urls': urls},
          open(OUT, 'w'), indent=1, ensure_ascii=False)
from collections import Counter
print('messages', len(messages), Counter(m['kind'] for m in messages).most_common())
print('prefs ops', len(prefs), '| distinct keys', len(set(p['key'] for p in prefs)))
print('extras put', len(extras_put), '| get', len(extras_get), '| distinct keys', len(set(e['key'] for e in extras_put + extras_get)))
print('urls', len(urls), '| distinct', len(set(u['url'] for u in urls)))
