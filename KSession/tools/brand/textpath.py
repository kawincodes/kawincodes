"""Turn a string into SVG/VectorDrawable path data using a font's outlines (fontTools)."""
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen
from fontTools.ttLib import TTFont


class TextPath:
    def __init__(self, font_file):
        self.font = TTFont(font_file)
        self.glyphs = self.font.getGlyphSet()
        self.cmap = self.font.getBestCmap()
        self.cap = self.font["OS/2"].sCapHeight or 700
        self.hmtx = self.font["hmtx"]

    def width(self, text, cap_height, tracking=0.0):
        s = cap_height / self.cap
        w = sum(self.hmtx[self.cmap[ord(c)]][0] for c in text) * s
        return w + tracking * (len(text) - 1)

    def path(self, text, x, baseline, cap_height, tracking=0.0, anchor="start"):
        """Path data for `text` with its baseline at `baseline` (y grows downwards)."""
        s = cap_height / self.cap
        if anchor == "middle":
            x -= self.width(text, cap_height, tracking) / 2
        elif anchor == "end":
            x -= self.width(text, cap_height, tracking)
        parts = []
        for c in text:
            name = self.cmap[ord(c)]
            pen = SVGPathPen(self.glyphs)
            self.glyphs[name].draw(TransformPen(pen, (s, 0, 0, -s, x, baseline)))
            d = pen.getCommands()
            if d:
                parts.append(d)
            x += self.hmtx[name][0] * s + tracking
        return " ".join(parts)
