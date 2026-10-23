#!/usr/bin/env python3
"""Render Android VectorDrawable XML as SVG (for previews and screenshots).

Supports viewport, nested <group> transforms, <path> fill/stroke attributes and aapt:attr
linear/radial gradients - everything KRS Tunnels' drawables use.

    python3 vector2svg.py <vector.xml> [--tint #RRGGBB] [--size 96]
"""
import argparse
import xml.etree.ElementTree as ET

A = "{http://schemas.android.com/apk/res/android}"
_grad_id = [0]


def color(v, tint=None):
    """Returns (#rrggbb, alpha). A tint replaces the RGB of every visible color, like
    Android's default SRC_IN tint on a vector drawable."""
    if v is None:
        return None, 1.0
    if v.startswith("?") or v.startswith("@"):
        v = "#FFFFFF"
    v = v.lstrip("#")
    if len(v) == 8:
        a, rgb = int(v[:2], 16) / 255.0, v[2:]
    elif len(v) == 6:
        a, rgb = 1.0, v
    elif len(v) == 4:
        a, rgb = int(v[0] * 2, 16) / 255.0, "".join(c * 2 for c in v[1:])
    else:
        a, rgb = 1.0, "".join(c * 2 for c in v)
    if tint and a > 0:
        rgb = tint.lstrip("#")[-6:]
    return "#" + rgb, a


def gradient(el, defs):
    _grad_id[0] += 1
    gid = f"g{_grad_id[0]}"
    kind = el.get(A + "type", "linear")
    stops = "".join(
        f'<stop offset="{it.get(A + "offset")}" stop-color="{color(it.get(A + "color"))[0]}" '
        f'stop-opacity="{color(it.get(A + "color"))[1]:.3f}"/>' for it in el.findall("item"))
    if kind == "radial":
        defs.append(f'<radialGradient id="{gid}" gradientUnits="userSpaceOnUse" cx="{el.get(A + "centerX")}" '
                    f'cy="{el.get(A + "centerY")}" r="{el.get(A + "gradientRadius")}">{stops}</radialGradient>')
    else:
        defs.append(f'<linearGradient id="{gid}" gradientUnits="userSpaceOnUse" x1="{el.get(A + "startX")}" '
                    f'y1="{el.get(A + "startY")}" x2="{el.get(A + "endX")}" y2="{el.get(A + "endY")}">{stops}</linearGradient>')
    return f"url(#{gid})"


def render(el, defs, tint):
    out = []
    for child in el:
        tag = child.tag
        if tag == "group":
            tx, ty = float(child.get(A + "translateX", 0)), float(child.get(A + "translateY", 0))
            sx, sy = float(child.get(A + "scaleX", 1)), float(child.get(A + "scaleY", 1))
            rot = float(child.get(A + "rotation", 0))
            px, py = float(child.get(A + "pivotX", 0)), float(child.get(A + "pivotY", 0))
            t = (f"translate({tx + px} {ty + py}) rotate({rot}) scale({sx} {sy}) translate({-px} {-py})")
            out.append(f'<g transform="{t}">{render(child, defs, tint)}</g>')
        elif tag == "path":
            fill, fill_a = color(child.get(A + "fillColor"), tint)
            stroke, stroke_a = color(child.get(A + "strokeColor"), tint)
            for attr in child.findall("{http://schemas.android.com/aapt}attr"):
                g = attr.find("gradient")
                if attr.get("name") == "android:fillColor":
                    fill, fill_a = gradient(g, defs), 1.0
                elif attr.get("name") == "android:strokeColor":
                    stroke, stroke_a = gradient(g, defs), 1.0
            parts = [f'd="{child.get(A + "pathData")}"',
                     f'fill="{fill or "none"}"', f'fill-opacity="{fill_a:.3f}"']
            if stroke:
                parts += [f'stroke="{stroke}"', f'stroke-opacity="{stroke_a:.3f}"',
                          f'stroke-width="{child.get(A + "strokeWidth", "1")}"',
                          f'stroke-linecap="{child.get(A + "strokeLineCap", "butt")}"',
                          f'stroke-linejoin="{child.get(A + "strokeLineJoin", "miter")}"']
            if child.get(A + "fillType") == "evenOdd":
                parts.append('fill-rule="evenodd"')
            out.append("<path " + " ".join(parts) + "/>")
    return "".join(out)


def to_svg(path, tint=None, size=None):
    root = ET.parse(path).getroot()
    vw, vh = root.get(A + "viewportWidth"), root.get(A + "viewportHeight")
    tint = tint or (color(root.get(A + "tint"))[0] if root.get(A + "tint", "").startswith("#") else None)
    defs = []
    body = render(root, defs, tint)
    w = f' width="{size}" height="{size}"' if size else ""
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {vw} {vh}"{w}>'
            f'<defs>{"".join(defs)}</defs>{body}</svg>')


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("vector")
    ap.add_argument("--tint")
    ap.add_argument("--size", type=int)
    a = ap.parse_args()
    print(to_svg(a.vector, a.tint, a.size))
