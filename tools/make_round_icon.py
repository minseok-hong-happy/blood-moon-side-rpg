from __future__ import annotations

import argparse
from pathlib import Path

from PIL import Image, ImageDraw


def main() -> None:
    parser = argparse.ArgumentParser(description="Create an antialiased circular launcher icon.")
    parser.add_argument("input", type=Path)
    parser.add_argument("output", type=Path)
    parser.add_argument("--size", type=int, default=512)
    args = parser.parse_args()

    image = Image.open(args.input).convert("RGBA")
    size = min(image.size)
    left = (image.width - size) // 2
    top = (image.height - size) // 2
    image = image.crop((left, top, left + size, top + size))
    target_size = max(192, min(1024, args.size))
    if image.size != (target_size, target_size):
        image = image.resize((target_size, target_size), Image.Resampling.LANCZOS)
    size = target_size

    mask = Image.new("L", (size, size), 0)
    inset = max(2, size // 128)
    ImageDraw.Draw(mask).ellipse((inset, inset, size - inset - 1, size - inset - 1), fill=255)
    image.putalpha(mask)

    args.output.parent.mkdir(parents=True, exist_ok=True)
    image.save(args.output, "PNG", optimize=True)


if __name__ == "__main__":
    main()
