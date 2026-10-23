#!/usr/bin/env python3
"""Build an HTML preview of the launcher icon and the Lucide icon set.

    python3 tools/preview/icon_sheet.py <res dir> <out.html> [--tint #FF4D6D]
Screenshot it with any headless browser.
"""
import argparse
import html
import os
import sys

sys.path.insert(0, os.path.dirname(__file__))
from vector2svg import to_svg  # noqa: E402


def launcher_layers(res):
    d = os.path.join(res, "drawable")
    bg = to_svg(os.path.join(d, "ic_launcher_background.xml"))
    fg = to_svg(os.path.join(d, "ic_launcher_foreground.xml"))
    mono = to_svg(os.path.join(d, "ic_launcher_monochrome.xml"), tint="#FFE2C7")
    return bg, fg, mono


def adaptive(bg, fg, size, radius, extra_class=""):
    # Launchers show the centre 72 of the 108 unit layers, clipped by the device mask.
    crop = lambda s: s.replace('viewBox="0 0 108 108"', 'viewBox="18 18 72 72"')
    return (f'<div class="icon {extra_class}" style="width:{size}px;height:{size}px;border-radius:{radius}">'
            f'<div class="layer">{crop(bg)}</div><div class="layer">{crop(fg)}</div></div>')


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("res")
    ap.add_argument("out")
    ap.add_argument("--tint", default="#F6821F")
    ap.add_argument("--icons", default="tools/lucide/icons.txt")
    a = ap.parse_args()
    bg, fg, mono = launcher_layers(a.res)
    names = [l.strip() for l in open(a.icons) if l.strip() and not l.startswith("#")]
    tiles = []
    for n in names:
        svg = to_svg(os.path.join(a.res, "drawable", f"ic_{n.replace('-', '_')}.xml"), tint=a.tint, size=34)
        tiles.append(f'<div class="tile">{svg}<span>{html.escape(n)}</span></div>')
    mono_icon = (f'<div class="icon mono" style="width:120px;height:120px;border-radius:50%">'
                 f'<div class="layer">{mono.replace(chr(34) + "0 0 108 108" + chr(34), chr(34) + "18 18 72 72" + chr(34))}</div></div>')
    page = f"""<!doctype html><html><head><meta charset="utf-8"><style>
body{{margin:0;background:#05070D;color:#E6EDF3;font-family:Roboto,'Segoe UI',sans-serif;}}
.wrap{{padding:40px 48px;background:radial-gradient(900px 420px at 50% -80px,rgba(246,130,31,.22),transparent);}}
h1{{font-size:34px;margin:0 0 4px;letter-spacing:-.5px}} .sub{{color:#8B98A9;margin:0 0 28px;font-size:15px}}
.over{{color:#5B6B7F;text-transform:uppercase;letter-spacing:.16em;font-size:12px;margin:28px 0 14px;font-weight:600}}
.row{{display:flex;gap:36px;align-items:flex-end;flex-wrap:wrap}}
.cell{{display:flex;flex-direction:column;align-items:center;gap:10px;color:#8B98A9;font-size:13px}}
.icon{{position:relative;overflow:hidden;box-shadow:0 10px 40px rgba(246,130,31,.35)}}
.icon.mono{{background:#3A2410;box-shadow:none}}
.layer{{position:absolute;inset:0}} .layer svg{{width:100%;height:100%;display:block}}
.home{{display:flex;gap:28px;padding:22px 26px;border-radius:28px;background:linear-gradient(135deg,#1b2433,#0D1117);border:1px solid #1F2A3A}}
.app{{display:flex;flex-direction:column;align-items:center;gap:8px;font-size:13px;color:#E6EDF3}}
.grid{{display:grid;grid-template-columns:repeat(11,1fr);gap:10px}}
.tile{{background:#0D1117;border:1px solid #1F2A3A;border-radius:16px;height:92px;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:9px}}
.tile span{{font:11px monospace;color:#8B98A9;max-width:92%;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}}
.badge{{display:inline-block;padding:4px 10px;border-radius:999px;border:1px solid #F6821F;color:#F6821F;font:12px monospace;margin-left:10px;vertical-align:middle}}
</style></head><body><div class="wrap">
<h1>KRS Tunnels <span class="badge">com.krstech.tunnels</span></h1>
<p class="sub">Launcher icon (adaptive, Lucide “radar”) and the {len(names)} Lucide icons used in the app, in the brand orange.</p>
<div class="over">Launcher icon · device masks</div>
<div class="row">
<div class="cell">{adaptive(bg, fg, 160, '50%')}circle</div>
<div class="cell">{adaptive(bg, fg, 160, '38%')}squircle</div>
<div class="cell">{adaptive(bg, fg, 160, '22%')}rounded square</div>
<div class="cell">{mono_icon}themed (Android 13+)</div>
<div class="home">
<div class="app">{adaptive(bg, fg, 64, '50%')}KRS Tunnels</div>
<div class="app">{adaptive(bg, fg, 64, '30%')}KRS Tunnels</div>
<div class="app">{adaptive(bg, fg, 48, '50%')}48 dp</div>
</div></div>
<div class="over">Lucide icon set · tint {a.tint}</div>
<div class="grid">{''.join(tiles)}</div>
</div></body></html>"""
    with open(a.out, "w") as f:
        f.write(page)


if __name__ == "__main__":
    main()
