#!/usr/bin/env python3
"""Extract every SQL statement from the decompiled app sources using a Java AST (tree-sitter).

For each SQLite / JDBC call site it reconstructs the full SQL text (string concatenations,
local variables, += appends and StringBuilder appends are resolved; non-literal parts become
{expression}), the bound parameters, OUT parameters, the result columns read from the
Cursor/ResultSet, the DB connection context and the source location. SQL-looking literals that
never reach a recognised call site are reported as orphans so nothing is missed.

usage: sqlextract.py <app-source-root> <out.json>
"""
import json, os, re, sys
import tree_sitter_java as tsj
from tree_sitter import Language, Parser

JAVA = Language(tsj.language())
PARSER = Parser(JAVA)
SRC_ROOT, OUT = sys.argv[1], sys.argv[2]

SQLISH = re.compile(r'^\s*(select|insert|update|delete|exec|execute|create|drop|alter|with|declare|truncate|merge|\{\s*(\?\s*=\s*)?call)\b|\b(select\s.+\sfrom|insert\s+into|update\s+\w+\s+set|delete\s+from|create\s+table|drop\s+table)\b', re.I | re.S)
STRICT = re.compile(r'\b(select\s.+?\sfrom\b|insert\s+into\b|update\s+\[?\w+\]?\s+set\b|delete\s+from\b|exec(?:ute)?\s+\[?\w+|create\s+table\b|drop\s+table\b|alter\s+table\b|truncate\s+table\b)|\{\s*(\?\s*=\s*)?call\b', re.I | re.S)
PROC = re.compile(r'(?:\bexec(?:ute)?|\{\s*(?:\?\s*=\s*)?call)\s+\[?(?:dbo\]?\.)?\[?(\w+)', re.I)
TABLE = re.compile(r'\b(?:from|join|into|update|table(?:\s+if\s+not\s+exists)?|delete\s+from)\s+\[?(?:dbo\]?\.)?\[?([A-Za-z_]\w*)', re.I)
TSTOP = {'select', 'where', 'set', 'values', 'the', 'if', 'not', 'exists', 'as', 'on', 'a', 'b', 'c', 'x', 'y', 'z', 't'}
JDBC_SET = {'setString', 'setInt', 'setDouble', 'setDate', 'setBoolean', 'setFloat', 'setLong', 'setTimestamp',
            'setNull', 'setBytes', 'setBinaryStream', 'setObject', 'setBigDecimal', 'setTime', 'setShort', 'setNString'}
SQLITE_BIND = {'bindString', 'bindLong', 'bindDouble', 'bindNull', 'bindBlob', 'bindAllArgsAsStrings'}
RS_GET = {'getString', 'getInt', 'getDouble', 'getBoolean', 'getDate', 'getFloat', 'getLong', 'getTimestamp',
          'getObject', 'getBigDecimal', 'getShort', 'getByte', 'getBytes', 'getNString', 'getTime'}

def txt(n):
    return n.text.decode('utf-8', 'replace')

def unescape(lit):
    s = lit[1:-1]
    out, i = [], 0
    while i < len(s):
        ch = s[i]
        if ch == '\\' and i + 1 < len(s):
            nx = s[i + 1]
            if nx == 'u' and i + 5 < len(s) + 0:
                try:
                    out.append(chr(int(s[i + 2:i + 6], 16))); i += 6; continue
                except ValueError:
                    pass
            out.append({'n': '\n', 't': '\t', 'r': '', '"': '"', "'": "'", '\\': '\\', '0': '\0'}.get(nx, nx)); i += 2; continue
        out.append(ch); i += 1
    return ''.join(out)

def compact(s, n=90):
    s = re.sub(r'\s+', ' ', s)
    s = re.sub(r'\b[\w$]+\.this\.', '', s)
    s = s.replace('this.', '')
    s = re.sub(r'net\.venussolutions\.venusmicrofindailyweeklycollection\.(clone\.)?', '', s)
    return s if len(s) <= n else s[:n - 1] + '…'

def walk(node, skip_nested=True, root=True):
    """yield nodes in document order, not descending into nested class bodies / method declarations."""
    yield node
    for ch in node.children:
        if skip_nested and not root and ch.type in ('class_body',) and node.type in ('object_creation_expression', 'class_declaration'):
            continue
        if skip_nested and ch.type in ('method_declaration', 'constructor_declaration', 'class_declaration'):
            continue
        if skip_nested and ch.type == 'class_body' and node.type == 'object_creation_expression':
            continue
        yield from walk(ch, skip_nested, False)

def enclosing_classes(node):
    names = []
    p = node.parent
    while p is not None:
        if p.type in ('class_declaration', 'interface_declaration', 'enum_declaration'):
            nm = p.child_by_field_name('name')
            if nm is not None:
                names.append(txt(nm))
        elif p.type == 'object_creation_expression' and any(c.type == 'class_body' for c in p.children):
            t = p.child_by_field_name('type')
            names.append('new ' + (txt(t) if t is not None else '?') + '(){}')
        p = p.parent
    return list(reversed(names))

def call_parts(inv):
    """method_invocation -> (object_node or None, name, [arg nodes])"""
    obj = inv.child_by_field_name('object')
    name = inv.child_by_field_name('name')
    args = inv.child_by_field_name('arguments')
    argl = [c for c in args.children if c.is_named and c.type not in ('comment', 'block_comment', 'line_comment')] if args is not None else []
    return obj, (txt(name) if name is not None else ''), argl

def lhs_name(n):
    """identifier or this.field -> plain name"""
    if n is None: return None
    if n.type == 'identifier': return txt(n)
    if n.type == 'field_access':
        f = n.child_by_field_name('field')
        o = n.child_by_field_name('object')
        if f is not None and o is not None and (o.type == 'this' or txt(o).endswith('.this')):
            return txt(f)
    return None

class MethodCtx:
    def __init__(self, fieldvals):
        self.events = {}        # var -> [(pos, op, node)]
        self.fieldvals = fieldvals
        self.contrib = set()    # (start,end) of literals used
    def add(self, var, pos, op, node):
        self.events.setdefault(var, []).append((pos, op, node))

    def resolve_var(self, var, pos, depth):
        evs = [e for e in self.events.get(var, []) if e[0] < pos]
        if not evs:
            fv = self.fieldvals.get(var)
            if fv is not None and depth < 6:
                return fv
            return None
        # last plain assignment, then subsequent appends
        last = max((i for i, e in enumerate(evs) if e[1] == '='), default=None)
        seq = evs[last:] if last is not None else evs
        parts = []
        for (p, op, n) in seq:
            if n is None:
                continue
            r = self.render(n, p, depth + 1)
            parts.append(r)
        return ''.join(parts) if parts else None

    def render(self, n, pos, depth=0):
        if depth > 12:
            return '${' + compact(txt(n)) + '}'
        t = n.type
        if t == 'string_literal':
            self.contrib.add((n.start_byte, n.end_byte))
            return unescape(txt(n))
        if t == 'binary_expression':
            op = n.child_by_field_name('operator')
            if op is not None and txt(op) == '+':
                return self.render(n.child_by_field_name('left'), pos, depth + 1) + self.render(n.child_by_field_name('right'), pos, depth + 1)
        if t == 'parenthesized_expression':
            inner = [c for c in n.children if c.is_named]
            if inner:
                return self.render(inner[0], pos, depth + 1)
        if t == 'identifier':
            v = self.resolve_var(txt(n), pos, depth)
            return v if v is not None else '${' + txt(n) + '}'
        if t == 'field_access':
            nm = lhs_name(n)
            if nm:
                v = self.resolve_var(nm, pos, depth)
                if v is not None:
                    return v
            return '${' + compact(txt(n)) + '}'
        if t == 'method_invocation':
            obj, name, args = call_parts(n)
            if name in ('trim', 'toString', 'toUpperCase', 'toLowerCase', 'intern') and obj is not None:
                inner = self.render(obj, pos, depth + 1)
                if name == 'toUpperCase' and not inner.startswith('{'): inner = inner.upper()
                if name == 'toLowerCase' and not inner.startswith('{'): inner = inner.lower()
                return inner
            if name == 'valueOf' and obj is not None and txt(obj) in ('String', 'Integer', 'Double', 'Long', 'Float', 'Boolean', 'Short') and args:
                return self.render(args[0], pos, depth + 1)
            if name == 'concat' and obj is not None and args:
                return self.render(obj, pos, depth + 1) + self.render(args[0], pos, depth + 1)
            return '${' + compact(txt(n)) + '}'
        if t in ('decimal_integer_literal', 'decimal_floating_point_literal', 'true', 'false', 'null_literal', 'character_literal'):
            return txt(n)
        return '${' + compact(txt(n)) + '}'

def field_initialisers(class_node, ctx_factory):
    vals = {}
    body = class_node.child_by_field_name('body')
    if body is None: return vals
    for ch in body.children:
        if ch.type == 'field_declaration':
            for d in ch.children:
                if d.type == 'variable_declarator':
                    nm = d.child_by_field_name('name'); val = d.child_by_field_name('value')
                    if nm is not None and val is not None and val.type in ('string_literal', 'binary_expression'):
                        c = ctx_factory()
                        r = c.render(val, val.start_byte)
                        if SQLISH.search(r):
                            vals[txt(nm)] = r
    return vals

def conn_label(s):
    for key, lab in (('selbranchconnString', 'selected-branch DB'), ('branchlevelReport_branchconnString', 'branch-report DB'),
                     ('MasterDBconnString', 'master DB'), ('ImagesuploadconnString', 'images DB'), ('branchconnString', 'branch DB')):
        if key in s:
            return lab
    return compact(s, 40)

stmts, orphans = [], []
sql_literals = []
for root, _, files in os.walk(SRC_ROOT):
    for f in sorted(files):
        if not f.endswith('.java') or f == 'R.java':
            continue
        path = os.path.join(root, f)
        src = open(path, 'rb').read()
        tree = PARSER.parse(src)
        outer = f[:-5]
        # gather SQL-looking literals for orphan detection
        for n in walk(tree.root_node, skip_nested=False):
            if n.type == 'string_literal':
                s = unescape(txt(n))
                if len(s) >= 8 and STRICT.search(s):
                    sql_literals.append((outer, n))
        # class-level field values (for SQL held in fields)
        classfields = {}
        for n in walk(tree.root_node, skip_nested=False):
            if n.type == 'class_declaration':
                classfields.update(field_initialisers(n, lambda: MethodCtx({})))
        contributed = set()
        for meth in walk(tree.root_node, skip_nested=False):
            if meth.type not in ('method_declaration', 'constructor_declaration'):
                continue
            body = meth.child_by_field_name('body')
            if body is None:
                continue
            mname = txt(meth.child_by_field_name('name')) if meth.child_by_field_name('name') is not None else '<init>'
            classes = enclosing_classes(meth)
            inner = '.'.join(c for c in classes[1:]) if len(classes) > 1 else ''
            ctx = MethodCtx(classfields)
            nodes = list(walk(body, skip_nested=True))
            # pass 1: assignment events (strings, builders, ContentValues, statements)
            cvs = {}      # cv var -> [(pos, 'new'|'put'|'clear', key, valnode)]
            for n in nodes:
                if n.type == 'variable_declarator':
                    nm = n.child_by_field_name('name'); val = n.child_by_field_name('value')
                    if nm is not None:
                        v = txt(nm)
                        if val is not None and val.type == 'object_creation_expression' and 'StringBuilder' in txt(val.child_by_field_name('type') or val):
                            args = [c for c in val.child_by_field_name('arguments').children if c.is_named] if val.child_by_field_name('arguments') is not None else []
                            ctx.add(v, n.start_byte, '=', args[0] if args else None)
                        elif val is not None and val.type == 'object_creation_expression' and 'ContentValues' in txt(val):
                            cvs.setdefault(v, []).append((n.start_byte, 'new', None, None))
                        elif val is not None:
                            ctx.add(v, n.start_byte, '=', val)
                elif n.type == 'assignment_expression':
                    l = lhs_name(n.child_by_field_name('left'))
                    op = txt(n.child_by_field_name('operator')) if n.child_by_field_name('operator') is not None else '='
                    r = n.child_by_field_name('right')
                    if l is None or r is None:
                        continue
                    if r.type == 'object_creation_expression' and 'ContentValues' in txt(r):
                        cvs.setdefault(l, []).append((n.start_byte, 'new', None, None))
                    elif r.type == 'object_creation_expression' and 'StringBuilder' in txt(r):
                        args = [c for c in r.child_by_field_name('arguments').children if c.is_named] if r.child_by_field_name('arguments') is not None else []
                        ctx.add(l, n.start_byte, '=', args[0] if args else None)
                    elif op == '+=':
                        ctx.add(l, n.start_byte, '+=', r)
                    else:
                        ctx.add(l, n.start_byte, '=', r)
                elif n.type == 'method_invocation':
                    obj, name, args = call_parts(n)
                    if name == 'append' and obj is not None:
                        # find chain root identifier
                        root_obj = obj
                        while root_obj is not None and root_obj.type == 'method_invocation':
                            root_obj = root_obj.child_by_field_name('object')
                        rv = lhs_name(root_obj) if root_obj is not None else None
                        if rv and args:
                            ctx.add(rv, n.start_byte, '+=', args[0])
                    if name in ('put',) and obj is not None and lhs_name(obj) in cvs and len(args) == 2:
                        key = unescape(txt(args[0])) if args[0].type == 'string_literal' else ctx.render(args[0], n.start_byte)
                        cvs[lhs_name(obj)].append((n.start_byte, 'put', key, args[1]))
                    if name == 'clear' and obj is not None and lhs_name(obj) in cvs:
                        cvs[lhs_name(obj)].append((n.start_byte, 'clear', None, None))
            # connection label for JDBC in this method / class
            conn_here = [txt(c) for c in nodes if c.type == 'method_invocation' and call_parts(c)[1] == 'getConnection']
            # pass 2: sinks
            stvars = {}   # statement/cursor/resultset var -> stmt index
            stmtvars = set()
            for n0 in nodes:
                if n0.type == 'method_invocation' and call_parts(n0)[1] == 'createStatement':
                    p0 = n0.parent
                    if p0 is not None and p0.type == 'variable_declarator' and p0.child_by_field_name('name') is not None:
                        stmtvars.add(txt(p0.child_by_field_name('name')))
                    elif p0 is not None and p0.type == 'assignment_expression':
                        ln0 = lhs_name(p0.child_by_field_name('left'))
                        if ln0: stmtvars.add(ln0)
            def new_stmt(kind, sqlnode, n, extra=None):
                sql = ctx.render(sqlnode, n.start_byte) if sqlnode is not None else ''
                rec = {'cls': outer, 'inner': inner, 'method': mname, 'line': n.start_point[0] + 1, 'kind': kind,
                       'db': 'local SQLite' if kind.startswith('sqlite') else 'SQL Server', 'sql': sql.strip(),
                       'params': [], 'reads': [], 'outs': []}
                if not kind.startswith('sqlite'):
                    rec['conn'] = conn_label(conn_here[-1]) if conn_here else 'connection opened elsewhere'
                if extra: rec.update(extra)
                stmts.append(rec)
                return len(stmts) - 1
            def bind_target(n):
                """find variable the invocation result is assigned to"""
                p = n.parent
                while p is not None and p.type in ('parenthesized_expression', 'cast_expression'):
                    p = p.parent
                if p is None: return None
                if p.type == 'variable_declarator':
                    nm = p.child_by_field_name('name'); return txt(nm) if nm is not None else None
                if p.type == 'assignment_expression':
                    return lhs_name(p.child_by_field_name('left'))
                return None
            for n in nodes:
                if n.type != 'method_invocation':
                    continue
                obj, name, args = call_parts(n)
                objname = lhs_name(obj) if obj is not None else None
                if name in ('rawQuery', 'execSQL') and args:
                    extra = {}
                    if len(args) > 1 and txt(args[1]) != 'null':
                        a = args[1]
                        if a.type == 'array_creation_expression':
                            init = [c for c in a.children if c.type == 'array_initializer']
                            extra['args'] = [compact(txt(c)) for c in init[0].children if c.is_named] if init else [compact(txt(a))]
                        else:
                            extra['args'] = [compact(txt(a))]
                    i = new_stmt('sqlite.' + name, args[0], n, extra)
                    tv = bind_target(n)
                    if tv: stvars[tv] = i
                elif name == 'compileStatement' and args:
                    i = new_stmt('sqlite.compileStatement', args[0], n)
                    tv = bind_target(n)
                    if tv: stvars[tv] = i
                elif name in ('insert', 'insertOrThrow', 'insertWithOnConflict', 'replace', 'update', 'delete') and obj is not None and len(args) >= 2 and args[0].type == 'string_literal':
                    # SQLiteDatabase helpers with ContentValues (skip String.replace / unrelated update/delete)
                    otxt = txt(obj)
                    dbish = re.search(r'(?i)(^|\.)(db|database|mydb|sqlitedatabase|sqldb|\w*db\w*)$', otxt) is not None or 'getWritableDatabase' in otxt or 'openOrCreateDatabase' in otxt
                    cvarg = args[2] if name in ('insert', 'insertOrThrow', 'insertWithOnConflict', 'replace') and len(args) >= 3 else (args[1] if name == 'update' else None)
                    if name in ('insert', 'insertOrThrow', 'insertWithOnConflict', 'replace', 'update'):
                        if cvarg is None or lhs_name(cvarg) not in cvs:
                            continue
                    elif not dbish:
                        continue
                    table = unescape(txt(args[0]))
                    cvnode = args[2] if name in ('insert', 'insertOrThrow', 'insertWithOnConflict', 'replace') and len(args) >= 3 else (args[1] if name == 'update' else None)
                    cols = []
                    if cvnode is not None and lhs_name(cvnode) in cvs:
                        evs = [e for e in cvs[lhs_name(cvnode)] if e[0] < n.start_byte]
                        last = max((k for k, e in enumerate(evs) if e[1] in ('new', 'clear')), default=-1)
                        for e in evs[last + 1:]:
                            if e[1] == 'put':
                                cols.append((e[2], ctx.render(e[3], e[0]) if e[3].type == 'string_literal' else '${' + compact(txt(e[3])) + '}'))
                    if name in ('insert', 'insertOrThrow', 'insertWithOnConflict', 'replace'):
                        verb = 'INSERT OR REPLACE INTO' if name == 'replace' else 'INSERT INTO'
                        sql = f"{verb} {table} ({', '.join(c for c, _ in cols)}) VALUES ({', '.join(v for _, v in cols)})"
                    elif name == 'update':
                        where = ctx.render(args[2], n.start_byte) if len(args) > 2 and txt(args[2]) != 'null' else ''
                        sql = f"UPDATE {table} SET {', '.join(f'{c}={v}' for c, v in cols)}" + (f" WHERE {where}" if where else '')
                    else:
                        where = ctx.render(args[1], n.start_byte) if txt(args[1]) != 'null' else ''
                        sql = f"DELETE FROM {table}" + (f" WHERE {where}" if where else '')
                    rec = {'cls': outer, 'inner': inner, 'method': mname, 'line': n.start_point[0] + 1, 'kind': f'sqlite.{name}(ContentValues)',
                           'db': 'local SQLite', 'sql': sql, 'params': [], 'reads': [], 'outs': []}
                    if name in ('update', 'delete'):
                        ai = 3 if name == 'update' else 2
                        if len(args) > ai and txt(args[ai]) != 'null':
                            rec['args'] = [compact(txt(args[ai]))]
                    stmts.append(rec)
                elif name in ('prepareStatement', 'prepareCall') and args:
                    i = new_stmt('jdbc.' + name, args[0], n)
                    tv = bind_target(n)
                    if tv: stvars[tv] = i
                elif name in ('executeQuery', 'executeUpdate', 'execute', 'addBatch', 'executeLargeUpdate') and args:
                    # Statement.execute*(sql)  (also chained createStatement().executeQuery(sql)); skip AsyncTask.execute(...)
                    chained = obj is not None and obj.type == 'method_invocation' and call_parts(obj)[1] == 'createStatement'
                    if args[0].type == 'array_creation_expression':
                        continue
                    probe = ctx.render(args[0], n.start_byte)
                    if not (chained or objname in stmtvars or SQLISH.search(probe)):
                        continue
                    i = new_stmt('jdbc.Statement.' + name, args[0], n)
                    tv = bind_target(n)
                    if tv: stvars[tv] = i
                elif name in ('executeQuery',) and not args and objname in stvars:
                    tv = bind_target(n)
                    if tv: stvars[tv] = stvars[objname]
                elif (name in JDBC_SET or name in SQLITE_BIND) and objname in stvars and args:
                    idx = txt(args[0]); val = compact(txt(args[1])) if len(args) > 1 else ''
                    stmts[stvars[objname]]['params'].append({'i': idx, 'set': name, 'expr': val})
                elif name == 'registerOutParameter' and objname in stvars and args:
                    stmts[stvars[objname]]['outs'].append({'i': txt(args[0]), 'type': compact(txt(args[1])) if len(args) > 1 else ''})
                elif name in RS_GET and objname in stvars and args:
                    a = args[0]
                    if a.type == 'string_literal':
                        col = unescape(txt(a))
                        if col not in stmts[stvars[objname]]['reads']:
                            stmts[stvars[objname]]['reads'].append(col)
                    elif stmts[stvars[objname]]['kind'] == 'jdbc.prepareCall':
                        stmts[stvars[objname]]['reads'].append('OUT#' + txt(a))
                elif name in ('getColumnIndexOrThrow', 'getColumnIndex') and objname in stvars and args and args[0].type == 'string_literal':
                    col = unescape(txt(args[0]))
                    if col not in stmts[stvars[objname]]['reads']:
                        stmts[stvars[objname]]['reads'].append(col)
            contributed |= ctx.contrib
        for (cls, n) in [s for s in sql_literals if s[0] == outer]:
            if (n.start_byte, n.end_byte) not in contributed:
                p = n.parent
                meth = None
                while p is not None:
                    if p.type in ('method_declaration', 'constructor_declaration'):
                        meth = txt(p.child_by_field_name('name')) if p.child_by_field_name('name') is not None else '<init>'
                        break
                    if p.type == 'field_declaration':
                        meth = '<field>'
                        break
                    p = p.parent
                orphans.append({'cls': outer, 'method': meth or '?', 'line': n.start_point[0] + 1, 'text': unescape(txt(n))})

for s in stmts:
    sq = s['sql']
    m = re.match(r'\s*(\{\s*(\?\s*=\s*)?call|\w+)', sq, re.I)
    op = (m.group(1).lower() if m else '?')
    if op.startswith('{'): op = 'call'
    if op == 'execute': op = 'exec'
    s['op'] = op
    s['procs'] = sorted(set(p.lower() for p in PROC.findall(sq)))
    s['tables'] = sorted(set(t for t in TABLE.findall(sq) if t.lower() not in TSTOP and len(t) > 2), key=str.lower)

json.dump({'stmts': stmts, 'orphans': orphans}, open(OUT, 'w'), indent=1, ensure_ascii=False)
from collections import Counter
print('statements:', len(stmts), '| local:', sum(1 for s in stmts if s['db'] == 'local SQLite'), '| remote:', sum(1 for s in stmts if s['db'] == 'SQL Server'))
print('by kind:', Counter(s['kind'] for s in stmts).most_common())
print('empty sql:', sum(1 for s in stmts if not s['sql']), '| orphans:', len(orphans), '| sql literals seen:', len(sql_literals))
