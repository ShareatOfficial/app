#!/usr/bin/env python3
"""Compose store screenshots from raw device captures.

Usage:
    python3 release/tools/compose_screenshots.py play   --raw <dir>
    python3 release/tools/compose_screenshots.py appstore --raw <dir>

The raw directory holds the untouched captures named raw_c1..raw_c4 (customer
flow) and raw_r1..raw_r4 (restaurant flow). Each output pairs a Spanish caption
with the capture in a device frame, on the Shareat brand background.
"""

import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = Path(__file__).resolve().parents[2]
FONTS = ROOT / "shared/designsystem/src/commonMain/composeResources/font"

ORANGE = (255, 79, 0)
TEAL = (0, 104, 116)
INK = (35, 25, 23)
MUTED = (83, 67, 63)
CREAM_TOP = (255, 248, 246)
CREAM_BOTTOM = (255, 226, 212)

# Both apps draw their own top inset, so the system status bar strip is empty
# app background and is cropped away rather than shown half-covered.
TARGETS = {
    "play": {
        "out": ROOT / "release/playstore/screenshots/es",
        "size": (1080, 1920),
        "status_bar": 142,
        "device_width": 620,
    },
    "appstore": {
        "out": ROOT / "release/appstore/screenshots/es",
        "size": (1320, 2868),
        "status_bar": 0,
        "device_width": 900,
    },
}

SHOTS = [
    ("01-descubre", "raw_c1", "Clientes", "Descubre dónde\ncomer hoy",
     "Restaurantes publicados en Shareat,\ncon su nota y si están abiertos ahora."),
    ("02-carta", "raw_c2", "Clientes", "La carta entera,\nantes de sentarte",
     "Cada plato con su precio, su descripción\ny los alérgenos que declara el local."),
    ("03-alergenos", "raw_c3", "Clientes", "Filtra por\nalérgenos",
     "Solo aparecen los alérgenos que declara\nesa carta. Nada se da por supuesto."),
    ("04-valora-platos", "raw_c4", "Clientes", "Valora plato\na plato",
     "No solo el restaurante: cada plato tiene\nsu nota y sus reseñas."),
    ("05-alta-restaurante", "raw_r1", "Restaurantes", "Da de alta tu\nrestaurante",
     "Nombre, contacto y dirección en un paso.\nEmpieza como borrador, sin prisa."),
    ("06-anadir-plato", "raw_r2", "Restaurantes", "Añade cada plato\ncon sus alérgenos",
     "Precio, descripción y los 14 alérgenos\nde la normativa europea."),
    ("07-gestiona-carta", "raw_r3", "Restaurantes", "Tu carta, siempre\nal día",
     "Añade platos, ordena categorías y cambia\nprecios desde el móvil."),
    ("08-vista-cliente", "raw_r4", "Restaurantes", "Compruébalo como\nlo ve tu cliente",
     "La vista de cliente te enseña tu carta\ntal y como llega a la gente."),
]


def fonts(scale: float):
    return (
        ImageFont.truetype(str(FONTS / "Fraunces_72pt-Bold.ttf"), round(66 * scale)),
        ImageFont.truetype(str(FONTS / "Inter_18pt-Regular.ttf"), round(35 * scale)),
        ImageFont.truetype(str(FONTS / "Inter_18pt-SemiBold.ttf"), round(28 * scale)),
    )


def background(size: tuple[int, int]) -> Image.Image:
    w, h = size
    bg = Image.new("RGB", size)
    draw = ImageDraw.Draw(bg)
    for y in range(h):
        t = y / (h - 1)
        draw.line(
            [(0, y), (w, y)],
            fill=tuple(round(a + (b - a) * t) for a, b in zip(CREAM_TOP, CREAM_BOTTOM)),
        )

    glow = Image.new("RGB", size, (0, 0, 0))
    ImageDraw.Draw(glow).ellipse(
        [-w // 4, round(h * 0.25), w + w // 4, h + round(h * 0.36)], fill=(70, 22, 0)
    )
    glow = glow.filter(ImageFilter.GaussianBlur(round(w * 0.11)))
    tint = Image.composite(Image.new("RGB", size, ORANGE), bg, glow.convert("L"))
    return Image.blend(bg, tint, 0.12)


def rounded_mask(size: tuple[int, int], radius: int) -> Image.Image:
    mask = Image.new("L", size, 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, size[0] - 1, size[1] - 1], radius, fill=255)
    return mask


def device(capture: Image.Image, width: int, scale: float):
    """Screenshot in a white bezel, plus the drop shadow to paste beneath it."""
    bezel = round(12 * scale)
    inner_w = width - bezel * 2
    inner_h = round(capture.height * inner_w / capture.width)
    screen = capture.resize((inner_w, inner_h), Image.LANCZOS)
    screen.putalpha(rounded_mask(screen.size, round(40 * scale)))

    frame = Image.new("RGBA", (width, inner_h + bezel * 2), (0, 0, 0, 0))
    frame.paste(
        Image.new("RGBA", frame.size, (255, 255, 255, 255)),
        (0, 0),
        rounded_mask(frame.size, round(52 * scale)),
    )
    frame.paste(screen, (bezel, bezel), screen)

    pad = round(80 * scale)
    shadow = Image.new("RGBA", (frame.width + pad * 2, frame.height + pad * 2), (0, 0, 0, 0))
    shadow.paste(
        Image.new("RGBA", frame.size, (60, 20, 5, 105)),
        (pad, pad + round(12 * scale)),
        rounded_mask(frame.size, round(52 * scale)),
    )
    return frame, shadow.filter(ImageFilter.GaussianBlur(round(38 * scale))), pad


def compose(target: dict, raw_dir: Path, name, raw, audience, headline, sub) -> None:
    w, h = target["size"]
    scale = w / 1080
    display, body, chip = fonts(scale)

    canvas = background(target["size"])
    draw = ImageDraw.Draw(canvas)
    margin = round(72 * scale)

    color = ORANGE if audience == "Clientes" else TEAL
    chip_w = draw.textlength(audience, font=chip) + round(56 * scale)
    chip_h = round(58 * scale)
    chip_y = round(84 * scale)
    draw.rounded_rectangle(
        [margin, chip_y, margin + chip_w, chip_y + chip_h], chip_h // 2, fill=color
    )
    draw.text(
        (margin + round(28 * scale), chip_y + chip_h // 2),
        audience, font=chip, fill=(255, 255, 255), anchor="lm",
    )

    head_y = round(188 * scale)
    spacing = round(14 * scale)
    draw.multiline_text((margin, head_y), headline, font=display, fill=INK, spacing=spacing)
    head_bottom = draw.multiline_textbbox((margin, head_y), headline, font=display, spacing=spacing)[3]

    sub_y = head_bottom + round(28 * scale)
    draw.multiline_text((margin, sub_y), sub, font=body, fill=MUTED, spacing=round(12 * scale))
    sub_bottom = draw.multiline_textbbox((margin, sub_y), sub, font=body, spacing=round(12 * scale))[3]

    capture = Image.open(raw_dir / f"{raw}.png").convert("RGB")
    if target["status_bar"]:
        capture = capture.crop((0, target["status_bar"], capture.width, capture.height))

    frame, shadow, pad = device(capture, target["device_width"], scale)
    top = sub_bottom + round(66 * scale)
    if top + frame.height > h:
        top = max(sub_bottom + round(40 * scale), h - frame.height)

    x = (w - frame.width) // 2
    canvas.paste(shadow, (x - pad, top - pad), shadow)
    canvas.paste(frame, (x, top), frame)

    out = target["out"] / f"{name}.png"
    out.parent.mkdir(parents=True, exist_ok=True)
    canvas.save(out)
    print("wrote", out.relative_to(ROOT), canvas.size)


def main() -> None:
    if len(sys.argv) < 2 or sys.argv[1] not in TARGETS:
        sys.exit(f"usage: {sys.argv[0]} {{{'|'.join(TARGETS)}}} --raw <dir>")
    target = TARGETS[sys.argv[1]]
    raw_dir = Path(sys.argv[sys.argv.index("--raw") + 1]) if "--raw" in sys.argv else Path.cwd()
    for shot in SHOTS:
        compose(target, raw_dir, *shot)


if __name__ == "__main__":
    main()
