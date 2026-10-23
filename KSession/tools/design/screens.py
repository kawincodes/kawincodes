#!/usr/bin/env python3
"""Design previews of the KRS Tunnels screens (Chat, Radar, Tunnels, Cloud) as HTML.

These are the visual targets for the Android implementation: same palette (res/values/colors.xml),
same Lucide drawables (rendered through tools/preview/vector2svg.py) and the same logo drawables.

    python3 tools/design/screens.py <out dir> <roboto dir> <roboto-mono dir>
Writes chat.html, radar.html, tunnels.html, cloud.html and all.html into <out dir>.
"""
import math
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "preview"))
from vector2svg import to_svg  # noqa: E402

RES = os.path.join(HERE, "..", "..", "app", "src", "main", "res", "drawable")

ACCENT, GLOW, TEXT, MUTED, FAINT = "#F6821F", "#FFAD5C", "#E6EDF3", "#8B98A9", "#5B6B7F"
ONLINE, CONNECTING, ERROR, ON_ACCENT = "#2CF5D0", "#38BDF8", "#FF4D6D", "#1A0E05"


def ic(name, color=MUTED, size=20):
    return to_svg(os.path.join(RES, f"ic_{name.replace('-', '_')}.xml"), tint=color, size=size)


def logo_mark(size):
    return f'<div class="mark" style="width:{size}px;height:{size}px">{to_svg(os.path.join(RES, "logo_mark.xml"))}</div>'


def css(roboto, mono):
    return f"""
@font-face{{font-family:R;src:url('file://{roboto}/roboto-latin-400-normal.woff2');font-weight:400}}
@font-face{{font-family:R;src:url('file://{roboto}/roboto-latin-500-normal.woff2');font-weight:500}}
@font-face{{font-family:R;src:url('file://{roboto}/roboto-latin-700-normal.woff2');font-weight:700}}
@font-face{{font-family:M;src:url('file://{mono}/roboto-mono-latin-400-normal.woff2');font-weight:400}}
@font-face{{font-family:M;src:url('file://{mono}/roboto-mono-latin-500-normal.woff2');font-weight:500}}
*{{box-sizing:border-box}} body{{margin:0;background:#020306;font-family:R,sans-serif;color:{TEXT}}}
.stage{{display:flex;gap:36px;padding:36px;align-items:flex-start}}
.phone{{width:412px;height:892px;border-radius:44px;background:#000;padding:9px;box-shadow:0 30px 80px rgba(0,0,0,.6),0 0 0 1px #1F2A3A}}
.screen{{position:relative;width:100%;height:100%;border-radius:36px;overflow:hidden;
 background:radial-gradient(520px 300px at 50% -40px,rgba(246,130,31,.13),transparent),#05070D}}
.sb{{height:34px;display:flex;align-items:center;justify-content:space-between;padding:0 24px;font-size:14px;font-weight:500}}
.sb .r{{display:flex;gap:6px;align-items:center}}
.bat{{width:24px;height:12px;border:1.6px solid {TEXT};border-radius:3px;position:relative}}
.bat:after{{content:"";position:absolute;inset:1.5px;right:5px;background:{TEXT};border-radius:1px}}
.content{{position:absolute;top:34px;left:0;right:0;bottom:84px;overflow:hidden;padding:0 16px}}
.nav{{position:absolute;left:0;right:0;bottom:0;height:84px;background:#0D1117;border-top:1px solid #1F2A3A;display:flex;justify-content:space-around;padding-top:10px}}
.nav .it{{display:flex;flex-direction:column;align-items:center;gap:4px;font-size:12px;font-weight:500;color:{FAINT};width:80px}}
.nav .pill{{width:60px;height:30px;border-radius:15px;display:flex;align-items:center;justify-content:center}}
.nav .on{{color:{GLOW}}} .nav .on .pill{{background:rgba(246,130,31,.18)}}
.gest{{position:absolute;bottom:7px;left:50%;width:120px;height:4px;margin-left:-60px;border-radius:2px;background:#5B6B7F}}
.over{{font-size:11px;font-weight:500;letter-spacing:.14em;text-transform:uppercase;color:{FAINT}}}
.h1{{font-size:28px;font-weight:500;letter-spacing:-.01em;margin:2px 0 2px}}
.sub{{font-size:14px;color:{MUTED}}}
.card{{background:#0D1117;border:1px solid #1F2A3A;border-radius:20px;padding:14px 16px}}
.mono{{font-family:M,monospace;font-size:13px;color:{GLOW};white-space:nowrap;overflow:hidden;text-overflow:ellipsis}}
.chip{{display:inline-flex;align-items:center;gap:6px;height:32px;padding:0 12px;border-radius:14px;background:#121A26;border:1px solid #2B3B52;font-size:13px;color:{TEXT};white-space:nowrap}}
.chip svg{{width:16px;height:16px}}
.badge{{display:inline-flex;align-items:center;gap:4px;height:22px;padding:0 8px;border-radius:11px;font-size:11px;font-weight:500;letter-spacing:.04em;background:#182233;color:{MUTED}}}
.badge svg{{width:12px;height:12px}}
.btn{{display:inline-flex;align-items:center;justify-content:center;gap:8px;height:40px;padding:0 16px;border-radius:14px;font-size:14px;font-weight:500}}
.btn svg{{width:18px;height:18px}}
.fill{{background:{ACCENT};color:{ON_ACCENT}}} .out{{border:1px solid #2B3B52;color:{TEXT}}} .danger{{border:1px solid {ERROR};color:{ERROR}}}
.ib{{width:40px;height:40px;border-radius:12px;display:inline-flex;align-items:center;justify-content:center}}
.mark{{border-radius:24%;overflow:hidden;flex:none}} .mark svg{{width:100%;height:100%;display:block}}
.dot{{width:8px;height:8px;border-radius:50%;display:inline-block}}
.row{{display:flex;align-items:center}}
"""


def status_bar():
    return f'<div class="sb"><span>9:41</span><span class="r">{ic("wifi", TEXT, 16)}<span class="bat"></span></span></div>'


def nav(active):
    items = [("message-circle", "Chat"), ("radar", "Radar"), ("waypoints", "Tunnels"), ("cloud", "Cloud")]
    out = []
    for icon, label in items:
        on = label == active
        out.append(f'<div class="it {"on" if on else ""}"><div class="pill">{ic(icon, GLOW if on else FAINT, 22)}</div>{label}</div>')
    return f'<div class="nav">{"".join(out)}</div><div class="gest"></div>'


def phone(body, active):
    return (f'<div class="phone"><div class="screen">{status_bar()}<div class="content">{body}</div>'
            f'{nav(active)}</div></div>')


# --------------------------------------------------------------------------------------------- chat
def chat():
    bot_av = f'<div style="width:28px;height:28px;flex:none;margin-top:2px">{logo_mark(28)}</div>'

    def bot(html, extra=""):
        return (f'<div style="display:flex;gap:8px;margin:10px 0;align-items:flex-start">{bot_av}'
                f'<div style="max-width:318px"><div style="background:#121A26;border:1px solid rgba(246,130,31,.28);'
                f'border-radius:4px 18px 18px 18px;padding:10px 13px;font-size:14.5px;line-height:1.45">{html}</div>{extra}</div></div>')

    def user(text):
        return (f'<div style="display:flex;justify-content:flex-end;margin:10px 0"><div style="max-width:260px;'
                f'background:linear-gradient(135deg,#F6821F,#C2410C);color:#fff;border-radius:18px 4px 18px 18px;'
                f'padding:10px 14px;font-size:14.5px;font-weight:500">{text}</div></div>')

    code = lambda t: f'<span style="font-family:M;font-size:13px;color:{GLOW};background:rgba(246,130,31,.12);padding:1px 5px;border-radius:5px">{t}</span>'
    chips = lambda *cs: ('<div style="display:flex;gap:6px;flex-wrap:wrap;margin-top:8px">'
                         + "".join(f'<span class="chip">{ic(i, GLOW, 16)}{t}</span>' for i, t in cs) + "</div>")
    tunnel_card = f"""<div class="card" style="margin-top:8px;padding:12px 14px;border-color:rgba(44,245,208,.35)">
<div class="row" style="gap:8px"><span class="dot" style="background:{ONLINE};box-shadow:0 0 10px {ONLINE}"></span>
<b style="font-weight:500;font-size:15px">quick-3000</b><span class="badge">{ic('zap', MUTED, 12)}QUICK</span>
<span style="margin-left:auto;font-size:12px;color:{ONLINE};font-weight:500">Online</span></div>
<div class="mono" style="margin:8px 0 4px;font-size:12.5px">https://bright-otter-lake.trycloudflare.com</div>
<div style="font-size:12px;color:{MUTED}">2 links · BOM, SIN · 4m 12s</div>
<div class="row" style="gap:4px;margin-top:10px"><span class="btn danger" style="height:34px;padding:0 12px">{ic('circle-stop', ERROR, 16)}Stop</span>
<span style="flex:1"></span><span class="ib" style="width:34px;height:34px">{ic('copy', MUTED, 18)}</span>
<span class="ib" style="width:34px;height:34px">{ic('external-link', MUTED, 18)}</span><span class="ib" style="width:34px;height:34px">{ic('qr-code', MUTED, 18)}</span>
<span class="ib" style="width:34px;height:34px">{ic('scroll-text', MUTED, 18)}</span></div></div>"""
    body = f"""
<div class="row" style="gap:12px;padding:8px 0 12px;border-bottom:1px solid #1F2A3A;margin:0 -16px;padding-left:16px;padding-right:12px">
{logo_mark(40)}<div><div style="font-size:18px;font-weight:500">Kay</div>
<div style="font-size:12.5px;color:{MUTED}"><span class="dot" style="background:{ONLINE};width:7px;height:7px;margin-right:5px"></span>KRS Tunnels assistant · 2 online</div></div>
<span style="margin-left:auto" class="ib">{ic('eraser', MUTED, 20)}</span></div>
<div style="text-align:center;margin:12px 0 2px"><span style="font-size:11px;color:{FAINT};background:#0D1117;border:1px solid #1F2A3A;padding:4px 10px;border-radius:10px">Today</span></div>
<div style="height:4px"></div>
{bot(f"Hi, I'm <b>Kay</b>. I run Cloudflare Tunnels from this phone. Try {code('expose port 3000')} or sign in to use your own domain.", chips(('zap', 'Quick tunnel'), ('log-in', 'Sign in'), ('list', 'My tunnels')))}
{user('expose port 3000')}
{bot("Quick tunnel <b>quick-3000</b> is live on Cloudflare. Scan the QR code to open it on another device.", tunnel_card)}
<div style="position:absolute;left:0;right:0;bottom:0;padding:8px 12px 10px;background:linear-gradient(180deg,rgba(5,7,13,0),#05070D 22%)">
<div style="display:flex;gap:6px;margin-bottom:8px;overflow:hidden">
<span class="chip">{ic('activity', GLOW, 16)}Status</span><span class="chip">{ic('circle-stop', GLOW, 16)}Stop all</span>
<span class="chip">{ic('radar', GLOW, 16)}Radar</span><span class="chip">{ic('key-round', GLOW, 16)}Add token</span></div>
<div class="row" style="gap:8px"><div style="flex:1;height:48px;border-radius:24px;background:#121A26;border:1px solid #2B3B52;display:flex;align-items:center;padding:0 18px;color:{FAINT};font-size:15px">Message Kay…</div>
<div style="width:48px;height:48px;border-radius:50%;background:{ACCENT};display:flex;align-items:center;justify-content:center;box-shadow:0 6px 20px rgba(246,130,31,.4)">{ic('send', ON_ACCENT, 22)}</div></div></div>"""
    return phone(body, "Chat")


# -------------------------------------------------------------------------------------------- radar
def radar_svg(size=348):
    c, rr = 174, 160
    parts = [f'<svg width="{size}" height="{size}" viewBox="0 0 348 348" xmlns="http://www.w3.org/2000/svg">',
             '<defs><radialGradient id="rbg" cx="50%" cy="50%" r="50%"><stop offset="0" stop-color="#1A120B"/>'
             '<stop offset="1" stop-color="#07090F"/></radialGradient>'
             '<radialGradient id="glowT"><stop offset="0" stop-color="#2CF5D0" stop-opacity=".55"/><stop offset="1" stop-color="#2CF5D0" stop-opacity="0"/></radialGradient>'
             '<radialGradient id="glowE"><stop offset="0" stop-color="#FFAD5C" stop-opacity=".6"/><stop offset="1" stop-color="#FFAD5C" stop-opacity="0"/></radialGradient>'
             '<radialGradient id="core"><stop offset="0" stop-color="#FFAD5C"/><stop offset=".5" stop-color="#F6821F" stop-opacity=".5"/><stop offset="1" stop-color="#F6821F" stop-opacity="0"/></radialGradient></defs>',
             f'<circle cx="{c}" cy="{c}" r="{rr}" fill="url(#rbg)" stroke="#33220F" stroke-width="1.5"/>']
    for k in (0.25, 0.5, 0.75):
        parts.append(f'<circle cx="{c}" cy="{c}" r="{rr * k}" fill="none" stroke="#33220F" stroke-width="1"/>')
    parts.append(f'<path d="M{c - rr},{c}H{c + rr}M{c},{c - rr}V{c + rr}" stroke="#22170C" stroke-width="1"/>')
    for deg in range(0, 360, 5):
        t = math.radians(deg - 90)
        l = 9 if deg % 30 == 0 else 4
        x1, y1 = c + (rr - l) * math.cos(t), c + (rr - l) * math.sin(t)
        x2, y2 = c + rr * math.cos(t), c + rr * math.sin(t)
        parts.append(f'<line x1="{x1:.1f}" y1="{y1:.1f}" x2="{x2:.1f}" y2="{y2:.1f}" stroke="#4A3218" stroke-width="{1.4 if l > 5 else 1}"/>')
    for lbl, deg in (("N", 0), ("E", 90), ("S", 180), ("W", 270)):
        t = math.radians(deg - 90)
        parts.append(f'<text x="{c + (rr - 20) * math.cos(t):.1f}" y="{c + (rr - 20) * math.sin(t) + 4:.1f}" fill="#6B5034" '
                     f'font-family="M" font-size="11" text-anchor="middle">{lbl}</text>')
    # Sweep: wedge made of thin slices with increasing opacity (SweepGradient in the app).
    lead = 52
    for i in range(48):
        a0, a1 = lead - 70 + i * 70 / 48, lead - 70 + (i + 1) * 70 / 48 + 0.4
        p0 = (c + rr * math.cos(math.radians(a0 - 90)), c + rr * math.sin(math.radians(a0 - 90)))
        p1 = (c + rr * math.cos(math.radians(a1 - 90)), c + rr * math.sin(math.radians(a1 - 90)))
        parts.append(f'<path d="M{c},{c}L{p0[0]:.1f},{p0[1]:.1f}A{rr},{rr} 0 0 1 {p1[0]:.1f},{p1[1]:.1f}Z" '
                     f'fill="#F6821F" fill-opacity="{0.34 * (i / 47) ** 2:.3f}"/>')
    le = (c + rr * math.cos(math.radians(lead - 90)), c + rr * math.sin(math.radians(lead - 90)))
    parts.append(f'<line x1="{c}" y1="{c}" x2="{le[0]:.1f}" y2="{le[1]:.1f}" stroke="#FFAD5C" stroke-width="2"/>')
    # Tunnels (inner) and edges (outer ring).
    tunnels = [("blog", 28, 0.46, "#2CF5D0", 1), ("quick-3000", 142, 0.38, "#2CF5D0", 1), ("nas", 222, 0.64, "#5B6B7F", 0.7)]
    edges = [("BOM", 20, "blog"), ("SIN", 44, "blog"), ("BOM", 132, "quick"), ("MAA", 156, "quick")]
    for name, deg, rad_k, col, op in tunnels:
        t = math.radians(deg - 90)
        x, y = c + rr * rad_k * math.cos(t), c + rr * rad_k * math.sin(t)
        if col == "#2CF5D0":
            parts.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="16" fill="url(#glowT)"/>')
        parts.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="5.5" fill="{col}" fill-opacity="{op}"/>')
        parts.append(f'<text x="{x + 10:.1f}" y="{y + 4:.1f}" fill="{col}" fill-opacity="{op}" font-family="M" font-size="11.5">{name}</text>')
    for code, deg, _ in edges:
        t = math.radians(deg - 90)
        x, y = c + rr * 0.86 * math.cos(t), c + rr * 0.86 * math.sin(t)
        parts.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="13" fill="url(#glowE)"/>')
        parts.append(f'<rect x="{x - 4:.1f}" y="{y - 4:.1f}" width="8" height="8" fill="#FFAD5C" transform="rotate(45 {x:.1f} {y:.1f})"/>')
        parts.append(f'<text x="{x:.1f}" y="{y + 19:.1f}" fill="#FFAD5C" font-family="M" font-size="10.5" text-anchor="middle">{code}</text>')
    pulse = (c + rr * 0.86 * math.cos(math.radians(44 - 90)), c + rr * 0.86 * math.sin(math.radians(44 - 90)))
    parts.append(f'<circle cx="{pulse[0]:.1f}" cy="{pulse[1]:.1f}" r="19" fill="none" stroke="#FFAD5C" stroke-opacity=".45" stroke-width="1.2"/>')
    parts.append(f'<circle cx="{c}" cy="{c}" r="22" fill="url(#core)"/><circle cx="{c}" cy="{c}" r="4.5" fill="#FFE3C2"/>')
    parts.append(f'<text x="{c}" y="{c + 38}" fill="#E6EDF3" font-family="M" font-weight="500" font-size="12" text-anchor="middle" letter-spacing="2">2 ONLINE</text>')
    parts.append("</svg>")
    return "".join(parts)


def radar():
    stat = lambda icon, label, value, extra="": (
        f'<div class="card" style="padding:12px 14px"><div class="row" style="gap:6px">{ic(icon, GLOW, 16)}'
        f'<span class="over" style="font-size:10px">{label}</span></div>'
        f'<div style="font-family:M;font-size:24px;font-weight:500;margin-top:6px">{value}<span style="font-size:13px;color:{MUTED}">{extra}</span></div></div>')
    body = f"""
<div style="padding:10px 0 4px"><div class="over">Live edge map</div><div class="h1">Radar</div>
<div class="sub">2 of 3 tunnels online · 4 edge links</div></div>
<div style="display:flex;justify-content:center;margin:10px 0 12px">{radar_svg()}</div>
<div style="display:grid;grid-template-columns:1fr 1fr;gap:10px">
{stat('radio-tower', 'Online', '2', ' / 3')}{stat('waypoints', 'Edge links', '4')}
{stat('activity', 'Requests', '1.2k')}{stat('timer', 'Uptime', '2h 14m')}</div>
<div class="over" style="margin:14px 0 8px">Data centres</div>
<div style="display:flex;gap:6px"><span class="chip">{ic('earth', GLOW, 16)}BOM <span style="color:{MUTED}">×2</span></span>
<span class="chip">{ic('earth', GLOW, 16)}SIN <span style="color:{MUTED}">×1</span></span><span class="chip">{ic('earth', GLOW, 16)}MAA <span style="color:{MUTED}">×1</span></span></div>"""
    return phone(body, "Radar")


# ------------------------------------------------------------------------------------------ tunnels
def tunnel_card(name, typ, typ_icon, status, color, url, meta, online, url_muted=False):
    border = "rgba(44,245,208,.35)" if online else "#1F2A3A"
    status_icon = "radio-tower" if online else "cloud-off"
    main_btn = (f'<span class="btn danger" style="height:36px">{ic("circle-stop", ERROR, 17)}Stop</span>' if online else
                f'<span class="btn fill" style="height:36px">{ic("play", ON_ACCENT, 17)}Start</span>')
    return f"""<div class="card" style="border-color:{border};margin-bottom:12px">
<div class="row" style="gap:12px"><div style="width:42px;height:42px;border-radius:14px;background:{color}1f;display:flex;align-items:center;justify-content:center">{ic(status_icon, color, 22)}</div>
<div style="flex:1"><div class="row" style="gap:8px"><b style="font-size:16px;font-weight:500">{name}</b><span class="badge">{ic(typ_icon, MUTED, 12)}{typ}</span></div>
<div style="font-size:12.5px;color:{color};font-weight:500;margin-top:2px">{status}</div></div>
<span class="ib" style="width:34px;height:34px">{ic('chevron-down', MUTED, 18)}</span></div>
<div class="mono" style="margin:12px 0 4px;{'color:' + MUTED if url_muted else ''}">{url}</div>
<div style="font-size:12.5px;color:{MUTED}">{meta}</div>
<div class="row" style="gap:2px;margin-top:12px">{main_btn}<span style="flex:1"></span>
<span class="ib">{ic('copy', MUTED, 19)}</span><span class="ib">{ic('external-link', MUTED, 19)}</span>
<span class="ib">{ic('qr-code', MUTED, 19)}</span><span class="ib">{ic('scroll-text', MUTED, 19)}</span></div></div>"""


def tunnels():
    body = f"""
<div class="row" style="padding:10px 0 14px"><div><div class="over">Tunnels</div><div class="h1">Tunnels</div>
<div class="sub">2 active · 3 saved</div></div><span style="margin-left:auto" class="btn out">{ic('circle-stop', ERROR, 17)}Stop all</span></div>
{tunnel_card('blog', 'MANAGED', 'waypoints', 'Online', ONLINE, 'https://blog.krstechsolutions.in', '2 links · BOM, SIN · 2h 14m · 1.1k req', True)}
{tunnel_card('quick-3000', 'QUICK', 'zap', 'Online', ONLINE, 'https://bright-otter-lake.trycloudflare.com', '2 links · BOM, MAA · 4m 12s · 86 req', True)}
{tunnel_card('nas', 'TOKEN', 'key-round', 'Stopped', FAINT, 'Remote config · nas.krstechsolutions.in', 'Auto-start on boot', False, True)}
<div style="position:absolute;right:16px;bottom:16px" class="btn fill" ><span style="display:flex">{ic('plus', ON_ACCENT, 20)}</span>New tunnel</div>"""
    body = body.replace('<div style="position:absolute;right:16px;bottom:16px" class="btn fill" >',
                        '<div style="position:absolute;right:16px;bottom:16px;height:52px;padding:0 20px;border-radius:18px;box-shadow:0 8px 24px rgba(246,130,31,.4)" class="btn fill">')
    return phone(body, "Tunnels")


# -------------------------------------------------------------------------------------------- cloud
def cloud():
    def section(icon, title, inner, right=""):
        return (f'<div class="card" style="margin-bottom:12px"><div class="row" style="gap:10px;margin-bottom:10px">'
                f'<div style="width:34px;height:34px;border-radius:11px;background:rgba(246,130,31,.15);display:flex;align-items:center;justify-content:center">{ic(icon, GLOW, 18)}</div>'
                f'<b style="font-weight:500;font-size:15.5px">{title}</b><span style="margin-left:auto">{right}</span></div>{inner}</div>')

    kv = lambda k, v: (f'<div class="row" style="justify-content:space-between;font-size:13.5px;padding:4px 0">'
                       f'<span style="color:{MUTED}">{k}</span><span>{v}</span></div>')
    switch = lambda on: (f'<span style="width:44px;height:26px;border-radius:13px;background:{"#F6821F" if on else "#182233"};'
                         f'border:1px solid {"#F6821F" if on else "#2B3B52"};position:relative;display:inline-block">'
                         f'<span style="position:absolute;top:3px;{"right:3px" if on else "left:3px"};width:18px;height:18px;border-radius:50%;'
                         f'background:{"#1A0E05" if on else "#5B6B7F"}"></span></span>')
    verified = f'<span style="color:{ONLINE}" class="row">{ic("circle-check", ONLINE, 15)}&nbsp;Verified</span>'
    account = section("cloud", "Cloudflare account",
                      kv("Account", "KRS Tech") + kv("Sign-in", '<span class="badge">BROWSER LOGIN</span>')
                      + kv("Zone", "krstechsolutions.in") + kv("Status", verified)
                      + f'<div class="row" style="gap:8px;margin-top:10px"><span class="btn out" style="height:36px">{ic("globe", GLOW, 16)}Zones</span>'
                        f'<span class="btn out" style="height:36px">{ic("refresh-cw", GLOW, 16)}Refresh</span>'
                        f'<span style="margin-left:auto;color:{ERROR};font-size:14px;font-weight:500">Sign out</span></div>')
    engine = section("package-check", "Engine",
                     kv("cloudflared", '<span class="mono" style="color:#E6EDF3">2026.10.3</span>')
                     + kv("Source", '<span class="badge">BUNDLED</span>&nbsp;<span class="badge">ARM64-V8A</span>')
                     + kv("Status", f'<span style="color:{ONLINE}">Ready</span>'),
                     f'<span class="btn out" style="height:32px;padding:0 10px;font-size:13px">Check latest</span>')
    pocket = section("server", "Pocket Server",
                     kv("Address", '<span class="mono">http://127.0.0.1:8080</span>') + kv("Requests", "42")
                     + f'<div class="row" style="gap:8px;margin-top:10px"><span class="btn out" style="height:36px">{ic("upload", GLOW, 16)}Share files</span>'
                       f'<span class="btn fill" style="height:36px">{ic("zap", ON_ACCENT, 16)}Tunnel it</span></div>', switch(True))
    seg = ''.join(f'<span style="flex:1;text-align:center;padding:7px 0;font-size:13px;font-weight:500;'
                  f'{"background:rgba(246,130,31,.2);color:#FFAD5C" if i == 0 else "color:#8B98A9"}">{t}</span>'
                  for i, t in enumerate(["Auto", "QUIC", "HTTP/2"]))
    settings = section("settings", "Settings",
                       f'<div style="font-size:13px;color:{MUTED};margin-bottom:6px">Protocol</div>'
                       f'<div style="display:flex;border:1px solid #2B3B52;border-radius:12px;overflow:hidden;margin-bottom:8px">{seg}</div>'
                       + "".join(f'<div class="row" style="justify-content:space-between;padding:7px 0;font-size:14px">{t}{switch(on)}</div>'
                                 for t, on in [("Auto-restart", True), ("Start on boot", True), ("Keep awake", False)]))
    body = f"""<div style="padding:10px 0 14px"><div class="over">Account · engine · settings</div><div class="h1">Cloud</div></div>
{account}{engine}{pocket}{settings}"""
    return phone(body, "Cloud")


def page(phones, roboto, mono):
    return (f'<!doctype html><html><head><meta charset="utf-8"><style>{css(roboto, mono)}</style></head>'
            f'<body><div class="stage">{"".join(phones)}</div></body></html>')


def main():
    out, roboto, mono = sys.argv[1], sys.argv[2], sys.argv[3]
    screens = {"chat": chat(), "radar": radar(), "tunnels": tunnels(), "cloud": cloud()}
    for name, html in screens.items():
        with open(os.path.join(out, f"{name}.html"), "w") as f:
            f.write(page([html], roboto, mono))
    with open(os.path.join(out, "all.html"), "w") as f:
        f.write(page(list(screens.values()), roboto, mono))


if __name__ == "__main__":
    main()
