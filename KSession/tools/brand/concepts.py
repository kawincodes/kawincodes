#!/usr/bin/env python3
"""Logo concept explorations for KRS Tunnels (SVG, 108x108 adaptive-icon space).

Only paths and linear/radial gradients are used so any concept can become an Android
VectorDrawable unchanged.  python3 concepts.py <font700.woff> <font500.woff> <out.html>
"""
import math
import sys

ORANGE, ORANGE_L, ORANGE_D = "#F6821F", "#FFB054", "#C2410C"
CREAM = "#FFE3C2"
NAVY, NAVY_2 = "#0B0F17", "#161E2B"


def arch(cx, top_cy, r, base_y):
    """Tunnel portal: two legs from base_y up to top_cy and a half circle of radius r."""
    return (f"M{cx - r},{base_y} V{top_cy} A{r},{r} 0 0 1 {cx + r},{top_cy} V{base_y}")


def circle(cx, cy, r):
    return f"M{cx - r},{cy} a{r},{r} 0 1,0 {2 * r},0 a{r},{r} 0 1,0 {-2 * r},0"


def polar(cx, cy, r, deg):
    t = math.radians(deg)
    return cx + r * math.cos(t), cy + r * math.sin(t)


def sector(cx, cy, r, a0, a1):
    p0, p1 = polar(cx, cy, r, a0), polar(cx, cy, r, a1)
    large = 1 if (a1 - a0) % 360 > 180 else 0
    return f"M{cx},{cy} L{p0[0]:.2f},{p0[1]:.2f} A{r},{r} 0 {large} 1 {p1[0]:.2f},{p1[1]:.2f} Z"


def defs(*items):
    return "<defs>" + "".join(items) + "</defs>"


def lin(gid, stops, x1=0, y1=0, x2=108, y2=108):
    s = "".join(f'<stop offset="{o}" stop-color="{c}" stop-opacity="{a}"/>' for o, c, a in stops)
    return f'<linearGradient id="{gid}" gradientUnits="userSpaceOnUse" x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}">{s}</linearGradient>'


def rad(gid, stops, cx, cy, r):
    s = "".join(f'<stop offset="{o}" stop-color="{c}" stop-opacity="{a}"/>' for o, c, a in stops)
    return f'<radialGradient id="{gid}" gradientUnits="userSpaceOnUse" cx="{cx}" cy="{cy}" r="{r}">{s}</radialGradient>'


SQ = "M0,0h108v108h-108z"


def p(d, fill="none", stroke=None, w=None, cap="round", op=1):
    s = f'<path d="{d}" fill="{fill}" fill-opacity="{op if fill != "none" else 1}"'
    if stroke:
        s += f' stroke="{stroke}" stroke-width="{w}" stroke-linecap="{cap}" stroke-linejoin="round" stroke-opacity="{op}"'
    return s + "/>"


def c1_echo_tunnel(k):
    """1. Echo Tunnel (dark): nested portals in perspective = tunnel depth and radar echoes;
    light at the end of the tunnel."""
    d = defs(rad(f"bg{k}", [(0, "#3A1F0A", 1), (0.55, NAVY_2, 1), (1, NAVY, 1)], 54, 78, 70),
             lin(f"a{k}", [(0, ORANGE_L, 1), (1, ORANGE_D, 1)], 30, 30, 78, 78),
             rad(f"glow{k}", [(0, "#FFD9A8", 0.95), (0.35, ORANGE, 0.45), (1, ORANGE, 0)], 54, 66, 16))
    body = [p(SQ, fill=f"url(#bg{k})"),
            p(circle(54, 64, 14), fill=f"url(#glow{k})"),
            p(arch(54, 50, 19, 72), stroke=f"url(#a{k})", w=5),
            p(arch(54, 55.5, 12.5, 72), stroke=ORANGE, w=3.8, op=0.85),
            p(arch(54, 61, 6.5, 72), stroke=ORANGE_L, w=2.8, op=0.9),
            p("M30,72 H78", stroke=ORANGE, w=2.4, op=0.55),
            p(circle(54, 66.5, 2.2), fill="#FFF4E6")]
    return d + "".join(body)


def c2_orange_portal(k):
    """2. Orange Portal: the same tunnel in white on the brand orange; bold and simple."""
    d = defs(lin(f"bg{k}", [(0, ORANGE_L, 1), (0.55, ORANGE, 1), (1, ORANGE_D, 1)]),
             rad(f"hl{k}", [(0, "#FFFFFF", 0.28), (1, "#FFFFFF", 0)], 34, 28, 70))
    body = [p(SQ, fill=f"url(#bg{k})"), p(SQ, fill=f"url(#hl{k})"),
            '<g transform="translate(54 55) scale(0.86) translate(-54 -55)">',
            p(arch(54, 50, 19, 72), stroke="#FFFFFF", w=5),
            p(arch(54, 55.5, 12.5, 72), stroke="#FFFFFF", w=3.8, op=0.72),
            p(arch(54, 61, 6.5, 72), stroke="#FFFFFF", w=2.8, op=0.5),
            p("M30,72 H78", stroke="#FFFFFF", w=2.6),
            p(circle(54, 66.5, 2.2), fill="#FFFFFF"), "</g>"]
    return d + "".join(body)


def c3_radar_k(bold, k):
    """3. Radar K: bold K monogram inside a radar ring with an orange sweep."""
    d = defs(rad(f"bg{k}", [(0, NAVY_2, 1), (1, NAVY, 1)], 54, 50, 70),
             rad(f"sw{k}", [(0, ORANGE, 0.0), (1, ORANGE, 0.75)], 54, 54, 27))
    kpath = bold.path("K", 54, 62.2, 16.5, anchor="middle")
    body = [p(SQ, fill=f"url(#bg{k})"),
            p(sector(54, 54, 21.5, -90, -18), fill=f"url(#sw{k})"),
            p(circle(54, 54, 21.5), stroke=ORANGE, w=2.8),
            p(circle(54, 54, 14.5), stroke="#FFFFFF", w=1, op=0.16),
            p("M54,54 L%.2f,%.2f" % polar(54, 54, 21.5, -18), stroke=ORANGE_L, w=2),
            p(kpath, fill="#FFFFFF"),
            p(circle(*polar(54, 54, 17.8, -50), 1.8), fill=ORANGE_L)]
    return d + "".join(body)


def c4_cloud_gate(k):
    """4. Cloud Gate: a cloud with a tunnel doorway cut into it - a tunnel into Cloudflare."""
    d = defs(lin(f"bg{k}", [(0, ORANGE_L, 1), (0.55, ORANGE, 1), (1, ORANGE_D, 1)]),
             rad(f"hl{k}", [(0, "#FFFFFF", 0.25), (1, "#FFFFFF", 0)], 34, 28, 70))
    sc, tx, ty = 2.05, 54 - 12 * 2.05, 56 - 12 * 2.05
    cloud = (f"M{17.5 * sc + tx},{19 * sc + ty} H{9 * sc + tx} "
             f"a{7 * sc},{7 * sc} 0 1 1 {6.71 * sc},{-9 * sc} h{1.79 * sc} "
             f"a{4.5 * sc},{4.5 * sc} 0 1 1 0,{9 * sc} Z")
    base = 19 * sc + ty
    door = f"M47.5,{base} V{base - 10} A6.5,6.5 0 0 1 60.5,{base - 10} V{base} Z"
    body = [p(SQ, fill=f"url(#bg{k})"), p(SQ, fill=f"url(#hl{k})"),
            f'<path d="{cloud} {door}" fill="#FFFFFF" fill-rule="evenodd"/>',
            p(circle(54, base - 6.5, 1.9), fill="#FFFFFF")]
    return d + "".join(body)


def svg(inner, view="0 0 108 108"):
    return f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="{view}">{inner}</svg>'


def main():
    sys.path.insert(0, __import__("os").path.dirname(__file__))
    from textpath import TextPath
    bold = TextPath(sys.argv[1])
    out = sys.argv[3]
    font700, font500 = sys.argv[1], sys.argv[2]
    concepts = [
        ("1", "Echo Tunnel", "Nested portals: a tunnel in perspective that doubles as radar echoes. Dark, premium, orange glow.", c1_echo_tunnel),
        ("2", "Orange Portal", "The same tunnel in white on brand orange. Loudest on a home screen.", c2_orange_portal),
        ("3", "Radar K", "Bold K monogram inside a radar ring with an orange sweep.", lambda k: c3_radar_k(bold, k)),
        ("4", "Cloud Gate", "A cloud with a tunnel doorway cut into it: your tunnel into Cloudflare.", c4_cloud_gate),
    ]
    cards = []
    for n, name, desc, fn in concepts:
        full = svg(fn(f"x{n}"))
        icon = lambda r, s, sub: (f'<div class="ic" style="width:{s}px;height:{s}px;border-radius:{r}">'
                                  f'{svg(fn(f"{sub}{n}"), "18 18 72 72")}</div>')
        cards.append(f"""<div class="card"><div class="num">{n}</div><div class="name">{name}</div><div class="desc">{desc}</div>
<div class="row">{icon('50%', 132, 'a')}{icon('30%', 132, 'b')}
<div class="home">{icon('50%', 56, 'c')}{icon('30%', 56, 'd')}{icon('50%', 40, 'e')}</div></div>
<div class="lock"><div class="mk">{icon('26%', 64, 'f')}</div><div><div class="wm"><b>KRS</b> TUNNELS</div><div class="tg">Cloudflare Tunnels · Radar · Chat</div></div></div></div>""")
    page = f"""<!doctype html><html><head><meta charset="utf-8"><style>
@font-face{{font-family:SG;src:url('file://{font700}');font-weight:700}}
@font-face{{font-family:SG;src:url('file://{font500}');font-weight:500}}
body{{margin:0;background:#05070D;color:#E6EDF3;font-family:Roboto,sans-serif}}
.wrap{{padding:34px 40px;display:grid;grid-template-columns:1fr 1fr;gap:22px}}
.card{{position:relative;background:#0D1117;border:1px solid #1F2A3A;border-radius:24px;padding:22px 24px}}
.num{{position:absolute;right:22px;top:16px;font:700 44px SG;color:#1F2A3A}}
.name{{font:700 22px SG;color:#FFAD5C}} .desc{{color:#8B98A9;font-size:14px;margin:4px 0 16px;max-width:85%}}
.row{{display:flex;gap:22px;align-items:center}}
.ic{{overflow:hidden;flex:none;box-shadow:0 8px 30px rgba(246,130,31,.22)}} .ic svg{{width:100%;height:100%;display:block}}
.home{{display:flex;gap:14px;align-items:center;padding:14px 16px;border-radius:20px;background:linear-gradient(135deg,#1b2433,#121A26)}}
.lock{{display:flex;gap:16px;align-items:center;margin-top:18px;padding-top:16px;border-top:1px solid #1F2A3A}}
.wm{{font:500 30px SG;letter-spacing:.06em;color:#E6EDF3}} .wm b{{font-weight:700;color:#F6821F}}
.tg{{font:500 14px SG;color:#8B98A9;letter-spacing:.03em;margin-top:2px}}
</style></head><body><div class="wrap">{''.join(cards)}</div></body></html>"""
    open(out, "w").write(page)


if __name__ == "__main__":
    main()
