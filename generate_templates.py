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
    img = Image.new('RGBA', (1400, 1000), (255, 255, 255, 255))
    draw = ImageDraw.Draw(img)

    for y in range(img.height):
        for x in range(img.width):
            base_r = 220 + int(20 * (y / img.height))
            base_g = 225 + int(20 * (y / img.height))
            base_b = 230 + int(20 * (y / img.height))
            noise = hash((x * 7 + y * 13) % 997) % 15 - 7
            r = max(0, min(255, base_r + noise))
            g = max(0, min(255, base_g + noise))
            b = max(0, min(255, base_b + noise))
            img.putpixel((x, y), (r, g, b, 255))

    magnets = [
        {'type': 'circle', 'center': (250, 200), 'radius': 120},
        {'type': 'square', 'box': [500, 80, 740, 320]},
        {'type': 'hexagon', 'center': (950, 200), 'radius': 120},
        {'type': 'circle', 'center': (250, 550), 'radius': 120},
        {'type': 'heart', 'center': (620, 550), 'size': 140},
        {'type': 'square', 'box': [850, 430, 1090, 670]},
    ]

    def draw_shadow(cx, cy, r, shape_type='circle', box=None):
        shadow_offset_x, shadow_offset_y = 8, 8
        shadow_blur = 12

        if shape_type == 'circle':
            for dy in range(-r - shadow_blur, r + shadow_blur):
                for dx in range(-r - shadow_blur, r + shadow_blur):
                    px, py = cx + dx + shadow_offset_x, cy + dy + shadow_offset_y
                    if 0 <= px < img.width and 0 <= py < img.height:
                        dist = (dx**2 + dy**2) ** 0.5
                        if dist < r + shadow_blur:
                            alpha = max(0, min(50, int(50 * (1 - (dist - r) / shadow_blur))))
                            if alpha > 0:
                                current = img.getpixel((px, py))
                                new_r = max(0, current[0] - alpha)
                                new_g = max(0, current[1] - alpha)
                                new_b = max(0, current[2] - alpha)
                                img.putpixel((px, py), (new_r, new_g, new_b, 255))

        elif shape_type == 'square' and box:
            x1, y1, x2, y2 = box
            for dy in range(-shadow_blur, y2 - y1 + shadow_blur):
                for dx in range(-shadow_blur, x2 - x1 + shadow_blur):
                    px, py = x1 + dx + shadow_offset_x, y1 + dy + shadow_offset_y
                    if 0 <= px < img.width and 0 <= py < img.height:
                        dist_x = max(0, max(x1 - px, px - x2))
                        dist_y = max(0, max(y1 - py, py - y2))
                        dist = (dist_x**2 + dist_y**2) ** 0.5
                        alpha = max(0, min(50, int(50 * (1 - dist / shadow_blur))))
                        if alpha > 0:
                            current = img.getpixel((px, py))
                            new_r = max(0, current[0] - alpha)
                            new_g = max(0, current[1] - alpha)
                            new_b = max(0, current[2] - alpha)
                            img.putpixel((px, py), (new_r, new_g, new_b, 255))

    for magnet in magnets:
        if magnet['type'] == 'circle':
            cx, cy = magnet['center']
            r = magnet['radius']

            draw_shadow(cx, cy, r, 'circle')

            draw.ellipse([cx-r-3, cy-r-3, cx+r+3, cy+r+3], fill=(200, 200, 200, 255))
            draw.ellipse([cx-r, cy-r, cx+r, cy+r], fill=(250, 250, 250, 255))

            for angle_deg in range(0, 360, 10):
                angle = angle_deg * 3.14159 / 180
                px = int(cx + (r - 5) * (1 + 0.02 * ((cx + cy + angle_deg) % 7)) * (0.5 if angle_deg % 20 == 0 else 1) * (0 if angle_deg % 40 == 0 else 1) + r * (1 - abs((angle_deg % 180) - 90) / 90) * 0.02)
                py = int(cy + (r - 5) * (1 + 0.02 * ((cx + cy + angle_deg) % 7)) * (0.5 if angle_deg % 20 == 0 else 1) * (0 if angle_deg % 40 == 0 else 1) + r * (1 - abs((angle_deg % 180) - 90) / 90) * 0.02)

            border_width = 8
            for bw in range(border_width):
                alpha = int(255 * (1 - bw / border_width))
                draw.ellipse([cx-r+bw, cy-r+bw, cx+r-bw, cy+r-bw], outline=(240, 240, 240, alpha), width=1)

            mask = Image.new('L', (r*2, r*2), 0)
            mask_draw = ImageDraw.Draw(mask)
            mask_draw.ellipse([0, 0, r*2, r*2], fill=255)

            for x in range(r*2):
                for y in range(r*2):
                    if mask.getpixel((x, y)) > 128:
                        px, py = cx - r + x, cy - r + y
                        if 0 <= px < img.width and 0 <= py < img.height:
                            dist_from_center = ((x - r)**2 + (y - r)**2) ** 0.5
                            if dist_from_center < r - border_width:
                                img.putpixel((px, py), (0, 0, 0, 0))

        elif magnet['type'] == 'square':
            x1, y1, x2, y2 = magnet['box']

            draw_shadow((x1+x2)//2, (y1+y2)//2, 0, 'square', (x1, y1, x2, y2))

            draw.rectangle([x1-3, y1-3, x2+3, y2+3], fill=(200, 200, 200, 255))
            draw.rectangle([x1, y1, x2, y2], fill=(250, 250, 250, 255))

            border_width = 8
            for i in range(x1 + border_width, x2 - border_width):
                for j in range(y1 + border_width, y2 - border_width):
                    img.putpixel((i, j), (0, 0, 0, 0))

        elif magnet['type'] == 'hexagon':
            cx, cy = magnet['center']
            r = magnet['radius']

            points = [
                (cx, cy - r),
                (cx + r * 0.866, cy - r * 0.5),
                (cx + r * 0.866, cy + r * 0.5),
                (cx, cy + r),
                (cx - r * 0.866, cy + r * 0.5),
                (cx - r * 0.866, cy - r * 0.5),
            ]

            draw_shadow(cx, cy, r, 'circle')

            shadow_points = [(x + 3, y + 3) for x, y in points]
            draw.polygon(shadow_points, fill=(200, 200, 200, 255))
            draw.polygon(points, fill=(250, 250, 250, 255))

            mask = Image.new('L', (int(r*2.2), int(r*2.2)), 0)
            mask_draw = ImageDraw.Draw(mask)
            offset_points = [(x - cx + r*1.1, y - cy + r*1.1) for x, y in points]
            mask_draw.polygon(offset_points, fill=255)

            border_width = 8
            for x in range(int(r*2.2)):
                for y in range(int(r*2.2)):
                    if mask.getpixel((x, y)) > 128:
                        px, py = int(cx - r*1.1 + x), int(cy - r*1.1 + y)
                        if 0 <= px < img.width and 0 <= py < img.height:
                            dist_from_edge = min([
                                abs((py - cy) - (px - cx) * (points[i+1][1] - points[i][1]) / (points[i+1][0] - points[i][0] + 0.001))
                                for i in range(len(points) - 1)
                            ] + [abs((py - cy) - (px - cx) * (points[0][1] - points[-1][1]) / (points[0][0] - points[-1][0] + 0.001))])

                            if dist_from_edge > border_width:
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
                            shadow_px, shadow_py = px + 5, py + 5
                            if 0 <= shadow_px < img.width and 0 <= shadow_py < img.height:
                                current = img.getpixel((shadow_px, shadow_py))
                                img.putpixel((shadow_px, shadow_py), (max(0, current[0] - 30), max(0, current[1] - 30), max(0, current[2] - 30), 255))

                            img.putpixel((px, py), (250, 250, 250, 255))

            border_width = 8
            for x in range(s):
                for y in range(s):
                    if heart_mask.getpixel((x, y)) > 128:
                        px, py = cx - s//2 + x, cy - s//2 + y
                        if border_width < x < s - border_width and border_width < y < s - border_width:
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
