#!/usr/bin/env python3
"""HTML preview of the KRS Tunnels logo set (launcher icon, mark, lockup).

    python3 tools/brand/brand_sheet.py <out.html>
"""
import os
import sys

HERE = os.path.dirname(__file__)
sys.path.insert(0, os.path.join(HERE, "..", "preview"))
from vector2svg import to_svg  # noqa: E402

D = os.path.join(HERE, "..", "..", "app", "src", "main", "res", "drawable")


def svg(name, **kw):
    return to_svg(os.path.join(D, name), **kw)


def crop(s):
    return s.replace('viewBox="0 0 108 108"', 'viewBox="18 18 72 72"')


def adaptive(size, radius):
    bg, fg = crop(svg("ic_launcher_background.xml")), crop(svg("ic_launcher_foreground.xml"))
    return (f'<div class="icon" style="width:{size}px;height:{size}px;border-radius:{radius}">'
            f'<div class="layer">{bg}</div><div class="layer">{fg}</div></div>')


def main(out):
    mono = crop(svg("ic_launcher_monochrome.xml", tint="#FFD9B8"))
    page = f"""<!doctype html><html><head><meta charset="utf-8"><style>
body{{margin:0;background:#05070D;color:#E6EDF3;font-family:Roboto,'Segoe UI',sans-serif}}
.wrap{{padding:40px 48px 48px;background:radial-gradient(900px 420px at 30% -60px,rgba(246,130,31,.20),transparent)}}
.over{{color:#5B6B7F;text-transform:uppercase;letter-spacing:.16em;font-size:12px;margin:30px 0 14px;font-weight:600}}
.row{{display:flex;gap:34px;align-items:flex-end;flex-wrap:wrap}}
.cell{{display:flex;flex-direction:column;align-items:center;gap:10px;color:#8B98A9;font-size:13px}}
.icon{{position:relative;overflow:hidden;box-shadow:0 12px 44px rgba(246,130,31,.33)}}
.layer{{position:absolute;inset:0}} .layer svg,.box svg{{width:100%;height:100%;display:block}}
.mono{{width:132px;height:132px;border-radius:50%;background:#3A2410;overflow:hidden}}
.home{{display:flex;gap:26px;padding:22px 26px;border-radius:28px;background:linear-gradient(135deg,#1b2433,#0D1117);border:1px solid #1F2A3A}}
.app{{display:flex;flex-direction:column;align-items:center;gap:8px;font-size:13px}}
.lockup{{width:960px;height:300px;border-radius:28px;background:linear-gradient(135deg,#121A26,#05070D);border:1px solid #1F2A3A;padding:6px}}
.anat{{display:grid;grid-template-columns:repeat(4,1fr);gap:14px;margin-top:18px}}
.anat div{{background:#0D1117;border:1px solid #1F2A3A;border-radius:16px;padding:14px 16px;font-size:13px;color:#8B98A9}}
.anat b{{display:block;color:#FFAD5C;font-size:14px;margin-bottom:4px}}
</style></head><body><div class="wrap">
<div class="over">Logo lockup</div>
<div class="lockup box">{svg("logo_lockup.xml")}</div>
<div class="anat">
<div><b>Tunnel arch</b>Your service travels through a secure Cloudflare Tunnel.</div>
<div><b>Radar</b>The live radar UI: tunnels and edge data centres as blips.</div>
<div><b>Cloud keystone</b>Cloudflare's edge network at the top of the tunnel.</div>
<div><b>KRS monogram</b>KRS Tech Solutions, under the tunnel.</div>
</div>
<div class="over">Launcher icon · device masks</div>
<div class="row">
<div class="cell">{adaptive(170, '50%')}circle</div>
<div class="cell">{adaptive(170, '38%')}squircle</div>
<div class="cell">{adaptive(170, '22%')}rounded square</div>
<div class="cell"><div class="mono box">{mono}</div>themed (Android 13+)</div>
<div class="cell" style="width:132px;height:132px"><div class="box" style="width:132px;height:132px">{svg("logo_mark.xml")}</div>in-app mark</div>
</div>
<div class="over">On a home screen</div>
<div class="row"><div class="home">
<div class="app">{adaptive(72, '50%')}KRS Tunnels</div>
<div class="app">{adaptive(72, '32%')}KRS Tunnels</div>
<div class="app">{adaptive(56, '50%')}56 dp</div>
<div class="app">{adaptive(44, '50%')}44 dp</div>
</div></div>
</div></body></html>"""
    with open(out, "w") as f:
        f.write(page)


if __name__ == "__main__":
    main(sys.argv[1])
