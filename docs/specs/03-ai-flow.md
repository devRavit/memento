# AI 처리 플로우

## 전체 흐름

```
사용자 요청
    ↓
1. 사진 분석
    ↓
2. 테마 추론
    ↓
3. 스타일 결정
    ↓
4. 베이스 사진 선택
    ↓
5. AI 이미지 생성
    ↓
6. 결과 반환
```

## 1️⃣ 사진 분석 단계

### 입력
- 사용자가 업로드한 사진 (0~N장)
- 예약/상품 정보

### 처리
- **사진 개수 확인**
- **사진 내용 분석** (Vision AI)
  - 인물 감지
  - 풍경 감지
  - 물체 감지 (음식, 활동 등)
- **품질 검사**
  - 해상도 확인
  - 얼굴 선명도 확인

### 출력
```json
{
  "photoCount": 2,
  "categories": ["person", "landscape"],
  "hasFace": true,
  "quality": "good",
  "mainSubjects": ["couple", "beach"]
}
```

## 2️⃣ 테마 추론 단계

### 사진 분석 결과 기반 테마 매칭

| 사진 특징 | 추론 테마 | 키워드 |
|---------|---------|--------|
| 인물 2명 + 해변 | Romantic | 로맨틱, 커플, 감성 |
| 인물 3명 이상 | Family | 가족, 단체, 즐거움 |
| 풍경만 | Nature | 자연, 힐링, 여유 |
| 음식 위주 | Gourmet | 미식, 맛집, 현지 |
| 액티비티 | Adventure | 모험, 도전, 활동 |

### 출력
```json
{
  "theme": "romantic",
  "confidence": 0.85,
  "keywords": ["couple", "beach", "sunset"]
}
```

## 3️⃣ 스타일 결정 단계

### 사진 개수별 기본 스타일

| 사진 개수 | 기본 스타일 | 설명 |
|----------|-----------|------|
| 0장 | Minimal Art | 상품 사진만으로 미니멀한 포스터 |
| 1장 | Poster | 영화 포스터 스타일 |
| 2-3장 | Magazine | 매거진 표지 + 내지 |
| 5장 이상 | Photobook | 스토리텔링 포토북 |

### 테마별 세부 조정

**Romantic 테마**
- 톤: 따뜻한 색감 (Warm tone)
- 배경: 석양, 노을, 해변
- 문구: "Our Special Moment"

**Family 테마**
- 톤: 밝고 경쾌한 색감
- 배경: 넓은 공간, 활동적인 장소
- 문구: "Family Traveler"

**Nature 테마**
- 톤: 자연스러운 색감
- 배경: 파노라마 풍경
- 문구: "Nature's Gift"

### 출력
```json
{
  "style": "poster",
  "subStyle": "movie_romantic",
  "tone": "warm",
  "template": "romantic_poster_v1"
}
```

## 4️⃣ 베이스 사진 선택

### 상품 사진 DB 쿼리
```sql
SELECT * FROM product_photos
WHERE product_id = :productId
  AND category IN ('landscape', 'hotel')
  AND tags CONTAINS ANY ('beach', 'sunset')
ORDER BY quality_score DESC
LIMIT 3
```

### 선택 기준
1. **테마 일치도** (최우선)
2. **품질 점수** (해상도, 구도)
3. **다양성** (여러 각도/시간대)

### 출력
```json
{
  "selectedPhotos": [
    {
      "photoId": "photo_001",
      "url": "https://...",
      "category": "landscape",
      "matchScore": 0.92
    }
  ]
}
```

## 5️⃣ AI 이미지 생성

### AWS Bedrock 호출

#### 프롬프트 구성
```
Create a romantic travel poster with the following elements:
- Base image: [Jeju beach sunset photo]
- User photos: [Couple selfie]
- Style: Movie poster with warm tone
- Text overlay: "Our Special Moment in Jeju"
- Layout: Portrait orientation, couple in foreground, beach in background
```

#### 파라미터
```json
{
  "modelId": "stability.stable-diffusion-xl-v1",
  "imageGenerationConfig": {
    "numberOfImages": 1,
    "quality": "premium",
    "height": 1024,
    "width": 768,
    "cfgScale": 8.0,
    "seed": 42
  }
}
```

### 후처리
- 텍스트 오버레이 추가
- 브랜딩 요소 추가 (야놀자 로고 등)
- 최종 품질 검사

## 6️⃣ 결과 저장 및 반환

### S3 저장
```
s3://memento-souvenirs/
  ├── {userId}/
  │   ├── {souvenirId}/
  │   │   ├── original.png
  │   │   ├── preview.jpg
  │   │   └── metadata.json
```

### 응답
```json
{
  "souvenirId": "sov_abc123",
  "status": "completed",
  "imageUrl": "https://cdn.memento.com/...",
  "previewUrl": "https://cdn.memento.com/.../preview.jpg",
  "metadata": {
    "theme": "romantic",
    "style": "poster",
    "processingTime": 28000
  }
}
```

## 에러 처리

### 1. 사진 품질 불량
```json
{
  "error": "PHOTO_QUALITY_LOW",
  "message": "업로드된 사진의 해상도가 너무 낮습니다",
  "suggestion": "다른 사진을 선택하거나 사진 없이 생성해보세요"
}
```

### 2. AI 생성 실패
```json
{
  "error": "AI_GENERATION_FAILED",
  "message": "이미지 생성 중 오류가 발생했습니다",
  "fallback": "기본 템플릿으로 대체 생성 중..."
}
```

### 3. 타임아웃
```json
{
  "error": "PROCESSING_TIMEOUT",
  "message": "처리 시간이 초과되었습니다",
  "retryable": true
}
```
