#!/usr/bin/env python3
"""Rename Paparazzi snapshots to <name>.png / <name>.ta.png, downscale to 540px width, palette-quantize."""
import glob, os, re, sys, json
from PIL import Image
SRC, DST, W = sys.argv[1], sys.argv[2], int(sys.argv[3])
os.makedirs(DST, exist_ok=True)
total = 0; out = {}
for f in sorted(glob.glob(SRC + '/*.png')):
    m = re.search(r'render\[([^\]]+)\]_', os.path.basename(f))
    if not m: continue
    name = m.group(1)
    fn = name[:-4] + '.ta.png' if name.endswith('__ta') else name + '.png'
    im = Image.open(f).convert('RGB')
    if im.width > W:
        im = im.resize((W, round(im.height * W / im.width)), Image.LANCZOS)
    q = im.quantize(colors=128, method=Image.Quantize.FASTOCTREE, dither=Image.Dither.NONE)
    p = os.path.join(DST, fn); q.save(p, optimize=True)
    total += os.path.getsize(p); out[fn] = list(im.size)
json.dump(out, open(DST + '/_sizes.json', 'w'), indent=0)
print(len(out), 'images,', round(total / 1e6, 1), 'MB total, avg', round(total / max(1, len(out)) / 1024), 'KB')
