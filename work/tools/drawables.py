#!/usr/bin/env python3
"""Export the app's own drawables: vectors -> SVG, rasters -> best-density PNG/JPG, others -> list for swatch rendering."""
import json, os, re, shutil, sys
from lxml import etree
S = sys.argv[1]
RES = S + '/render/screens/src/main/res'
APKRES = S + '/out/apktool/res'
OUT = S + '/work/out_images/drawables'
AN = '{http://schemas.android.com/apk/res/android}'
if os.path.isdir(OUT): shutil.rmtree(OUT)
os.makedirs(OUT)
colors = {}
for f in ('colors.xml',):
    p = APKRES + '/values/' + f
    if os.path.exists(p):
        for c in etree.parse(p).getroot():
            if c.tag == 'color': colors[c.get('name')] = (c.text or '').strip()
FRAMEWORK = {'white': '#FFFFFFFF', 'black': '#FF000000', 'transparent': '#00000000', 'holo_red_dark': '#FFCC0000',
             'holo_green_dark': '#FF669900', 'holo_blue_dark': '#FF0099CC', 'holo_orange_dark': '#FFFF8800', 'darker_gray': '#AAAAAAAA'}
ATTRS = {'colorControlNormal': '#FF757575', 'colorPrimary': colors.get('colorPrimary', '#FF228B22'), 'colorAccent': colors.get('colorAccent', '#FFFF4081'),
         'colorOnSurface': '#FF1C1B1F', 'colorOnPrimary': '#FFFFFFFF', 'colorPrimaryDark': colors.get('colorPrimaryDark', '#FF1B5E20')}
def color(v, depth=0):
    if not v: return None
    v = v.strip()
    if v.startswith('@color/'):
        r = colors.get(v[7:]); return color(r, depth + 1) if r and depth < 5 else None
    if v.startswith('@android:color/'): return FRAMEWORK.get(v[15:], '#FF000000')
    if v.startswith('?'):
        return ATTRS.get(v.split('/')[-1], '#FF757575')
    if v.startswith('#'):
        h = v[1:]
        if len(h) == 3: h = 'FF' + ''.join(c * 2 for c in h)
        if len(h) == 4: h = ''.join(c * 2 for c in h)
        if len(h) == 6: h = 'FF' + h
        return '#' + h.upper()
    return None
def svgfill(c):
    """#AARRGGBB -> (rgb, opacity)"""
    if not c or len(c) != 9: return ('none', 1)
    a = int(c[1:3], 16) / 255; return ('#' + c[3:], round(a, 3))
def num(v, d=0.0):
    try: return float(re.sub(r'(dp|dip|px|sp)$', '', v))
    except (TypeError, ValueError): return d
def conv_group(el, out):
    for ch in el:
        if not isinstance(ch.tag, str): continue
        if ch.tag == 'path':
            d = ch.get(AN + 'pathData', '')
            if d.startswith('@string/'): continue
            fc, fo = svgfill(color(ch.get(AN + 'fillColor')))
            fa = num(ch.get(AN + 'fillAlpha'), 1.0)
            sc, so = svgfill(color(ch.get(AN + 'strokeColor')))
            sw = num(ch.get(AN + 'strokeWidth'), 0)
            rule = 'evenodd' if ch.get(AN + 'fillType') == 'evenOdd' else 'nonzero'
            attrs = [f'd="{d}"', f'fill="{fc}"', f'fill-rule="{rule}"']
            if fc != 'none' and fo * fa < 1: attrs.append(f'fill-opacity="{round(fo * fa, 3)}"')
            if sc != 'none' and sw > 0:
                attrs += [f'stroke="{sc}"', f'stroke-width="{sw}"']
                if so < 1: attrs.append(f'stroke-opacity="{so}"')
                lc = ch.get(AN + 'strokeLineCap'); lj = ch.get(AN + 'strokeLineJoin')
                if lc: attrs.append(f'stroke-linecap="{lc}"')
                if lj: attrs.append(f'stroke-linejoin="{lj}"')
            out.append('<path ' + ' '.join(attrs) + '/>')
        elif ch.tag == 'group':
            tx, ty = num(ch.get(AN + 'translateX')), num(ch.get(AN + 'translateY'))
            sx, sy = num(ch.get(AN + 'scaleX'), 1), num(ch.get(AN + 'scaleY'), 1)
            rot = num(ch.get(AN + 'rotation')); px, py = num(ch.get(AN + 'pivotX')), num(ch.get(AN + 'pivotY'))
            tf = f'translate({tx},{ty}) translate({px},{py}) rotate({rot}) scale({sx},{sy}) translate({-px},{-py})'
            out.append(f'<g transform="{tf}">'); conv_group(ch, out); out.append('</g>')
        elif ch.tag == 'clip-path':
            pass  # clip paths are rare in app icons; skipped
def vector_to_svg(path):
    r = etree.parse(path).getroot()
    vw, vh = num(r.get(AN + 'viewportWidth'), 24), num(r.get(AN + 'viewportHeight'), 24)
    w, h = num(r.get(AN + 'width'), 24), num(r.get(AN + 'height'), 24)
    body = []; conv_group(r, body)
    tint = color(r.get(AN + 'tint'))
    if tint:
        body = [re.sub(r'fill="#[0-9A-Fa-f]{6}"', f'fill="#{tint[3:]}"', b) for b in body]
    scale = 4  # export at 4x of the dp size for crispness
    return (f'<svg xmlns="http://www.w3.org/2000/svg" width="{w * scale:g}" height="{h * scale:g}" viewBox="0 0 {vw:g} {vh:g}">'
            + ''.join(body) + '</svg>')
DENS = ['drawable-xxxhdpi', 'drawable-xxhdpi', 'drawable-xhdpi', 'drawable-hdpi', 'drawable-mdpi', 'drawable-ldpi', 'drawable', 'drawable-anydpi']
names = {}
for d in sorted(os.listdir(RES)):
    if d.split('-')[0] not in ('drawable', 'mipmap'): continue
    for f in os.listdir(os.path.join(RES, d)):
        nm, ext = os.path.splitext(f)
        names.setdefault(nm, []).append((d, ext))
index = []; swatch = []
for nm in sorted(names):
    vars_ = names[nm]
    raster = [(d, e) for d, e in vars_ if e in ('.png', '.jpg', '.webp')]
    xmls = [(d, e) for d, e in vars_ if e == '.xml']
    kind = None
    if raster:
        order = ['mipmap-xxxhdpi', 'mipmap-xxhdpi', 'mipmap-xhdpi', 'mipmap-hdpi', 'mipmap-mdpi'] + DENS
        best = sorted(raster, key=lambda de: order.index(de[0]) if de[0] in order else 99)[0]
        shutil.copy(os.path.join(RES, best[0], nm + best[1]), os.path.join(OUT, nm + best[1].replace('.webp', '.webp')))
        index.append({'name': nm, 'kind': 'raster', 'file': nm + best[1], 'from': best[0], 'variants': sorted(d for d, _ in raster)})
        continue
    if xmls:
        d0 = sorted(xmls, key=lambda de: DENS.index(de[0]) if de[0] in DENS else 99)[0][0]
        p = os.path.join(RES, d0, nm + '.xml')
        root = etree.parse(p).getroot()
        kind = root.tag
        if kind == 'vector':
            try:
                svg = vector_to_svg(p)
                open(os.path.join(OUT, nm + '.svg'), 'w').write(svg)
                index.append({'name': nm, 'kind': 'vector', 'file': nm + '.svg', 'from': d0})
                continue
            except Exception as ex:
                kind = 'vector-error'
        swatch.append({'name': nm, 'kind': kind, 'from': d0})
        index.append({'name': nm, 'kind': kind, 'file': None, 'from': d0})
json.dump({'index': index, 'swatch': swatch}, open(OUT + '/_index.json', 'w'), indent=1)
from collections import Counter
print('drawables exported:', len(index), Counter(i['kind'] for i in index).most_common(), '| need swatch render:', len(swatch))
# copy asset images too
os.makedirs(S + '/work/out_images/assets', exist_ok=True)
for f in ('img.png', 'ngx.jpg'):
    src = S + '/out/apktool/assets/image/' + f
    if os.path.exists(src): shutil.copy(src, S + '/work/out_images/assets/' + f)
print('asset images copied:', os.listdir(S + '/work/out_images/assets'))
