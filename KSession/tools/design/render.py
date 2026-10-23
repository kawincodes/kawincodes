#!/usr/bin/env python3
"""Render the design previews to PNG with headless Chromium.

    python3 tools/design/render.py <chromium> <scratch dir> <roboto dir> <roboto-mono dir>
Writes docs/design/krs-tunnels-screens.png and docs/design/screen-<name>.png.
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "..", "brand"))
from export_brand import trim_png  # noqa: E402

OUT = os.path.join(HERE, "..", "..", "docs", "design")


def shoot(chrome, page, png, w, h):
    subprocess.run([chrome, "--headless=new", "--no-sandbox", "--disable-gpu", "--hide-scrollbars",
                    "--allow-file-access-from-files", f"--window-size={w},{h + 87}", f"--screenshot={png}",
                    "file://" + page], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    trim_png(png, w, h)


def main(chrome, scratch, roboto, mono):
    os.makedirs(OUT, exist_ok=True)
    subprocess.run([sys.executable, os.path.join(HERE, "screens.py"), scratch, roboto, mono], check=True)
    shoot(chrome, os.path.join(scratch, "all.html"), os.path.join(OUT, "krs-tunnels-screens.png"), 1866, 964)
    for name in ("chat", "radar", "tunnels", "cloud"):
        shoot(chrome, os.path.join(scratch, f"{name}.html"), os.path.join(OUT, f"screen-{name}.png"), 484, 964)


if __name__ == "__main__":
    main(*sys.argv[1:5])
