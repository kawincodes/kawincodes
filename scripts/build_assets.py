#!/usr/bin/env python3
"""
KAWIN.OS - asset generator for the GitHub profile README.

Builds every custom animated SVG panel used by README.md:

    assets/hero.svg              animated HUD banner
    assets/terminal.svg          typed `whoami` terminal session
    assets/status.svg            "system status" dashboard
    assets/footer.svg            end-of-transmission footer
    assets/sections/*.svg        section headers (dark + light variants)

Standard library only. Edit the CONTENT block below, then run:

    python3 scripts/build_assets.py

Rendering notes
- An SVG shown through <img> cannot load web fonts or scripts, so all text
  uses system font stacks plus `textLength`, which keeps the layout identical
  on macOS, Windows, Linux and mobile.
- CSS animations only run under `prefers-reduced-motion: no-preference`.
  SMIL typewriters are swapped for a static frame when reduced motion is on.
"""
from __future__ import annotations

import math
import random
from html import escape
from pathlib import Path
from string import Template

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "assets"

# ════════════════════════════════════════════════════════════════════════
#  CONTENT - everything a human might want to edit lives here
# ════════════════════════════════════════════════════════════════════════

NAME = "P KAWIN RAJU"
ROLES = ["FULL-STACK DEVELOPER", "AI BUILDER", "CLOUD ENTHUSIAST"]
ROTATOR = [
    "Designing reliable full-stack products",
    "Building AI-powered developer experiences",
    "Automating workflows from idea to deployment",
    "Learning in public and shipping with purpose",
]
CHIPS = [  # (label, palette token)
    ("OPEN TO COLLABORATE", "ok"),
    ("AI · CLOUD · AUTOMATION", "accent"),
    ("SHIP · LEARN · IMPROVE", "secondary"),
]
MODULES = ["FULL-STACK", "REST APIs", "AI/LLM", "CLOUD-NATIVE", "AUTOMATION", "SAAS"]
TIMEZONE = "IST UTC+05:30"

PROMPT_USER, PROMPT_HOST = "kawin", "kawin-os"
# (kind, payload): "cmd" -> typed command, "out" -> list of (text, colour, bold)
TERMINAL = [
    ("cmd", "whoami"),
    ("out", [("P Kawin Raju", "text", True), (" // ", "dim", False),
             ("Full-Stack Developer", "primary", False), (" · ", "accent", False),
             ("AI Builder", "primary", False), (" · ", "accent", False),
             ("Cloud Enthusiast", "primary", False)]),
    ("cmd", "cat mission.txt"),
    ("out", [("I turn practical ideas into dependable software products.", "text", False)]),
    ("cmd", "ls ~/focus"),
    ("out", [("full-stack/", "primary", True), ("  ", "text", False),
             ("rest-apis/", "secondary", True), ("  ", "text", False),
             ("ai-llm/", "accent", True), ("  ", "text", False),
             ("automation/", "primary", True), ("  ", "text", False),
             ("cloud-native/", "secondary", True), ("  ", "text", False),
             ("saas/", "accent", True)]),
    ("cmd", "./ship --thoughtfully --learn --improve"),
    ("out", [("[ OK ]", "ok", True), (" ship thoughtfully   ", "text", False),
             ("[ OK ]", "ok", True), (" learn continuously   ", "text", False),
             ("[ OK ]", "ok", True), (" improve relentlessly", "text", False)]),
    ("cmd", "echo $STATUS"),
    ("out", [("Open to thoughtful collaborations", "ok", True), ("  ->  ", "dim", False),
             ("kawin@krstechsolutions.in", "accent", False)]),
    ("cmd", ""),
]

SECTIONS = [  # (slug, title, subtitle)  -> assets/sections/NN-slug-{dark,light}.svg
    ("whoami", "WHOAMI", "identity.sys · who I am & what I build"),
    ("capabilities", "CAPABILITIES", "modules.core · what I can deliver"),
    ("arsenal", "ARSENAL", "stack.lock · languages, frameworks, cloud & AI"),
    ("pipeline", "PIPELINE", "workflow.yml · how ideas become shipped software"),
    ("missions", "ACTIVE MISSIONS", "status.live · current direction & research queue"),
    ("telemetry", "TELEMETRY", "metrics.stream · live GitHub analytics"),
    ("mission-select", "MISSION SELECT", "options.menu · pick a path, launch a brief"),
    ("knowledge-base", "KNOWLEDGE BASE", "faq.db · quick answers"),
    ("comms", "COMMS", "uplink.cfg · open a channel"),
]

MISSIONS = [  # (title, outcome)
    ("AI-NATIVE PRODUCTS", "useful AI on reliable architecture"),
    ("SAAS FOUNDATIONS", "secure multi-tenant boundaries"),
    ("PLATFORM AUTOMATION", "repeatable builds, tests & deploys"),
    ("CLOUD-NATIVE ENGINEERING", "resilient, observable services"),
    ("OPEN-SOURCE LEARNING", "contributions & shared knowledge"),
]
RESEARCH = [
    ("AGENTS & RETRIEVAL", "evaluation · guardrails · RAG"),
    ("RUST BACKENDS", "safe and efficient services"),
    ("KUBERNETES OPS", "operations & observability"),
    ("EVENT-DRIVEN SYSTEMS", "real-time architectures"),
    ("SECURE SAAS", "multi-tenant by design"),
]
UPLINK = [
    ("PROJECTS", "kawin@krstechsolutions.in"),
    ("GENERAL", "kawinraju@gmail.com"),
    ("BASE", "India · IST (UTC+05:30)"),
    ("MODES", "13 collaboration options"),
    ("NODES", "@kawincodes · @kawinthedev"),
]

CLOSING = [
    "Thanks for stopping by!",
    "Explore the work, share an idea, or say hello.",
    "Let's build something useful together.",
]

# ════════════════════════════════════════════════════════════════════════
#  DESIGN TOKENS
# ════════════════════════════════════════════════════════════════════════

MONO = ("'JetBrains Mono','Fira Code','Cascadia Code','SF Mono',SFMono-Regular,"
        "ui-monospace,Menlo,Consolas,'Liberation Mono','DejaVu Sans Mono',monospace")
DISPLAY = ("'Orbitron','Rajdhani','Bahnschrift','DIN Alternate','Eurostile',"
           "'Segoe UI','Helvetica Neue',Arial,sans-serif")
MONO_RATIO = 0.6  # advance width of one monospace glyph, in em

# Theme: neon-green "phosphor terminal" HUD with amber accents (no pink / blue).
# Every colour in every panel comes from these two dicts - retheme here.
DARK = dict(
    void="#010402", deep="#03100A", panel="#061A10", panel2="#0A2416",
    edge="#16402A", grid="#1D5A3A",
    text="#E8FFF3", muted="#7FA592", dim="#3E6652",
    primary="#00FF9C",     # neon phosphor green
    glow="#A7FFD8",        # bright mint highlight
    secondary="#B6FF3B",   # acid lime
    secondary2="#6FBF00",  # deep lime (gradients)
    accent="#FFB000",      # amber
    hot="#FF6A00",         # ember orange (glitch / hot spots)
    ok="#3DFF8A",          # status green
)
LIGHT = dict(  # deeper tones that stay readable on GitHub's white background
    text="#1F2328", muted="#57606A", dim="#AFB8C1", edge="#D0D7DE",
    primary="#047857", glow="#065F46", secondary="#4D7C0F", secondary2="#3F6212",
    accent="#B45309", hot="#C2410C", ok="#15803D",
)

# ════════════════════════════════════════════════════════════════════════
#  HELPERS
# ════════════════════════════════════════════════════════════════════════


def t(s: str) -> str:
    """Escape text content."""
    return escape(s, quote=False)


def css(template: str, **kw) -> str:
    return Template(template).substitute(**kw)


def num(v: float) -> str:
    s = f"{v:.2f}".rstrip("0").rstrip(".")
    return s if s not in ("-0", "") else "0"


def doc(w: int, h: int, title: str, desc: str, style: str, defs: str, body: str) -> str:
    return (
        f'<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" '
        f'viewBox="0 0 {w} {h}" role="img" aria-labelledby="title desc">\n'
        f"<title id=\"title\">{t(title)}</title>\n<desc id=\"desc\">{t(desc)}</desc>\n"
        f"<style>{style}</style>\n<defs>{defs}</defs>\n{body}\n</svg>\n"
    )


def discrete(attr: str, events: list[tuple[float, float]], total: float) -> str:
    """SMIL step animation: each (time_s, value) holds until the next event."""
    keys: list[tuple[float, float]] = []
    for time_s, value in sorted(events, key=lambda e: e[0]):
        k = round(time_s / total, 5)
        if keys and k <= keys[-1][0]:
            keys[-1] = (keys[-1][0], value)
            continue
        if keys and value == keys[-1][1]:
            continue
        keys.append((k, value))
    if not keys or keys[0][0] != 0:
        raise ValueError("discrete animation needs an event at t=0")
    kt = ";".join(f"{k:.5f}" for k, _ in keys)
    vals = ";".join(num(v) for _, v in keys)
    return (f'<animate attributeName="{attr}" dur="{total:.3f}s" repeatCount="indefinite" '
            f'calcMode="discrete" keyTimes="{kt}" values="{vals}"/>')


def spans(segs, pal) -> str:
    out = []
    for text, colour, bold in segs:
        weight = ' font-weight="700"' if bold else ""
        out.append(f'<tspan fill="{pal[colour]}"{weight}>{t(text)}</tspan>')
    return "".join(out)


def mono_text(segs, x, y, size, pal, extra="") -> tuple[str, int]:
    """Monospace text pinned to an exact character grid via textLength."""
    n = sum(len(s[0]) for s in segs)
    width = n * size * MONO_RATIO
    return (f'<text x="{num(x)}" y="{num(y)}" font-size="{size}" textLength="{num(width)}" '
            f'lengthAdjust="spacing" xml:space="preserve"{extra}>{spans(segs, pal)}</text>', n)


def hexagon(cx: float, cy: float, r: float) -> str:
    pts = []
    for k in range(6):
        a = math.radians(60 * k + 30)
        pts.append(f"{num(cx + r * math.cos(a))},{num(cy + r * math.sin(a))}")
    return " ".join(pts)


# Advance widths (em) of a wide bold sans (DejaVu Sans Bold). Sizing display text
# for the widest likely fallback means narrower faces (DIN, Bahnschrift, Arial)
# simply get a little extra tracking instead of overlapping glyphs.
_DISPLAY_EM = dict(zip("ABCDEFGHIJKLMNOPQRSTUVWXYZ ", [
    .774, .763, .732, .827, .683, .683, .824, .837, .372, .372, .776, .638, .996,
    .837, .850, .732, .850, .771, .723, .686, .813, .774, 1.102, .765, .727, .732, .348]))


def display_width(text: str, size: float, tracking: float = 0.1) -> float:
    """Width for `textLength` on uppercase display text, incl. letter tracking (em)."""
    em = sum(_DISPLAY_EM.get(ch, .75) for ch in text.upper())
    return (em + tracking * (len(text) - 1)) * size


REDUCED_MOTION = """
.rm{display:none}
@media (prefers-reduced-motion: reduce){.am{display:none}.rm{display:inline}}
"""

# ════════════════════════════════════════════════════════════════════════
#  HERO
# ════════════════════════════════════════════════════════════════════════


def hero() -> str:
    P = DARK
    W, H, HZ = 1280, 440, 372          # canvas + horizon line
    cx, cy = 1040, 216                 # reactor centre
    rnd = random.Random(2026)

    style = css("""
text{font-family:$mono}
.disp,.disp text{font-family:$display;font-weight:700}
$rm
@media (prefers-reduced-motion: no-preference){
 .tw{animation:tw var(--d,3s) ease-in-out infinite;animation-delay:var(--dl,0s)}
 .hl{animation:hl 2.8s cubic-bezier(.55,.05,.9,.45) infinite}
 .spin{transform-box:view-box;transform-origin:${cx}px ${cy}px;animation:spin var(--d,60s) linear infinite}
 .rev{animation-direction:reverse}
 .pulse{transform-box:view-box;transform-origin:${cx}px ${cy}px;animation:pulse 2.6s ease-out infinite}
 .blip{animation:blip 4s linear infinite;animation-delay:var(--dl,0s)}
 .blink{animation:blink 1.1s steps(1) infinite}
 .sweep{animation:sweep 7s linear infinite}
 .glc{animation:glc 5.5s steps(1) infinite}
 .glm{animation:glm 5.5s steps(1) infinite}
 .slice{animation:slice 5.5s steps(1) infinite}
 .flick{animation:flick 5.5s linear infinite}
 .eq{transform-box:fill-box;transform-origin:50% 100%;animation:eq var(--d,1s) ease-in-out infinite alternate}
 .dash{animation:dash 6s linear infinite}
}
@keyframes tw{0%,100%{opacity:.15}50%{opacity:1}}
@keyframes hl{0%{transform:translateY(0);opacity:0}12%{opacity:.85}100%{transform:translateY(${floor}px);opacity:.95}}
@keyframes spin{to{transform:rotate(360deg)}}
@keyframes pulse{0%{transform:scale(.8);opacity:.75}100%{transform:scale(1.5);opacity:0}}
@keyframes blip{0%,100%{opacity:0}6%{opacity:1}35%{opacity:0}}
@keyframes blink{0%{opacity:1}50%{opacity:0}}
@keyframes sweep{from{transform:translateY(0)}to{transform:translateY(${sweep}px)}}
@keyframes glc{0%{transform:translate(-2px,0)}86%{transform:translate(-8px,-2px)}88%{transform:translate(6px,2px)}90%{transform:translate(-4px,1px)}92%{transform:translate(3px,-1px)}94%{transform:translate(-2px,0)}}
@keyframes glm{0%{transform:translate(2px,0)}86%{transform:translate(8px,2px)}88%{transform:translate(-6px,-1px)}90%{transform:translate(4px,0)}92%{transform:translate(-3px,1px)}94%{transform:translate(2px,0)}}
@keyframes slice{0%{opacity:0;transform:translateX(0)}86%{opacity:1;transform:translateX(16px)}88%{opacity:1;transform:translateX(-12px)}90%{opacity:1;transform:translateX(7px)}92%{opacity:0;transform:translateX(0)}}
@keyframes flick{0%,85%,100%{opacity:1}86%{opacity:.55}87%{opacity:1}89%{opacity:.7}90%{opacity:1}}
@keyframes eq{from{transform:scaleY(.25)}to{transform:scaleY(1)}}
@keyframes dash{to{stroke-dashoffset:-120}}
""", mono=MONO, display=DISPLAY, rm=REDUCED_MOTION, cx=cx, cy=cy, floor=H - HZ, sweep=H + 160)

    defs = f"""
<linearGradient id="bg" x1="0" y1="0" x2="0" y2="1">
  <stop offset="0" stop-color="#010503"/><stop offset=".66" stop-color="#03100A"/><stop offset="1" stop-color="#081A0E"/>
</linearGradient>
<radialGradient id="nebC"><stop offset="0" stop-color="{P['primary']}" stop-opacity=".20"/><stop offset="1" stop-color="{P['primary']}" stop-opacity="0"/></radialGradient>
<radialGradient id="nebV"><stop offset="0" stop-color="#00A86B" stop-opacity=".24"/><stop offset="1" stop-color="#00A86B" stop-opacity="0"/></radialGradient>
<radialGradient id="nebM"><stop offset="0" stop-color="{P['hot']}" stop-opacity=".16"/><stop offset="1" stop-color="{P['hot']}" stop-opacity="0"/></radialGradient>
<radialGradient id="coreGlow"><stop offset="0" stop-color="{P['primary']}" stop-opacity=".30"/><stop offset=".55" stop-color="{P['secondary2']}" stop-opacity=".10"/><stop offset="1" stop-color="{P['secondary2']}" stop-opacity="0"/></radialGradient>
<radialGradient id="horizon" cx=".5" cy=".5" r=".5"><stop offset="0" stop-color="{P['hot']}" stop-opacity=".45"/><stop offset=".5" stop-color="{P['secondary2']}" stop-opacity=".18"/><stop offset="1" stop-color="{P['secondary2']}" stop-opacity="0"/></radialGradient>
<linearGradient id="titleFill" x1="0" y1="0" x2="1" y2="0"><stop offset="0" stop-color="#FFFFFF"/><stop offset=".6" stop-color="#DFFFEE"/><stop offset="1" stop-color="{P['glow']}"/></linearGradient>
<linearGradient id="rule" gradientUnits="userSpaceOnUse" x1="40" y1="0" x2="{W - 40}" y2="0"><stop offset="0" stop-color="{P['primary']}" stop-opacity="0"/><stop offset=".15" stop-color="{P['primary']}" stop-opacity=".55"/><stop offset=".85" stop-color="{P['secondary']}" stop-opacity=".55"/><stop offset="1" stop-color="{P['secondary']}" stop-opacity="0"/></linearGradient>
<linearGradient id="hline" gradientUnits="userSpaceOnUse" x1="0" y1="0" x2="{W}" y2="0"><stop offset="0" stop-color="{P['secondary']}" stop-opacity="0"/><stop offset=".5" stop-color="{P['accent']}" stop-opacity=".9"/><stop offset="1" stop-color="{P['secondary']}" stop-opacity="0"/></linearGradient>
<linearGradient id="floorFade" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#03140B" stop-opacity="1"/><stop offset="1" stop-color="#03140B" stop-opacity="0"/></linearGradient>
<linearGradient id="band" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{P['void']}" stop-opacity="0"/><stop offset=".55" stop-color="{P['void']}" stop-opacity=".82"/><stop offset="1" stop-color="{P['void']}" stop-opacity=".92"/></linearGradient>
<linearGradient id="sweepFill" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{P['primary']}" stop-opacity="0"/><stop offset=".5" stop-color="{P['primary']}" stop-opacity=".06"/><stop offset="1" stop-color="{P['primary']}" stop-opacity="0"/></linearGradient>
<linearGradient id="wedge" gradientUnits="userSpaceOnUse" x1="{cx}" y1="{cy}" x2="{cx}" y2="{cy - 80}"><stop offset="0" stop-color="{P['primary']}" stop-opacity=".42"/><stop offset="1" stop-color="{P['primary']}" stop-opacity="0"/></linearGradient>
<pattern id="dots" width="24" height="24" patternUnits="userSpaceOnUse"><circle cx="1.5" cy="1.5" r="1" fill="#1D4A33" fill-opacity=".55"/></pattern>
<pattern id="scan" width="4" height="4" patternUnits="userSpaceOnUse"><rect width="4" height="1.2" fill="#000" fill-opacity=".16"/></pattern>
<filter id="glow" x="-30%" y="-80%" width="160%" height="260%"><feGaussianBlur stdDeviation="5" result="b"/><feMerge><feMergeNode in="b"/><feMergeNode in="SourceGraphic"/></feMerge></filter>
<filter id="glowSm" x="-50%" y="-50%" width="200%" height="200%"><feGaussianBlur stdDeviation="2.2" result="b"/><feMerge><feMergeNode in="b"/><feMergeNode in="SourceGraphic"/></feMerge></filter>
<clipPath id="frame"><rect width="{W}" height="{H}" rx="22"/></clipPath>
<clipPath id="floorClip"><rect x="0" y="{HZ}" width="{W}" height="{H - HZ}"/></clipPath>
<clipPath id="sliceA"><rect x="0" y="146" width="{W}" height="16"/></clipPath>
<clipPath id="sliceB"><rect x="0" y="170" width="{W}" height="9"/></clipPath>
"""

    b: list[str] = []
    # ── backdrop ─────────────────────────────────────────────────────────
    b.append(f'<rect width="{W}" height="{H}" fill="url(#bg)"/>')
    b.append(f'<rect width="{W}" height="{HZ}" fill="url(#dots)" opacity=".55"/>')
    b.append('<ellipse cx="300" cy="70" rx="420" ry="240" fill="url(#nebC)"/>')
    b.append('<ellipse cx="1010" cy="300" rx="460" ry="300" fill="url(#nebV)"/>')
    b.append('<ellipse cx="640" cy="440" rx="520" ry="170" fill="url(#nebM)"/>')
    stars = []
    for _ in range(64):
        x, y = rnd.uniform(16, W - 16), rnd.uniform(66, HZ - 14)
        r = rnd.choice([0.6, 0.8, 0.8, 1.1, 1.4])
        d, dl = rnd.uniform(2.2, 5.5), -rnd.uniform(0, 5)
        stars.append(f'<circle class="tw" style="--d:{d:.1f}s;--dl:{dl:.1f}s" cx="{x:.0f}" cy="{y:.0f}" r="{r}"/>')
    b.append(f'<g fill="#DCFFEC">{"".join(stars)}</g>')

    # ── synth grid floor ─────────────────────────────────────────────────
    b.append(f'<ellipse cx="640" cy="{HZ}" rx="760" ry="64" fill="url(#horizon)"/>')
    vp_y = HZ - 64
    verticals = "".join(
        f'<line x1="640" y1="{vp_y}" x2="{640 + k * 96}" y2="{H}"/>' for k in range(-19, 20))
    b.append(f'<g clip-path="url(#floorClip)" stroke="{P["primary"]}" stroke-opacity=".42" stroke-width="1.2">{verticals}</g>')
    moving = "".join(
        f'<line class="hl" style="animation-delay:-{i * 0.4:.1f}s" x1="0" y1="{HZ}" x2="{W}" y2="{HZ}"/>'
        for i in range(7))
    static = "".join(
        f'<line x1="0" y1="{HZ + (H - HZ) * (i / 7) ** 2.2:.1f}" x2="{W}" y2="{HZ + (H - HZ) * (i / 7) ** 2.2:.1f}"/>'
        for i in range(1, 8))
    b.append(f'<g stroke="url(#hline)" stroke-width="1.4"><g class="am">{moving}</g><g class="rm">{static}</g></g>')
    b.append(f'<rect x="0" y="{HZ}" width="{W}" height="26" fill="url(#floorFade)"/>')
    b.append(f'<line x1="0" y1="{HZ}" x2="{W}" y2="{HZ}" stroke="url(#hline)" stroke-width="2" filter="url(#glowSm)"/>')

    # ── reactor core ─────────────────────────────────────────────────────
    r_ = []
    r_.append(f'<circle cx="{cx}" cy="{cy}" r="190" fill="url(#coreGlow)"/>')
    r_.append(f'<circle cx="{cx}" cy="{cy}" r="150" stroke="{P["primary"]}" stroke-opacity=".35" stroke-width="1.2" fill="#020A05" fill-opacity=".55"/>')
    ticks = []
    for i in range(72):
        a = math.radians(i * 5)
        r1 = 133 if i % 6 == 0 else 141
        ticks.append(f'<line x1="{num(cx + r1 * math.cos(a))}" y1="{num(cy + r1 * math.sin(a))}" '
                     f'x2="{num(cx + 149 * math.cos(a))}" y2="{num(cy + 149 * math.sin(a))}"/>')
    r_.append(f'<g class="spin" style="--d:80s" stroke="{P["primary"]}" stroke-opacity=".6" stroke-width="1.4">{"".join(ticks)}</g>')
    r_.append(f'<circle class="spin rev" style="--d:36s" cx="{cx}" cy="{cy}" r="124" stroke="{P["secondary"]}" stroke-width="2" stroke-dasharray="3 9" stroke-opacity=".9"/>')
    arcs = []
    for k in range(3):
        a0, a1 = math.radians(k * 120), math.radians(k * 120 + 52)
        arcs.append(f'<path d="M{num(cx + 108 * math.cos(a0))} {num(cy + 108 * math.sin(a0))} '
                    f'A108 108 0 0 1 {num(cx + 108 * math.cos(a1))} {num(cy + 108 * math.sin(a1))}"/>')
    r_.append(f'<g class="spin" style="--d:14s" stroke="{P["primary"]}" stroke-width="4" stroke-linecap="round" filter="url(#glowSm)">{"".join(arcs)}</g>')
    r_.append(f'<circle cx="{cx}" cy="{cy}" r="88" stroke="{P["dim"]}" stroke-width="1"/>')
    r_.append(f'<g stroke="{P["dim"]}" stroke-width="1">'
              f'<line x1="{cx - 100}" y1="{cy}" x2="{cx - 60}" y2="{cy}"/><line x1="{cx + 60}" y1="{cy}" x2="{cx + 100}" y2="{cy}"/>'
              f'<line x1="{cx}" y1="{cy - 100}" x2="{cx}" y2="{cy - 60}"/><line x1="{cx}" y1="{cy + 60}" x2="{cx}" y2="{cy + 100}"/></g>')
    wedge_a = math.radians(-50)
    r_.append(f'<g class="spin" style="--d:4s">'
              f'<path d="M{cx} {cy} L{cx + 100} {cy} A100 100 0 0 0 {num(cx + 100 * math.cos(wedge_a))} {num(cy + 100 * math.sin(wedge_a))} Z" fill="url(#wedge)"/>'
              f'<line x1="{cx}" y1="{cy}" x2="{cx + 100}" y2="{cy}" stroke="{P["glow"]}" stroke-width="2" filter="url(#glowSm)"/></g>')
    for ang, rad in ((35, 70), (140, 92), (215, 58), (290, 84), (330, 40)):
        a = math.radians(ang)
        delay = -(1 - ang / 360) * 4
        r_.append(f'<circle class="blip" style="--dl:{delay:.2f}s" cx="{num(cx + rad * math.cos(a))}" '
                  f'cy="{num(cy + rad * math.sin(a))}" r="3.2" fill="{P["glow"]}" filter="url(#glowSm)"/>')
    r_.append(f'<circle class="pulse" cx="{cx}" cy="{cy}" r="52" stroke="{P["primary"]}" stroke-width="1.6"/>')
    r_.append(f'<polygon points="{hexagon(cx, cy, 46)}" fill="#04130B" stroke="{P["primary"]}" stroke-width="2.2" filter="url(#glowSm)"/>')
    r_.append(f'<polygon class="spin rev" style="--d:24s" points="{hexagon(cx, cy, 35)}" stroke="{P["secondary"]}" stroke-opacity=".75" stroke-width="1.3" stroke-dasharray="6 4"/>')
    r_.append(f'<text x="{cx}" y="{cy + 10}" text-anchor="middle" font-size="28" font-weight="700" fill="{P["glow"]}" filter="url(#glowSm)">&lt;/&gt;</text>')
    r_.append(f'<g class="spin" style="--d:12s"><circle cx="{cx + 150}" cy="{cy}" r="4.5" fill="{P["glow"]}" filter="url(#glowSm)"/></g>')
    r_.append(f'<g class="spin rev" style="--d:9s"><circle cx="{cx}" cy="{cy - 124}" r="3.6" fill="{P["hot"]}" filter="url(#glowSm)"/></g>')
    b.append("".join(r_))
    # reactor read-outs
    b.append(f'<text x="{cx - 168}" y="88" font-size="12" fill="{P["muted"]}" textLength="138" lengthAdjust="spacing">'
             f'SYS.CORE <tspan fill="{P["ok"]}">▸ ONLINE</tspan></text>')
    eq = []
    for i, d in enumerate((0.9, 0.6, 1.2, 0.75, 1.0, 0.55)):
        eq.append(f'<rect class="eq" style="--d:{d}s" x="{cx + 118 + i * 7}" y="72" width="4" height="16" rx="1" fill="{P["primary"]}" fill-opacity=".85"/>')
    b.append("".join(eq))
    b.append(f'<text x="{cx + 118}" y="102" font-size="11" fill="{P["muted"]}" textLength="46" lengthAdjust="spacing">SIGNAL</text>')

    # ── HUD: top bar ─────────────────────────────────────────────────────
    b.append(f'<text x="40" y="40" font-size="14" xml:space="preserve">'
             f'<tspan fill="{P["primary"]}" font-weight="700">◢ KAWIN.OS</tspan>'
             f'<tspan fill="{P["muted"]}">   v2026.11  //  PROFILE.SYS  //  </tspan>'
             f'<tspan fill="{P["secondary"]}">IDENTITY VERIFIED</tspan></text>')
    b.append(f'<text x="1222" y="40" font-size="14" text-anchor="end" xml:space="preserve" fill="{P["muted"]}">'
             f'{t(TIMEZONE)}  ·  NODE IN  ·  <tspan fill="{P["ok"]}">LINK SECURE</tspan></text>')
    b.append(f'<circle class="blink" cx="1236" cy="35" r="4.5" fill="{P["ok"]}" filter="url(#glowSm)"/>')
    rule_ticks = "".join(
        f'<line x1="{x}" y1="56" x2="{x}" y2="{52 if (x - 40) % 100 == 0 else 54}"/>' for x in range(40, W - 39, 20))
    b.append(f'<line x1="40" y1="56" x2="{W - 40}" y2="56" stroke="url(#rule)" stroke-width="1"/>'
             f'<g stroke="{P["primary"]}" stroke-opacity=".35" stroke-width="1">{rule_ticks}</g>')

    # ── identity block ───────────────────────────────────────────────────
    x0 = 64
    b.append(f'<text x="{x0}" y="110" font-size="14" textLength="420" lengthAdjust="spacing" xml:space="preserve">'
             f'<tspan fill="{P["ok"]}">▸ BOOT SEQUENCE COMPLETE</tspan><tspan fill="{P["muted"]}">  ·  WELCOME, OPERATOR</tspan></text>')
    title_w = display_width(NAME, 76, 0.08)
    title_attrs = f'x="{x0}" y="188" font-size="76" textLength="{num(title_w)}" lengthAdjust="spacing"'
    b.append(f'<g class="disp">'
             f'<text class="glc" transform="translate(-2 0)" {title_attrs} fill="{P["primary"]}" fill-opacity=".8" style="mix-blend-mode:screen">{t(NAME)}</text>'
             f'<text class="glm" transform="translate(2 0)" {title_attrs} fill="{P["hot"]}" fill-opacity=".75" style="mix-blend-mode:screen">{t(NAME)}</text>'
             f'<text class="flick" {title_attrs} fill="url(#titleFill)" filter="url(#glow)">{t(NAME)}</text>'
             f'<g class="am" clip-path="url(#sliceA)"><text class="slice" opacity="0" {title_attrs} fill="{P["glow"]}">{t(NAME)}</text></g>'
             f'<g class="am" clip-path="url(#sliceB)"><text class="slice" opacity="0" {title_attrs} fill="{P["hot"]}" style="animation-delay:-.15s">{t(NAME)}</text></g>'
             f'</g>')
    role_segs = []
    for i, role in enumerate(ROLES):
        if i:
            role_segs.append((" · ", "accent", True))
        role_segs.append((role, "text", False))
    b.append(f'<text x="{x0 + 2}" y="230" font-size="19" fill-opacity=".9" textLength="{num(title_w - 2)}" lengthAdjust="spacing" '
             f'xml:space="preserve" fill="{P["text"]}">{spans(role_segs, P)}</text>')

    # typewriter rotator
    fs = 22
    cw = fs * MONO_RATIO
    prefix = [("> ", "accent", True)]
    px = x0 + 2 + 2 * cw
    timeline, cursor = 0.5, [(0.0, px)]
    clips, lines = [], []
    for i, line in enumerate(ROTATOR):
        ev = [(0.0, 0.0)]
        for k in range(1, len(line) + 1):
            timeline += rnd.uniform(0.045, 0.095)
            ev.append((timeline, k * cw))
            cursor.append((timeline, px + k * cw))
        timeline += 2.2
        for k in range(len(line) - 1, -1, -1):
            timeline += 0.022
            ev.append((timeline, k * cw))
            cursor.append((timeline, px + k * cw))
        timeline += 0.45
        clips.append((i, ev))
        lines.append(line)
    total = timeline
    for i, ev in clips:
        defs += (f'<clipPath id="rot{i}"><rect x="{num(px)}" y="{282 - fs}" width="0" height="{fs + 10}">'
                 f'{discrete("width", ev, total)}</rect></clipPath>')
    pre, _ = mono_text(prefix, x0 + 2, 282, fs, P)
    b.append(pre)
    anim = "".join(
        f'<text clip-path="url(#rot{i})" x="{num(px)}" y="282" font-size="{fs}" textLength="{num(len(line) * cw)}" '
        f'lengthAdjust="spacing" fill="{P["glow"]}">{t(line)}</text>' for i, line in enumerate(lines))
    caret = (f'<rect class="blink" x="{num(px)}" y="{282 - fs + 3}" width="{num(cw * 0.55)}" height="{fs + 1}" '
             f'fill="{P["glow"]}" fill-opacity=".9">{discrete("x", cursor, total)}</rect>')
    b.append(f'<g class="am">{anim}{caret}</g>')
    b.append(f'<g class="rm"><text x="{num(px)}" y="282" font-size="{fs}" textLength="{num(len(lines[0]) * cw)}" '
             f'lengthAdjust="spacing" fill="{P["glow"]}">{t(lines[0])}</text></g>')

    # status chips
    cx_ = x0
    for label, colour in CHIPS:
        glyph = {"ok": "●", "accent": "◆", "secondary": "▲"}[colour]
        text = f"{glyph} {label}"
        w = len(text) * 13 * MONO_RATIO + 28
        col = P[colour]
        b.append(f'<g><rect x="{num(cx_)}" y="306" width="{num(w)}" height="30" rx="5" fill="{col}" fill-opacity=".08" '
                 f'stroke="{col}" stroke-opacity=".55"/>'
                 f'<text x="{num(cx_ + 14)}" y="326" font-size="13" font-weight="700" fill="{col}" '
                 f'textLength="{num(len(text) * 13 * MONO_RATIO)}" lengthAdjust="spacing">{t(text)}</text></g>')
        cx_ += w + 12

    # ── HUD: bottom bar ──────────────────────────────────────────────────
    b.append(f'<rect x="0" y="388" width="{W}" height="{H - 388}" fill="url(#band)"/>')
    mods = [("MODULES ▸ ", "primary", True)]
    for i, m in enumerate(MODULES):
        if i:
            mods.append((" · ", "accent", False))
        mods.append((m, "muted", False))
    mod_svg, _ = mono_text(mods, 40, 422, 13, P)
    b.append(mod_svg)
    b.append(f'<text x="1094" y="422" font-size="13" text-anchor="end" fill="{P["muted"]}">BOOT</text>')
    b.append(f'<rect x="1102" y="412" width="92" height="11" rx="2" stroke="{P["primary"]}" stroke-opacity=".6" fill="none"/>')
    b.append(f'<rect x="1104" y="414" width="88" height="7" rx="1" fill="{P["primary"]}" fill-opacity=".85">'
             f'<animate attributeName="width" values="0;88;88" keyTimes="0;.3;1" dur="9s" repeatCount="indefinite"/></rect>')
    b.append(f'<text x="1240" y="422" font-size="13" font-weight="700" text-anchor="end" fill="{P["ok"]}">READY</text>')

    # ── overlays: sweep, scanlines, frame, corner brackets ───────────────
    b.append(f'<rect class="sweep" x="0" y="-160" width="{W}" height="160" fill="url(#sweepFill)"/>')
    b.append(f'<rect width="{W}" height="{H}" fill="url(#scan)"/>')
    body = f'<g clip-path="url(#frame)">{"".join(b)}</g>'
    body += f'<rect x=".75" y=".75" width="{W - 1.5}" height="{H - 1.5}" rx="21.5" stroke="{P["primary"]}" stroke-opacity=".35" stroke-width="1.5" fill="none"/>'
    L = 30
    corners = (f'M14 {14 + L}V14H{14 + L}', f'M{W - 14 - L} 14H{W - 14}V{14 + L}',
               f'M14 {H - 14 - L}V{H - 14}H{14 + L}', f'M{W - 14 - L} {H - 14}H{W - 14}V{H - 14 - L}')
    body += f'<g stroke="{P["primary"]}" stroke-width="2.5" stroke-linecap="round" fill="none" filter="url(#glowSm)">' + \
            "".join(f'<path d="{d}"/>' for d in corners) + "</g>"

    return doc(W, H, "KAWIN.OS — P Kawin Raju · Full-Stack Developer · AI Builder · Cloud Enthusiast",
               "Animated sci-fi HUD banner: glitching name title, rotating role typewriter, radar reactor core "
               "and a synthwave grid floor.", style, defs, body)


# ════════════════════════════════════════════════════════════════════════
#  TERMINAL
# ════════════════════════════════════════════════════════════════════════


def terminal() -> str:
    P = DARK
    W = 1000
    fs, lh = 18, 30
    cw = fs * MONO_RATIO
    x0, y0 = 30, 92
    H = y0 + (len(TERMINAL) - 1) * lh + 34
    rnd = random.Random(7)

    style = css("""
text{font-family:$mono}
$rm
@media (prefers-reduced-motion: no-preference){
 .blink{animation:blink 1.05s steps(1) infinite}
 .led{animation:led 2.4s ease-in-out infinite}
}
@keyframes blink{0%{opacity:1}50%{opacity:0}}
@keyframes led{0%,100%{opacity:1}50%{opacity:.35}}
""", mono=MONO, rm=REDUCED_MOTION)

    prompt = [(PROMPT_USER + "@" + PROMPT_HOST, "secondary", True), (":", "muted", False),
              ("~", "accent", True), ("$ ", "muted", False)]
    p_len = sum(len(s[0]) for s in prompt)

    # timeline ------------------------------------------------------------
    timeline = 0.6
    reveal: list[list[tuple[float, float]]] = []
    cur_x, cur_y = [(0.0, x0 + p_len * cw)], [(0.0, y0)]
    rows = []
    for i, (kind, payload) in enumerate(TERMINAL):
        y = y0 + i * lh
        ev = [(0.0, 0.0)]
        if kind == "cmd":
            segs = prompt + [(payload, "text", False)]
            ev.append((timeline, p_len * cw))
            cur_x.append((timeline, x0 + p_len * cw))
            cur_y.append((timeline, y))
            timeline += 0.55
            for k in range(1, len(payload) + 1):
                timeline += rnd.uniform(0.045, 0.11)
                ev.append((timeline, (p_len + k) * cw))
                cur_x.append((timeline, x0 + (p_len + k) * cw))
            timeline += 0.35 if payload else 0
        else:
            segs = payload
            n = sum(len(s[0]) for s in segs)
            ev.append((timeline, n * cw + 4))
            nxt = y0 + (i + 1) * lh
            cur_x.append((timeline, x0))
            cur_y.append((timeline, nxt))
            timeline += 0.45
        rows.append((segs, y))
        reveal.append(ev)
    end = timeline + 5.0
    total = end + 0.7
    for ev in reveal:
        ev.append((end, 0.0))
    cur_x.append((end, -50.0))

    defs = f"""
<linearGradient id="chrome" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#0B1C13"/><stop offset="1" stop-color="#07140D"/></linearGradient>
<linearGradient id="body" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#050E09"/><stop offset="1" stop-color="#030A06"/></linearGradient>
<radialGradient id="glare" cx=".85" cy="0" r=".9"><stop offset="0" stop-color="{P['secondary2']}" stop-opacity=".16"/><stop offset="1" stop-color="{P['secondary2']}" stop-opacity="0"/></radialGradient>
<radialGradient id="glare2" cx="0" cy="1" r=".8"><stop offset="0" stop-color="{P['primary']}" stop-opacity=".09"/><stop offset="1" stop-color="{P['primary']}" stop-opacity="0"/></radialGradient>
<pattern id="scan" width="4" height="4" patternUnits="userSpaceOnUse"><rect width="4" height="1" fill="#000" fill-opacity=".18"/></pattern>
<filter id="glowSm" x="-50%" y="-50%" width="200%" height="200%"><feGaussianBlur stdDeviation="2" result="b"/><feMerge><feMergeNode in="b"/><feMergeNode in="SourceGraphic"/></feMerge></filter>
<clipPath id="win"><rect width="{W}" height="{H}" rx="14"/></clipPath>
"""
    for i, ev in enumerate(reveal):
        defs += (f'<clipPath id="ln{i}"><rect x="{x0 - 2}" y="{y0 + i * lh - fs}" width="0" height="{lh}">'
                 f'{discrete("width", ev, total)}</rect></clipPath>')

    b = [f'<rect width="{W}" height="{H}" fill="url(#body)"/>',
         f'<rect width="{W}" height="{H}" fill="url(#glare)"/>',
         f'<rect width="{W}" height="{H}" fill="url(#glare2)"/>',
         f'<rect width="{W}" height="46" fill="url(#chrome)"/>',
         f'<line x1="0" y1="46" x2="{W}" y2="46" stroke="{P["edge"]}"/>']
    for i, colour in enumerate(("#FF5F57", "#FEBC2E", "#28C840")):
        b.append(f'<circle cx="{26 + i * 22}" cy="23" r="6.5" fill="{colour}"/>')
    b.append(f'<text x="{W / 2}" y="28" font-size="14" text-anchor="middle" fill="{P["muted"]}">'
             f'{PROMPT_USER}@{PROMPT_HOST}: ~ — zsh — 100×30</text>')
    b.append(f'<circle class="led" cx="{W - 120}" cy="23" r="4" fill="{P["ok"]}" filter="url(#glowSm)"/>'
             f'<text x="{W - 108}" y="28" font-size="13" fill="{P["ok"]}" font-weight="700">SSH · LIVE</text>')

    anim, still = [], []
    for i, (segs, y) in enumerate(rows):
        txt, _ = mono_text(segs, x0, y, fs, P)
        anim.append(f'<g clip-path="url(#ln{i})">{txt}</g>')
        still.append(txt)
    caret_y0 = y0 - fs + 4
    cursor = (f'<rect class="blink" x="{num(x0 + p_len * cw)}" y="{caret_y0}" width="{num(cw)}" height="{fs + 3}" '
              f'fill="{P["glow"]}" fill-opacity=".85">{discrete("x", cur_x, total)}'
              f'{discrete("y", [(tt, yy - fs + 4) for tt, yy in cur_y], total)}</rect>')
    b.append(f'<g class="am">{"".join(anim)}{cursor}</g>')
    last_y = y0 + (len(rows) - 1) * lh
    b.append(f'<g class="rm">{"".join(still)}<rect x="{num(x0 + p_len * cw)}" y="{last_y - fs + 4}" '
             f'width="{num(cw)}" height="{fs + 3}" fill="{P["glow"]}" fill-opacity=".85"/></g>')
    b.append(f'<rect width="{W}" height="{H}" fill="url(#scan)"/>')

    body = f'<g clip-path="url(#win)">{"".join(b)}</g>'
    body += (f'<rect x=".75" y=".75" width="{W - 1.5}" height="{H - 1.5}" rx="13.5" fill="none" '
             f'stroke="{P["primary"]}" stroke-opacity=".28" stroke-width="1.5"/>')
    return doc(W, H, "Terminal — whoami: P Kawin Raju",
               "An animated terminal session: whoami, cat mission.txt, ls ~/focus, ./ship and echo $STATUS.",
               style, defs, body)


# ════════════════════════════════════════════════════════════════════════
#  STATUS BOARD
# ════════════════════════════════════════════════════════════════════════


def status_board() -> str:
    P = DARK
    W, H = 1280, 418
    pw, gap, top = 400, 16, 76
    ph = H - top - 22

    style = css("""
text{font-family:$mono}
.disp,.disp text{font-family:$display;font-weight:700}
@media (prefers-reduced-motion: no-preference){
 .shimmer{animation:shimmer var(--d,2.4s) ease-in-out infinite;animation-delay:var(--dl,0s)}
 .spin{transform-box:fill-box;transform-origin:50% 50%;animation:spin 1.4s linear infinite}
 .blink{animation:blink 1.1s steps(1) infinite}
 .pulse{transform-box:fill-box;transform-origin:50% 50%;animation:pulse 2.2s ease-out infinite}
 .dots{animation:dots 1.6s steps(1) infinite}
}
@keyframes shimmer{0%{transform:translateX(0)}100%{transform:translateX(330px)}}
@keyframes spin{to{transform:rotate(360deg)}}
@keyframes blink{0%{opacity:1}50%{opacity:0}}
@keyframes pulse{0%{transform:scale(.7);opacity:.85}100%{transform:scale(1.9);opacity:0}}
@keyframes dots{0%{opacity:.25}33%{opacity:.6}66%{opacity:1}}
""", mono=MONO, display=DISPLAY)

    defs = f"""
<linearGradient id="bg" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#020604"/><stop offset="1" stop-color="#06140C"/></linearGradient>
<linearGradient id="panel" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#0A1C12"/><stop offset="1" stop-color="#06110B"/></linearGradient>
<linearGradient id="shine" x1="0" y1="0" x2="1" y2="0"><stop offset="0" stop-color="#fff" stop-opacity="0"/><stop offset=".5" stop-color="#fff" stop-opacity=".55"/><stop offset="1" stop-color="#fff" stop-opacity="0"/></linearGradient>
<pattern id="dots" width="20" height="20" patternUnits="userSpaceOnUse"><circle cx="1" cy="1" r=".9" fill="#1D4A33" fill-opacity=".5"/></pattern>
<filter id="glowSm" x="-50%" y="-50%" width="200%" height="200%"><feGaussianBlur stdDeviation="2" result="b"/><feMerge><feMergeNode in="b"/><feMergeNode in="SourceGraphic"/></feMerge></filter>
<clipPath id="frame"><rect width="{W}" height="{H}" rx="20"/></clipPath>
"""
    b = [f'<rect width="{W}" height="{H}" fill="url(#bg)"/>', f'<rect width="{W}" height="{H}" fill="url(#dots)"/>']
    # header strip
    b.append(f'<text x="34" y="44" font-size="15" xml:space="preserve"><tspan fill="{P["primary"]}" font-weight="700">◢ SYSTEM STATUS</tspan>'
             f'<tspan fill="{P["muted"]}">  //  CURRENT DIRECTION · RESEARCH QUEUE · UPLINK</tspan></text>')
    b.append(f'<circle class="blink" cx="{W - 120}" cy="39" r="5" fill="{P["ok"]}" filter="url(#glowSm)"/>'
             f'<text x="{W - 106}" y="44" font-size="14" font-weight="700" fill="{P["ok"]}">LIVE FEED</text>')
    b.append(f'<line x1="34" y1="60" x2="{W - 34}" y2="60" stroke="{P["edge"]}"/>')

    def panel(ix: int, code: str, title: str, colour: str) -> tuple[float, float]:
        x = 24 + ix * (pw + gap)
        col = P[colour]
        b.append(f'<rect x="{x}" y="{top}" width="{pw}" height="{ph}" rx="14" fill="url(#panel)" stroke="{col}" stroke-opacity=".35"/>')
        b.append(f'<path d="M{x + 14} {top}h60" stroke="{col}" stroke-width="3" stroke-linecap="round" filter="url(#glowSm)"/>')
        b.append(f'<text x="{x + 20}" y="{top + 34}" font-size="14" xml:space="preserve">'
                 f'<tspan fill="{col}" font-weight="700">[{code}]</tspan><tspan fill="{P["text"]}" font-weight="700"> {t(title)}</tspan></text>')
        return x, top + 66

    # A — active missions
    x, y = panel(0, "A1", "ACTIVE MISSIONS", "primary")
    for i, (name, note) in enumerate(MISSIONS):
        yy = y + i * 54
        b.append(f'<text x="{x + 20}" y="{yy}" font-size="14" font-weight="700" fill="{P["text"]}">{t(name)}</text>')
        b.append(f'<text x="{x + pw - 20}" y="{yy}" font-size="11" font-weight="700" text-anchor="end" fill="{P["ok"]}">● LIVE</text>')
        b.append(f'<text x="{x + 20}" y="{yy + 18}" font-size="12" fill="{P["muted"]}">{t(note)}</text>')
        # uniform "activity" track + travelling highlight (deliberately not a progress level)
        b.append(f'<svg x="{x + 20}" y="{yy + 26}" width="{pw - 40}" height="3">'
                 f'<rect width="{pw - 40}" height="3" rx="1.5" fill="{P["primary"]}" fill-opacity=".3"/>'
                 f'<rect class="shimmer" style="--d:{2.2 + i * 0.35:.2f}s;--dl:-{i * 0.5:.1f}s" x="-30" width="60" '
                 f'height="3" fill="url(#shine)"/></svg>')

    # B — research queue
    x, y = panel(1, "B2", "RESEARCH QUEUE", "accent")
    for i, (name, note) in enumerate(RESEARCH):
        yy = y + i * 54
        b.append(f'<g transform="translate({x + 28} {yy - 5})"><circle r="8" stroke="{P["edge"]}" stroke-width="2.5" fill="none"/>'
                 f'<path class="spin" d="M0 -8 A8 8 0 0 1 8 0" stroke="{P["secondary"]}" stroke-width="2.5" fill="none" stroke-linecap="round"/></g>')
        b.append(f'<text x="{x + 46}" y="{yy}" font-size="14" font-weight="700" fill="{P["text"]}">{t(name)}</text>')
        b.append(f'<text x="{x + 46}" y="{yy + 18}" font-size="12" fill="{P["muted"]}">{t(note)}</text>')
        b.append(f'<text x="{x + pw - 20}" y="{yy}" font-size="11" font-weight="700" text-anchor="end" fill="{P["secondary"]}">'
                 f'SYNCING<tspan class="dots">...</tspan></text>')

    # C — uplink / availability
    x, y = panel(2, "C3", "UPLINK", "ok")
    ox, oy = x + 46, y + 8
    b.append(f'<circle class="pulse" cx="{ox}" cy="{oy}" r="14" stroke="{P["ok"]}" stroke-width="2" fill="none"/>')
    b.append(f'<circle cx="{ox}" cy="{oy}" r="9" fill="{P["ok"]}" filter="url(#glowSm)"/>')
    b.append(f'<text class="disp" x="{ox + 28}" y="{oy + 14}" font-size="38" fill="{P["ok"]}" textLength="112" lengthAdjust="spacing">OPEN</text>')
    b.append(f'<text x="{ox + 150}" y="{oy - 2}" font-size="12" fill="{P["muted"]}">to thoughtful</text>')
    b.append(f'<text x="{ox + 150}" y="{oy + 14}" font-size="12" fill="{P["muted"]}">collaborations</text>')
    for i, (key, val) in enumerate(UPLINK):
        yy = y + 64 + i * 38
        b.append(f'<line x1="{x + 20}" y1="{yy - 22}" x2="{x + pw - 20}" y2="{yy - 22}" stroke="{P["edge"]}" stroke-dasharray="2 4"/>')
        b.append(f'<text x="{x + 20}" y="{yy}" font-size="12" font-weight="700" fill="{P["ok"]}">{t(key)}</text>')
        b.append(f'<text x="{x + 112}" y="{yy}" font-size="13" fill="{P["text"]}" textLength="{num(len(val) * 13 * MONO_RATIO)}" '
                 f'lengthAdjust="spacing">{t(val)}</text>')

    body = f'<g clip-path="url(#frame)">{"".join(b)}</g>'
    body += (f'<rect x=".75" y=".75" width="{W - 1.5}" height="{H - 1.5}" rx="19.5" fill="none" '
             f'stroke="{P["primary"]}" stroke-opacity=".3" stroke-width="1.5"/>')
    return doc(W, H, "System status — active missions, research queue and uplink",
               "Dashboard with current focus areas, what is being learned right now, and contact details.",
               style, defs, body)


# ════════════════════════════════════════════════════════════════════════
#  SECTION HEADERS
# ════════════════════════════════════════════════════════════════════════


def section_header(idx: int, total: int, title: str, subtitle: str, theme: str, accent: str) -> str:
    P = DARK if theme == "dark" else LIGHT
    A = P[accent]
    W, H = 1000, 96
    hx, hy = 42, 46
    title_w = display_width(title, 34, 0.1)
    sub = [("// ", accent, True), (subtitle, "muted", False)]
    sub_n = sum(len(s[0]) for s in sub)
    sub_w = sub_n * 15 * MONO_RATIO
    line_x1 = 96 + title_w + 28        # rail sits beside the title (y=38), clear of the subtitle row
    line_x2 = 850
    travel = max(line_x2 - line_x1 - 10, 40)

    style = css("""
text{font-family:$mono}
.disp,.disp text{font-family:$display;font-weight:700}
@media (prefers-reduced-motion: no-preference){
 .spin{transform-box:view-box;transform-origin:${hx}px ${hy}px;animation:spin 18s linear infinite}
 .blink{animation:blink 1.1s steps(1) infinite}
 .ride{animation:ride 3.4s ease-in-out infinite alternate}
 .cur{animation:cur 1.6s ease-in-out infinite}
}
@keyframes spin{to{transform:rotate(360deg)}}
@keyframes blink{0%{opacity:1}50%{opacity:0}}
@keyframes ride{from{transform:translateX(0)}to{transform:translateX(${travel}px)}}
@keyframes cur{0%,100%{opacity:1}50%{opacity:.35}}
""", mono=MONO, display=DISPLAY, hx=hx, hy=hy, travel=num(travel))

    defs = (f'<linearGradient id="base" gradientUnits="userSpaceOnUse" x1="0" y1="0" x2="{W}" y2="0">'
            f'<stop offset="0" stop-color="{A}"/><stop offset=".45" stop-color="{P["secondary"]}" stop-opacity=".6"/>'
            f'<stop offset="1" stop-color="{P["secondary"]}" stop-opacity="0"/></linearGradient>'
            f'<linearGradient id="rail" gradientUnits="userSpaceOnUse" x1="{num(line_x1)}" y1="0" x2="{line_x2}" y2="0">'
            f'<stop offset="0" stop-color="{A}" stop-opacity=".9"/><stop offset="1" stop-color="{A}" stop-opacity=".1"/></linearGradient>'
            '<filter id="glowSm" x="-50%" y="-50%" width="200%" height="200%"><feGaussianBlur stdDeviation="1.8" result="b"/>'
            '<feMerge><feMergeNode in="b"/><feMergeNode in="SourceGraphic"/></feMerge></filter>')

    glow = ' filter="url(#glowSm)"' if theme == "dark" else ""
    b = []
    b.append(f'<polygon class="spin" points="{hexagon(hx, hy, 38)}" stroke="{A}" stroke-opacity=".55" stroke-width="1.2" '
             f'stroke-dasharray="5 7" fill="none"/>')
    b.append(f'<polygon points="{hexagon(hx, hy, 30)}" stroke="{A}" stroke-width="2" fill="{A}" fill-opacity=".1"{glow}/>')
    b.append(f'<text x="{hx}" y="{hy + 7}" text-anchor="middle" font-size="20" font-weight="700" fill="{A}">{idx:02d}</text>')
    b.append(f'<text class="disp" x="96" y="56" font-size="34" fill="{P["text"]}" textLength="{num(title_w)}" '
             f'lengthAdjust="spacing">{t(title)}</text>')
    sub_svg, _ = mono_text(sub, 98, 82, 15, P)
    b.append(sub_svg)
    b.append(f'<rect class="cur" x="{num(98 + sub_w + 5)}" y="70" width="8" height="15" fill="{A}" fill-opacity=".85"/>')
    # rail with ticks and a travelling pulse
    if line_x2 - line_x1 > 60:
        ticks = "".join(f'<line x1="{num(xx)}" y1="38" x2="{num(xx)}" y2="{34 if k % 4 == 0 else 36}"/>'
                        for k, xx in enumerate(range(int(line_x1), line_x2, 12)))
        b.append(f'<line x1="{num(line_x1)}" y1="38" x2="{line_x2}" y2="38" stroke="url(#rail)" stroke-width="1.5"/>'
                 f'<g stroke="{A}" stroke-opacity=".45">{ticks}</g>'
                 f'<circle class="ride" cx="{num(line_x1 + 5)}" cy="38" r="3.5" fill="{A}"{glow}/>')
    # right-hand meta: section counter + pips
    b.append(f'<text x="{W}" y="34" font-size="13" text-anchor="end" xml:space="preserve">'
             f'<tspan fill="{A}" font-weight="700">SEC.{idx:02d}</tspan><tspan fill="{P["muted"]}"> / {total:02d}</tspan></text>')
    pip_w, pip_gap = 9, 4
    px0 = W - total * pip_w - (total - 1) * pip_gap
    for k in range(1, total + 1):
        xx = px0 + (k - 1) * (pip_w + pip_gap)
        if k < idx:
            b.append(f'<rect x="{xx}" y="50" width="{pip_w}" height="6" rx="1" fill="{A}" fill-opacity=".55"/>')
        elif k == idx:
            b.append(f'<rect class="blink" x="{xx}" y="50" width="{pip_w}" height="6" rx="1" fill="{A}"{glow}/>')
        else:
            b.append(f'<rect x="{xx + .5}" y="50.5" width="{pip_w - 1}" height="5" rx="1" fill="none" stroke="{P["dim"]}"/>')
    b.append(f'<line x1="0" y1="93" x2="{W}" y2="93" stroke="url(#base)" stroke-width="1.6"/>')
    return doc(W, H, f"{idx:02d} // {title}", f"Section header — {subtitle}", style, defs, "".join(b))


# ════════════════════════════════════════════════════════════════════════
#  FOOTER
# ════════════════════════════════════════════════════════════════════════


def footer() -> str:
    P = DARK
    W, H, HZ = 1280, 250, 150
    rnd = random.Random(99)
    style = css("""
text{font-family:$mono}
$rm
@media (prefers-reduced-motion: no-preference){
 .hl{animation:hl 2.6s cubic-bezier(.55,.05,.9,.45) infinite}
 .blink{animation:blink 1.1s steps(1) infinite}
 .eq{transform-box:fill-box;transform-origin:50% 100%;animation:eq var(--d,1s) ease-in-out infinite alternate;animation-delay:var(--dl,0s)}
 .flow{animation:flow 3s linear infinite}
 .tw{animation:tw var(--d,3s) ease-in-out infinite;animation-delay:var(--dl,0s)}
}
@keyframes hl{0%{transform:translateY(0);opacity:0}12%{opacity:.8}100%{transform:translateY(${floor}px);opacity:.95}}
@keyframes blink{0%{opacity:1}50%{opacity:0}}
@keyframes eq{from{transform:scaleY(.15)}to{transform:scaleY(1)}}
@keyframes flow{to{stroke-dashoffset:-48}}
@keyframes tw{0%,100%{opacity:.15}50%{opacity:1}}
""", mono=MONO, rm=REDUCED_MOTION, floor=H - HZ)
    defs = f"""
<linearGradient id="bg" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#010503"/><stop offset=".6" stop-color="#04100A"/><stop offset="1" stop-color="#0A1A0C"/></linearGradient>
<radialGradient id="horizon" cx=".5" cy=".5" r=".5"><stop offset="0" stop-color="{P['hot']}" stop-opacity=".5"/><stop offset=".5" stop-color="{P['secondary2']}" stop-opacity=".2"/><stop offset="1" stop-color="{P['secondary2']}" stop-opacity="0"/></radialGradient>
<linearGradient id="hline" gradientUnits="userSpaceOnUse" x1="0" y1="0" x2="{W}" y2="0"><stop offset="0" stop-color="{P['secondary']}" stop-opacity="0"/><stop offset=".5" stop-color="{P['accent']}" stop-opacity=".9"/><stop offset="1" stop-color="{P['secondary']}" stop-opacity="0"/></linearGradient>
<linearGradient id="wave" gradientUnits="userSpaceOnUse" x1="0" y1="0" x2="{W}" y2="0"><stop offset="0" stop-color="{P['primary']}" stop-opacity="0"/><stop offset=".3" stop-color="{P['primary']}"/><stop offset=".7" stop-color="{P['secondary']}"/><stop offset="1" stop-color="{P['secondary']}" stop-opacity="0"/></linearGradient>
<linearGradient id="floorFade" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#03140B"/><stop offset="1" stop-color="#03140B" stop-opacity="0"/></linearGradient>
<linearGradient id="band" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="{P['void']}" stop-opacity="0"/><stop offset=".6" stop-color="{P['void']}" stop-opacity=".85"/><stop offset="1" stop-color="{P['void']}" stop-opacity=".92"/></linearGradient>
<pattern id="scan" width="4" height="4" patternUnits="userSpaceOnUse"><rect width="4" height="1.2" fill="#000" fill-opacity=".16"/></pattern>
<filter id="glowSm" x="-50%" y="-50%" width="200%" height="200%"><feGaussianBlur stdDeviation="2.2" result="b"/><feMerge><feMergeNode in="b"/><feMergeNode in="SourceGraphic"/></feMerge></filter>
<clipPath id="frame"><rect width="{W}" height="{H}" rx="22"/></clipPath>
<clipPath id="floorClip"><rect x="0" y="{HZ}" width="{W}" height="{H - HZ}"/></clipPath>
"""
    b = [f'<rect width="{W}" height="{H}" fill="url(#bg)"/>']
    stars = []
    for _ in range(40):
        stars.append(f'<circle class="tw" style="--d:{rnd.uniform(2, 5):.1f}s;--dl:-{rnd.uniform(0, 5):.1f}s" '
                     f'cx="{rnd.uniform(16, W - 16):.0f}" cy="{rnd.uniform(14, HZ - 20):.0f}" r="{rnd.choice([0.6, 0.9, 1.2])}"/>')
    b.append(f'<g fill="#DCFFEC">{"".join(stars)}</g>')
    b.append(f'<ellipse cx="640" cy="{HZ}" rx="760" ry="70" fill="url(#horizon)"/>')
    verticals = "".join(f'<line x1="640" y1="{HZ - 60}" x2="{640 + k * 80}" y2="{H}"/>' for k in range(-22, 23))
    b.append(f'<g clip-path="url(#floorClip)" stroke="{P["primary"]}" stroke-opacity=".4" stroke-width="1.1">{verticals}</g>')
    moving = "".join(f'<line class="hl" style="animation-delay:-{i * 0.37:.2f}s" x1="0" y1="{HZ}" x2="{W}" y2="{HZ}"/>' for i in range(7))
    static = "".join(f'<line x1="0" y1="{HZ + (H - HZ) * (i / 7) ** 2.2:.1f}" x2="{W}" y2="{HZ + (H - HZ) * (i / 7) ** 2.2:.1f}"/>'
                     for i in range(1, 8))
    b.append(f'<g stroke="url(#hline)" stroke-width="1.3"><g class="am">{moving}</g><g class="rm">{static}</g></g>')
    b.append(f'<rect x="0" y="{HZ}" width="{W}" height="24" fill="url(#floorFade)"/>')
    # signal waveform riding the horizon
    pts = []
    for i in range(0, W + 1, 8):
        env = math.sin(math.pi * i / W) ** 2
        yv = HZ - 2 - env * (10 * math.sin(i / 23) + 6 * math.sin(i / 7.3))
        pts.append(f"{i},{yv:.1f}")
    b.append(f'<polyline class="flow" points="{" ".join(pts)}" stroke="url(#wave)" stroke-width="2" fill="none" '
             f'stroke-dasharray="30 18" filter="url(#glowSm)"/>')
    b.append(f'<line x1="0" y1="{HZ}" x2="{W}" y2="{HZ}" stroke="url(#hline)" stroke-width="2"/>')
    # headline + rotator (centred monospace typewriter)
    b.append(f'<text x="640" y="40" font-size="14" text-anchor="middle" xml:space="preserve">'
             f'<tspan fill="{P["primary"]}" font-weight="700">▌END OF TRANSMISSION</tspan><tspan fill="{P["muted"]}">  //  KAWIN.OS</tspan></text>')
    fs = 26
    cw = fs * MONO_RATIO
    timeline, cursor_ev, clips = 0.4, [], []
    first_x = 640 - len(CLOSING[0]) * cw / 2
    cursor_ev.append((0.0, first_x))
    for i, line in enumerate(CLOSING):
        lx = 640 - len(line) * cw / 2
        ev = [(0.0, 0.0)]
        cursor_ev.append((timeline, lx))
        for k in range(1, len(line) + 1):
            timeline += rnd.uniform(0.04, 0.085)
            ev.append((timeline, k * cw))
            cursor_ev.append((timeline, lx + k * cw))
        timeline += 2.4
        for k in range(len(line) - 1, -1, -1):
            timeline += 0.02
            ev.append((timeline, k * cw))
            cursor_ev.append((timeline, lx + k * cw))
        timeline += 0.4
        clips.append((lx, line, ev))
    total = timeline
    anim = []
    for i, (lx, line, ev) in enumerate(clips):
        defs += (f'<clipPath id="cl{i}"><rect x="{num(lx)}" y="{94 - fs}" width="0" height="{fs + 10}">'
                 f'{discrete("width", ev, total)}</rect></clipPath>')
        anim.append(f'<text clip-path="url(#cl{i})" x="{num(lx)}" y="94" font-size="{fs}" font-weight="700" '
                    f'textLength="{num(len(line) * cw)}" lengthAdjust="spacing" fill="#FFFFFF">{t(line)}</text>')
    caret = (f'<rect class="blink" x="{num(first_x)}" y="{94 - fs + 4}" width="{num(cw * 0.55)}" height="{fs}" '
             f'fill="{P["glow"]}">{discrete("x", cursor_ev, total)}</rect>')
    b.append(f'<g class="am" filter="url(#glowSm)">{"".join(anim)}{caret}</g>')
    last = CLOSING[-1]
    b.append(f'<g class="rm"><text x="640" y="94" font-size="{fs}" font-weight="700" text-anchor="middle" '
             f'fill="#FFFFFF">{t(last)}</text></g>')

    b.append(f'<rect x="0" y="196" width="{W}" height="{H - 196}" fill="url(#band)"/>')
    b.append(f'<text x="40" y="232" font-size="13" xml:space="preserve"><tspan fill="{P["primary"]}" font-weight="700">SESSION ▸ </tspan>'
             f'<tspan fill="{P["muted"]}">CLOSED GRACEFULLY  ·  STATE SAVED  ·  SEE YOU IN THE NEXT COMMIT</tspan></text>')
    b.append(f'<text x="1222" y="232" font-size="13" text-anchor="end" xml:space="preserve"><tspan fill="{P["muted"]}">SIGNAL  </tspan>'
             f'<tspan fill="{P["ok"]}" font-weight="700">ONLINE</tspan></text>')
    b.append(f'<circle class="blink" cx="1236" cy="227" r="4.5" fill="{P["ok"]}" filter="url(#glowSm)"/>')
    b.append(f'<rect width="{W}" height="{H}" fill="url(#scan)"/>')
    body = f'<g clip-path="url(#frame)">{"".join(b)}</g>'
    body += (f'<rect x=".75" y=".75" width="{W - 1.5}" height="{H - 1.5}" rx="21.5" fill="none" '
             f'stroke="{P["primary"]}" stroke-opacity=".35" stroke-width="1.5"/>')
    return doc(W, H, "End of transmission — thanks for stopping by",
               "Animated footer with a synthwave grid, signal waveform and closing messages.", style, defs, body)


# ════════════════════════════════════════════════════════════════════════
#  BUILD
# ════════════════════════════════════════════════════════════════════════


def write(rel: str, svg: str) -> None:
    path = OUT / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(svg, encoding="utf-8")
    print(f"  wrote {path.relative_to(ROOT)}  ({len(svg.encode()) / 1024:.1f} KB)")


def main() -> None:
    print("KAWIN.OS asset build")
    write("hero.svg", hero())
    write("terminal.svg", terminal())
    write("status.svg", status_board())
    write("footer.svg", footer())
    accents = ["primary", "accent", "secondary"]
    for i, (slug, title, subtitle) in enumerate(SECTIONS, start=1):
        for theme in ("dark", "light"):
            write(f"sections/{i:02d}-{slug}-{theme}.svg",
                  section_header(i, len(SECTIONS), title, subtitle, theme, accents[(i - 1) % 3]))


if __name__ == "__main__":
    main()
