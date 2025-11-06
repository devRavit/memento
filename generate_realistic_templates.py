#!/usr/bin/env python3
import os
import json
import base64
import requests
from pathlib import Path

GEMINI_API_KEY = os.environ.get('GEMINI_API_KEY', '')
API_URL = 'https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent'

templates_dir = Path("src/main/resources/templates/goods")
templates_dir.mkdir(parents=True, exist_ok=True)

def generate_image_with_gemini(prompt: str, output_path: str):
    """Gemini 2.5 Flash Image를 사용하여 실사 이미지 생성"""

    payload = {
        "contents": [{
            "role": "user",
            "parts": [{
                "text": prompt
            }]
        }]
    }

    print(f"  Gemini API 호출 중...")
    response = requests.post(
        f"{API_URL}?key={GEMINI_API_KEY}",
        headers={'Content-Type': 'application/json'},
        json=payload,
        timeout=120
    )

    if response.status_code != 200:
        print(f"  ✗ API 오류: {response.status_code}")
        print(f"  응답: {response.text}")
        return False

    result = response.json()

    try:
        parts = result['candidates'][0]['content']['parts']
        image_data = None

        for part in parts:
            if 'inlineData' in part:
                image_data = part['inlineData']['data']
                break

        if image_data is None:
            raise KeyError("No inlineData found in response")

        image_bytes = base64.b64decode(image_data)

        with open(output_path, 'wb') as f:
            f.write(image_bytes)

        print(f"  ✓ 이미지 저장 완료: {output_path}")
        return True
    except (KeyError, IndexError) as e:
        print(f"  ✗ 이미지 데이터 추출 실패: {e}")
        print(f"  응답 구조: {json.dumps(result, indent=2)[:500]}")
        return False


# 각 굿즈별 실사 이미지 생성 프롬프트
templates = {
    'magnet': """
Generate a photorealistic top-down photograph of 6 photo magnets on a white modern refrigerator door.

COMPOSITION:
- Shot from directly above (bird's eye view)
- White/light gray refrigerator surface with subtle texture
- 6 magnets arranged aesthetically with natural spacing
- Soft natural lighting from upper left

MAGNET SHAPES (all with WHITE BORDERS, 8mm thick):
1. Top-left: Circle magnet (diameter 240px), white border
2. Top-center: Square magnet (240x240px), white border
3. Top-right: Hexagon magnet (diameter 240px), white border
4. Bottom-left: Circle magnet (diameter 240px), white border
5. Bottom-center: Heart-shaped magnet (140x140px), white border
6. Bottom-right: Square magnet (240x240px), white border

IMPORTANT - PHOTO AREAS:
Each magnet should have a BLANK WHITE CENTER area where customer photos will be placed later.
The white center should be clearly defined and surrounded by the white border frame.

LIGHTING & QUALITY:
- Professional product photography
- Soft shadows under each magnet (shows they're slightly elevated)
- Clean, minimal aesthetic
- High resolution, crisp details
- Refrigerator surface has subtle metallic sheen

STYLE: Premium e-commerce product photography, Apple store quality
IMAGE SIZE: 1400x1000 pixels
""",

    'calendar': """
Generate a photorealistic photograph of a premium desk calendar standing on a white oak desk.

PRODUCT DETAILS:
- Elegant standing desk calendar with spiral binding at top
- Viewed at a slight 15-degree angle (not flat, shows depth)
- Thick cardstock pages with matte finish
- Silver spiral binding visible at top

LAYOUT:
- Upper 60%: BLANK WHITE AREA for customer photo placement
- Lower 40%: Monthly calendar grid for December 2025
- Clean white background behind the calendar
- Subtle wood desk surface visible

CALENDAR GRID (bottom portion):
- "DECEMBER 2025" header in elegant sans-serif font
- 7 columns (Sun-Sat), 5 rows
- Light gray grid lines
- Professional typography

LIGHTING & QUALITY:
- Soft studio lighting from upper left
- Gentle shadow on desk surface (right side)
- Premium stationery aesthetic
- Professional e-commerce photography quality

STYLE: Luxury desk accessories, Muji/minimalist aesthetic
IMAGE SIZE: 1200x1600 pixels
""",

    'frame': """
Generate a photorealistic photograph of an elegant wooden photo frame hanging on a light gray wall.

PRODUCT DETAILS:
- Classic dark walnut wood frame, 4cm wide
- Viewed straight-on (perpendicular to wall)
- Frame dimensions: visible border + large blank center
- Subtle wood grain texture visible on frame

FRAME STRUCTURE:
- Outer frame: Dark walnut wood (40mm wide border)
- Inner matting: White matting (30mm wide)
- Center: BLANK WHITE AREA for customer photo placement
- Glass surface: subtle reflection/glare in one corner

WALL & LIGHTING:
- Clean light gray textured wall (not pure white)
- Natural lighting from upper left
- Soft shadow on right and bottom of frame
- Shadow shows frame is slightly elevated from wall

DETAILS:
- Frame looks premium and well-crafted
- Slight perspective/depth visible
- Professional interior photography quality

STYLE: High-end home decor, museum-quality framing
IMAGE SIZE: 1200x1600 pixels
""",

    'sticker': """
Generate a photorealistic top-down photograph of 4 premium glossy photo stickers on a pure white surface.

COMPOSITION:
- Bird's eye view (directly from above)
- 4 identical stickers arranged in 2x2 grid pattern
- Slight rotation/angle variation between stickers (looks natural)
- One sticker in bottom-right has corner slightly peeled up (shows backing paper)

STICKER DETAILS (each one):
- Square format with rounded corners
- Glossy vinyl surface with visible light reflections
- White border (3mm) around the edge
- Center: BLANK WHITE AREA for customer photo placement
- Die-cut edges (perfectly clean)

LIGHTING & REFLECTIONS:
- Professional studio lighting
- Glossy surface shows bright highlights and reflections
- Soft shadows where stickers meet surface
- Peeled corner shows subtle shadow underneath
- White backing paper visible on peeled sticker

QUALITY:
- Ultra-high resolution product photo
- Premium sticker material visible
- Commercial photography standard (like Sticker Mule quality)

STYLE: E-commerce product photography, premium vinyl stickers
IMAGE SIZE: 1200x1200 pixels
""",

    'poster': """
Generate a photorealistic photograph of a large poster print slightly rolled on one end, on a clean white surface.

PRODUCT DETAILS:
- A3 size poster (horizontal orientation preferred)
- Premium matte art paper (thick, high-quality)
- Poster is mostly flat but one corner/edge is slightly curled
- Shot at slight angle to show paper thickness and quality

POSTER LAYOUT:
- Center: Large BLANK WHITE AREA for customer photo/artwork
- Minimal white border around edges (5-10mm)
- Paper texture visible (matte finish, not glossy)

SURFACE & LIGHTING:
- Clean white surface below
- Natural daylight lighting from side
- Soft shadows showing poster is elevated
- Highlights on matte surface showing paper quality
- Slight curl shows thickness of paper stock

QUALITY INDICATORS:
- Premium art print aesthetic
- Professional gallery-quality appearance
- Clean, crisp edges
- Museum-grade printing look

STYLE: Art gallery prints, professional poster printing service
IMAGE SIZE: 1200x1700 pixels
""",

    'postcard': """
Generate a photorealistic photograph of a premium photo postcard on a clean white surface.

PRODUCT DETAILS:
- Standard postcard size (148x105mm / 6x4 inches)
- Horizontal orientation
- Thick cardstock paper (350gsm premium feel)
- Slightly tilted (15-20 degrees) on surface

POSTCARD LAYOUT - FRONT SIDE:
- Center: Large BLANK WHITE AREA for customer photo
- Thin white border around edges (3-5mm)
- Premium matte or semi-gloss finish
- Rounded corners (optional, subtle)

LIGHTING & DETAILS:
- Soft natural lighting from upper left
- Gentle shadow cast on surface (right side)
- Paper thickness visible from side angle
- High-quality cardstock texture visible

SURFACE:
- Clean white minimal surface
- Possibly light oak wood or white marble
- Very subtle texture/grain

QUALITY:
- Boutique stationery aesthetic
- Premium postcard printing service quality
- Professional product photography

STYLE: Luxury stationery, Moo.com or Artifact Uprising quality
IMAGE SIZE: 1400x1000 pixels
""",

    'photobook': """
Generate a photorealistic photograph of a premium hardcover photobook, partially open.

PRODUCT DETAILS:
- Luxury hardcover photobook with dark brown genuine leather texture
- Book is positioned at 45-degree angle, partially open
- Shows left cover, spine, and 2-3 visible pages
- Thick, high-quality pages with clean edges

BOOK STRUCTURE:
- Cover: Dark brown leather with subtle texture
- Spine: Professional bookbinding visible, same leather
- Pages: Thick matte paper, off-white/cream color
- One visible page shows: Large BLANK WHITE AREA (for photo placement)

PAGE LAYOUT:
- Right-side visible page: BLANK WHITE RECTANGLE (customer photo area)
- Minimal margins around photo area
- Clean, professional page layout
- Page slightly curved (shows book depth)

SURFACE & LIGHTING:
- Clean white or light oak wood surface
- Soft diffused lighting from upper left
- Gentle shadow underneath and to right
- Leather cover shows subtle light reflections (not too shiny)

QUALITY:
- Ultra-premium photobook aesthetic
- Professional bookbinding visible
- Museum-quality presentation
- Looks like $100+ product

STYLE: Luxury photo albums, Artifact Uprising / Leather Photo Book quality
IMAGE SIZE: 1200x1600 pixels
""",

    'wall-calendar': """
Generate a photorealistic photograph of a wall calendar hanging on a light gray wall.

PRODUCT DETAILS:
- Large wall calendar (A3 size)
- Spiral bound at top with metal coil binding
- Hanging from a small nail/hook (visible at top)
- Viewed straight-on

CALENDAR LAYOUT:
- Upper 60%: Large BLANK WHITE AREA for customer photo
- Lower 40%: Monthly calendar grid

CALENDAR GRID (December 2025):
- "DECEMBER 2025" header in clean typography
- 7 columns (S M T W T F S)
- 5-6 rows for dates
- Light gray grid lines
- Minimal, clean design

WALL & LIGHTING:
- Light gray textured wall (interior wall)
- Natural daylight from left side
- Soft shadow on right side of calendar
- Slightly elevated from wall (shows depth)

BINDING DETAILS:
- Metal spiral coil at top clearly visible
- Two punch holes at top for hanging
- Pages look thick and premium

QUALITY:
- Professional calendar printing quality
- Premium home decor aesthetic
- Interior design photography standard

STYLE: Premium wall calendars, Rifle Paper Co. quality
IMAGE SIZE: 1200x1600 pixels
"""
}


def main():
    if not GEMINI_API_KEY:
        print("❌ 오류: GEMINI_API_KEY 환경변수가 설정되지 않았습니다.")
        print("export GEMINI_API_KEY='your-api-key'")
        return

    print("🎨 실사 굿즈 템플릿 이미지 생성 시작...")
    print(f"📁 저장 경로: {templates_dir.absolute()}\n")

    success_count = 0

    for goods_type, prompt in templates.items():
        print(f"🔄 {goods_type} 템플릿 생성 중...")
        output_path = templates_dir / f"{goods_type}_template.png"

        if generate_image_with_gemini(prompt, output_path):
            success_count += 1

        print()  # 빈 줄

    print(f"\n✅ 완료: {success_count}/{len(templates)} 템플릿 생성 성공")


if __name__ == "__main__":
    main()
