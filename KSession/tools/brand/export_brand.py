#!/usr/bin/env python3
"""Export KRS Tunnels brand assets to docs/brand/ with a headless Chromium.

    python3 tools/brand/export_brand.py <chromium binary> <scratch dir>
Writes: krs-tunnels-lockup.svg, krs-tunnels-lockup.png (1120x320), krs-tunnels-icon-512.png,
krs-tunnels-feature-graphic.png (1024x500, Play Store), logo-sheet.png.
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "preview"))
sys.path.insert(0, HERE)
from vector2svg import to_svg  # noqa: E402
import brand_sheet  # noqa: E402

ROOT = os.path.abspath(os.path.join(HERE, "..", ".."))
D = os.path.join(ROOT, "app", "src", "main", "res", "drawable")
OUT = os.path.join(ROOT, "docs", "brand")
BASE_CSS = "html,body{margin:0;background:transparent;overflow:hidden} svg{display:block;width:100%;height:100%}"


def shoot(chrome, html, out_png, w, h, scratch):
    page = os.path.join(scratch, os.path.basename(out_png) + ".html")
    with open(page, "w") as f:
        f.write(html)
    # Headless Chromium reserves ~87px of the window height for browser UI.
    subprocess.run([chrome, "--headless=new", "--no-sandbox", "--disable-gpu", "--hide-scrollbars",
                    "--default-background-color=00000000", f"--window-size={w},{h + 87}",
                    f"--screenshot={out_png}", "file://" + page],
                   check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    # Crop the extra rows if Pillow-free: re-shoot at exact size is not possible, so pages pin
    # their content to the top-left and the extra transparent strip is trimmed below.
    trim_png(out_png, w, h)


def trim_png(path, w, h):
    """Crop a PNG to w x h using only the standard library (zlib)."""
    import struct
    import zlib
    data = open(path, "rb").read()
    assert data[:8] == b"\x89PNG\r\n\x1a\n"
    pos, chunks = 8, []
    while pos < len(data):
        ln = struct.unpack(">I", data[pos:pos + 4])[0]
        typ = data[pos + 4:pos + 8]
        chunks.append((typ, data[pos + 8:pos + 8 + ln]))
        pos += 12 + ln
    ihdr = chunks[0][1]
    W, H, depth, ctype = struct.unpack(">IIBB", ihdr[:10])
    if (W, H) == (w, h):
        return
    bpp = {6: 4, 2: 3}[ctype]
    raw = zlib.decompress(b"".join(c for t, c in chunks if t == b"IDAT"))
    stride = W * bpp
    rows, prev, i = [], bytearray(stride), 0
    for _ in range(H):
        ft = raw[i]
        line = bytearray(raw[i + 1:i + 1 + stride])
        i += 1 + stride
        for x in range(stride):
            a = line[x - bpp] if x >= bpp else 0
            b = prev[x]
            c = prev[x - bpp] if x >= bpp else 0
            if ft == 1:
                line[x] = (line[x] + a) & 255
            elif ft == 2:
                line[x] = (line[x] + b) & 255
            elif ft == 3:
                line[x] = (line[x] + (a + b) // 2) & 255
            elif ft == 4:
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                line[x] = (line[x] + (a if pa <= pb and pa <= pc else b if pb <= pc else c)) & 255
        rows.append(bytes(line))
        prev = line
    out = b"".join(b"\x00" + r[:w * bpp] for r in rows[:h])

    def chunk(t, d):
        return struct.pack(">I", len(d)) + t + d + struct.pack(">I", zlib.crc32(t + d) & 0xffffffff)

    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, depth, ctype, 0, 0, 0)) \
        + chunk(b"IDAT", zlib.compress(out, 9)) + chunk(b"IEND", b"")
    open(path, "wb").write(png)


def icon_layers(size):
    crop = lambda s: s.replace('viewBox="0 0 108 108"', 'viewBox="18 18 72 72"')
    bg = crop(to_svg(os.path.join(D, "ic_launcher_background.xml")))
    fg = crop(to_svg(os.path.join(D, "ic_launcher_foreground.xml")))
    return (f'<div style="position:relative;width:{size}px;height:{size}px">'
            f'<div style="position:absolute;inset:0">{bg}</div><div style="position:absolute;inset:0">{fg}</div></div>')


def lucide(name, color, size):
    return to_svg(os.path.join(D, f"ic_{name.replace('-', '_')}.xml"), tint=color, size=size)


def main(chrome, scratch):
    os.makedirs(OUT, exist_ok=True)
    lockup_svg = to_svg(os.path.join(D, "logo_lockup.xml"))
    with open(os.path.join(OUT, "krs-tunnels-lockup.svg"), "w") as f:
        f.write(lockup_svg)
    dark = "background:linear-gradient(135deg,#121A26,#05070D);"
    shoot(chrome, f"<style>{BASE_CSS}</style><div style='width:1120px;height:320px;{dark}'>{lockup_svg}</div>",
          os.path.join(OUT, "krs-tunnels-lockup.png"), 1120, 320, scratch)
    shoot(chrome, f"<style>{BASE_CSS}</style>{icon_layers(512)}",
          os.path.join(OUT, "krs-tunnels-icon-512.png"), 512, 512, scratch)
    feature = f"""<style>{BASE_CSS}
.g{{width:1024px;height:500px;position:relative;
background:radial-gradient(560px 420px at 50% 120%,rgba(246,130,31,.38),transparent),linear-gradient(135deg,#121A26,#05070D)}}
.lock{{position:absolute;left:92px;top:126px;width:840px;height:240px}}
</style><div class="g"><div class="lock">{lockup_svg}</div></div>"""
    shoot(chrome, feature, os.path.join(OUT, "krs-tunnels-feature-graphic.png"), 1024, 500, scratch)
    sheet = os.path.join(scratch, "brand.html")
    brand_sheet.main(sheet)
    shoot(chrome, open(sheet).read(), os.path.join(OUT, "logo-sheet.png"), 1100, 1210, scratch)
    for n in sorted(os.listdir(OUT)):
        print(n, os.path.getsize(os.path.join(OUT, n)))


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
