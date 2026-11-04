#!/usr/bin/env python3
"""Copy only the app's own resources from the apktool tree: drop anything a dependency AAR already defines (per R.txt)."""
import os, re, shutil, sys, zipfile
from lxml import etree
SRC, DST, CACHE = sys.argv[1], sys.argv[2], sys.argv[3]
lib = set()
for dp, _, fs in os.walk(CACHE):
    for f in fs:
        if f.endswith('.aar'):
            try:
                with zipfile.ZipFile(os.path.join(dp, f)) as z:
                    if 'R.txt' in z.namelist():
                        for line in z.read('R.txt').decode('utf-8', 'replace').splitlines():
                            parts = line.split()
                            if len(parts) >= 3 and parts[0] == 'int':
                                lib.add((parts[1], parts[2]))
            except zipfile.BadZipFile:
                pass
print('library resource keys:', len(lib))
FOLDER_TYPE = {'drawable': 'drawable', 'mipmap': 'mipmap', 'layout': 'layout', 'anim': 'anim', 'animator': 'animator', 'color': 'color',
               'menu': 'menu', 'xml': 'xml', 'raw': 'raw', 'font': 'font', 'interpolator': 'interpolator', 'transition': 'transition'}
TAG_TYPE = {'string': 'string', 'color': 'color', 'dimen': 'dimen', 'style': 'style', 'attr': 'attr', 'bool': 'bool', 'integer': 'integer',
            'string-array': 'array', 'integer-array': 'array', 'array': 'array', 'plurals': 'plurals', 'fraction': 'fraction', 'drawable': 'drawable'}
if os.path.isdir(DST): shutil.rmtree(DST)
kept = dropped = dollar = 0
for folder in sorted(os.listdir(SRC)):
    base = folder.split('-')[0]
    sp = os.path.join(SRC, folder)
    if base == 'values':
        for f in os.listdir(sp):
            if f == 'public.xml': continue
            tree = etree.parse(os.path.join(sp, f)); root = tree.getroot(); n0 = len(root)
            for el in list(root):
                if not isinstance(el.tag, str): continue
                nm = el.get('name') or ''
                ty = el.get('type') if el.tag == 'item' else TAG_TYPE.get(el.tag)
                key = (ty, nm.replace('.', '_'))
                if ty and key in lib:
                    root.remove(el); dropped += 1
                elif ty == 'id' and ('id', nm) in lib:
                    root.remove(el); dropped += 1
                else:
                    kept += 1
            if len(root):
                os.makedirs(os.path.join(DST, folder), exist_ok=True)
                tree.write(os.path.join(DST, folder, f), encoding='utf-8', xml_declaration=True)
        continue
    ty = FOLDER_TYPE.get(base)
    for f in os.listdir(sp):
        name = f.split('.')[0]
        if f.startswith('$'):
            dollar += 1; continue
        if ty and (ty, name) in lib:
            dropped += 1; continue
        os.makedirs(os.path.join(DST, folder), exist_ok=True)
        shutil.copy(os.path.join(sp, f), os.path.join(DST, folder, f)); kept += 1
# references to '$' generated files left in kept resources?
refs = 0
for dp, _, fs in os.walk(DST):
    for f in fs:
        if f.endswith('.xml') and '$' in open(os.path.join(dp, f), encoding='utf-8', errors='replace').read():
            refs += 1
print(f'kept {kept} | dropped {dropped} (library) | skipped {dollar} $-files | kept files still referencing $: {refs}')
