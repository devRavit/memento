#!/usr/bin/env python3
from PIL import Image, ImageDraw, ImageFont
from pathlib import Path

templates_dir = Path("src/main/resources/templates/goods")
templates_dir.mkdir(parents=True, exist_ok=True)

def create_sticker_template():
    img = Image.new('RGBA', (1200, 1200), (255, 255, 255, 255))
    draw = ImageDraw.Draw(img)

    sticker_positions = [
        (200, 200, 450, 450),
        (500, 180, 750, 430),
        (220, 520, 470, 770),
        (520, 550, 770, 800),
    ]

    for x1, y1, x2, y2 in sticker_positions:
        draw.rectangle([x1-5, y1-5, x2+5, y2+5], fill=(255, 255, 255, 255), outline=(200, 200, 200, 255), width=2)
        for i in range(x1, x2):
            for j in range(y1, y2):
                img.putpixel((i, j), (0, 0, 0, 0))

    return img

def create_photobook_template():
    img = Image.new('RGBA', (1200, 1600), (245, 240, 235, 255))
    draw = ImageDraw.Draw(img)

    draw.rectangle([50, 100, 550, 1500], fill=(100, 60, 30, 255))

    draw.rectangle([600, 120, 1150, 900], fill=(255, 255, 255, 255))

    for i in range(620, 1130):
        for j in range(140, 880):
            img.putpixel((i, j), (0, 0, 0, 0))

    return img

def create_calendar_template():
    img = Image.new('RGBA', (1200, 1600), (250, 250, 250, 255))
    draw = ImageDraw.Draw(img)

    draw.rectangle([100, 100, 1100, 800], fill=(255, 255, 255, 255))
    for i in range(120, 1080):
        for j in range(120, 780):
            img.putpixel((i, j), (0, 0, 0, 0))

    draw.rectangle([100, 850, 1100, 1500], fill=(255, 255, 255, 255))
    for col in range(7):
        for row in range(5):
            x1 = 100 + col * 142
            y1 = 850 + row * 130
            x2 = x1 + 142
            y2 = y1 + 130
            draw.rectangle([x1, y1, x2, y2], outline=(200, 200, 200, 255), width=1)

    return img

def create_wall_calendar_template():
    img = Image.new('RGBA', (1200, 1600), (240, 240, 240, 255))
    draw = ImageDraw.Draw(img)

    draw.rectangle([100, 100, 1100, 800], fill=(255, 255, 255, 255))
    for i in range(120, 1080):
        for j in range(120, 780):
            img.putpixel((i, j), (0, 0, 0, 0))

    draw.rectangle([100, 850, 1100, 1500], fill=(255, 255, 255, 255))
    for col in range(7):
        for row in range(5):
            x1 = 100 + col * 142
            y1 = 850 + row * 130
            x2 = x1 + 142
            y2 = y1 + 130
            draw.rectangle([x1, y1, x2, y2], outline=(200, 200, 200, 255), width=1)

    draw.ellipse([570, 70, 590, 90], fill=(180, 180, 180, 255))
    draw.ellipse([610, 70, 630, 90], fill=(180, 180, 180, 255))

    return img

def create_magnet_template():
    img = Image.new('RGBA', (1400, 1000), (240, 240, 240, 255))
    draw = ImageDraw.Draw(img)

    magnets = [
        {'type': 'circle', 'center': (250, 200), 'radius': 120},
        {'type': 'square', 'box': [500, 80, 740, 320]},
        {'type': 'hexagon', 'center': (950, 200), 'radius': 120},
        {'type': 'circle', 'center': (250, 550), 'radius': 120},
        {'type': 'heart', 'center': (620, 550), 'size': 140},
        {'type': 'square', 'box': [850, 430, 1090, 670]},
    ]

    for magnet in magnets:
        if magnet['type'] == 'circle':
            cx, cy = magnet['center']
            r = magnet['radius']
            draw.ellipse([cx-r, cy-r, cx+r, cy+r], fill=(255, 255, 255, 255), outline=(180, 180, 180, 255), width=3)

            mask = Image.new('L', (r*2, r*2), 0)
            mask_draw = ImageDraw.Draw(mask)
            mask_draw.ellipse([0, 0, r*2, r*2], fill=255)

            for x in range(r*2):
                for y in range(r*2):
                    if mask.getpixel((x, y)) > 128:
                        px, py = cx - r + x, cy - r + y
                        if 0 <= px < img.width and 0 <= py < img.height:
                            img.putpixel((px, py), (0, 0, 0, 0))

        elif magnet['type'] == 'square':
            x1, y1, x2, y2 = magnet['box']
            draw.rectangle([x1, y1, x2, y2], fill=(255, 255, 255, 255), outline=(180, 180, 180, 255), width=3)
            for i in range(x1+10, x2-10):
                for j in range(y1+10, y2-10):
                    img.putpixel((i, j), (0, 0, 0, 0))

        elif magnet['type'] == 'hexagon':
            cx, cy = magnet['center']
            r = magnet['radius']
            points = []
            for i in range(6):
                angle = 3.14159 / 3 * i
                x = cx + r * (0.866 if i % 2 == 0 else 0.5) * (1 if i < 3 else -1)
                y = cy + r * (0.5 if i % 2 == 0 else 0.866) * (1 if 1 <= i <= 4 else -1)
                points.append((x, y))

            points = [
                (cx, cy - r),
                (cx + r * 0.866, cy - r * 0.5),
                (cx + r * 0.866, cy + r * 0.5),
                (cx, cy + r),
                (cx - r * 0.866, cy + r * 0.5),
                (cx - r * 0.866, cy - r * 0.5),
            ]

            draw.polygon(points, fill=(255, 255, 255, 255), outline=(180, 180, 180, 255), width=3)

            mask = Image.new('L', (int(r*2.2), int(r*2.2)), 0)
            mask_draw = ImageDraw.Draw(mask)
            offset_points = [(x - cx + r*1.1, y - cy + r*1.1) for x, y in points]
            mask_draw.polygon(offset_points, fill=255)

            for x in range(int(r*2.2)):
                for y in range(int(r*2.2)):
                    if mask.getpixel((x, y)) > 128:
                        px, py = int(cx - r*1.1 + x), int(cy - r*1.1 + y)
                        if 0 <= px < img.width and 0 <= py < img.height:
                            img.putpixel((px, py), (0, 0, 0, 0))

        elif magnet['type'] == 'heart':
            cx, cy = magnet['center']
            s = magnet['size']

            heart_mask = Image.new('L', (s, s), 0)
            heart_draw = ImageDraw.Draw(heart_mask)

            heart_draw.ellipse([s*0.1, s*0.1, s*0.45, s*0.5], fill=255)
            heart_draw.ellipse([s*0.55, s*0.1, s*0.9, s*0.5], fill=255)

            points = [
                (s*0.5, s*0.9),
                (s*0.1, s*0.4),
                (s*0.9, s*0.4),
            ]
            heart_draw.polygon(points, fill=255)

            for x in range(s):
                for y in range(s):
                    if heart_mask.getpixel((x, y)) > 128:
                        px, py = cx - s//2 + x, cy - s//2 + y
                        if 0 <= px < img.width and 0 <= py < img.height:
                            current = img.getpixel((px, py))
                            if current[3] > 0:
                                img.putpixel((px, py), (255, 255, 255, 255))

            for x in range(s):
                for y in range(s):
                    if heart_mask.getpixel((x, y)) > 128:
                        px, py = cx - s//2 + x, cy - s//2 + y
                        if 10 < x < s-10 and 10 < y < s-10:
                            if 0 <= px < img.width and 0 <= py < img.height:
                                img.putpixel((px, py), (0, 0, 0, 0))

    return img

def create_frame_template():
    img = Image.new('RGBA', (1200, 1600), (235, 235, 235, 255))
    draw = ImageDraw.Draw(img)

    draw.rectangle([100, 200, 1100, 1400], fill=(40, 30, 20, 255))

    draw.rectangle([150, 250, 1050, 1350], fill=(255, 255, 255, 255))

    for i in range(180, 1020):
        for j in range(280, 1320):
            img.putpixel((i, j), (0, 0, 0, 0))

    return img

def create_poster_template():
    img = Image.new('RGBA', (1200, 1700), (240, 240, 240, 255))
    draw = ImageDraw.Draw(img)

    draw.rectangle([100, 100, 1100, 1600], fill=(255, 255, 255, 255))

    for i in range(120, 1080):
        for j in range(120, 1580):
            img.putpixel((i, j), (0, 0, 0, 0))

    return img

def create_postcard_template():
    img = Image.new('RGBA', (1400, 1000), (240, 240, 240, 255))
    draw = ImageDraw.Draw(img)

    draw.rectangle([200, 150, 1200, 850], fill=(255, 255, 255, 255))

    for i in range(230, 1170):
        for j in range(180, 820):
            img.putpixel((i, j), (0, 0, 0, 0))

    return img

templates = {
    'sticker': create_sticker_template,
    'photobook': create_photobook_template,
    'calendar': create_calendar_template,
    'wall-calendar': create_wall_calendar_template,
    'magnet': create_magnet_template,
    'frame': create_frame_template,
    'poster': create_poster_template,
    'postcard': create_postcard_template,
}

def main():
    print("굿즈 템플릿 이미지 생성 시작...")
    print(f"저장 경로: {templates_dir.absolute()}")

    for goods_type, create_func in templates.items():
        try:
            img = create_func()
            output_path = templates_dir / f"{goods_type}_template.png"
            img.save(output_path, 'PNG')
            print(f"✓ {goods_type} 템플릿 생성 완료: {output_path}")
        except Exception as e:
            print(f"✗ {goods_type} 생성 실패: {e}")

    print(f"\n완료: {len(templates)} 템플릿 생성 성공")

if __name__ == "__main__":
    main()
