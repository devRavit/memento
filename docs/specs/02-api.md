# API 명세

## API 설계 (초안)

### 1. 기념품 생성 요청

```http
POST /api/v1/souvenirs
Content-Type: application/json

{
  "reservationId": "string",
  "productId": "string",
  "userPhotos": ["base64_image_1", "base64_image_2"],  // 선택사항
  "style": "poster|magazine|photobook|auto"  // auto는 AI가 자동 선택
}
```

**Response**
```json
{
  "souvenirId": "string",
  "status": "processing|completed|failed",
  "previewUrl": "string",
  "estimatedTime": 30
}
```

### 2. 기념품 조회

```http
GET /api/v1/souvenirs/{souvenirId}
```

**Response**
```json
{
  "souvenirId": "string",
  "status": "processing|completed|failed",
  "style": "poster",
  "imageUrl": "string",
  "createdAt": "2025-11-06T10:00:00Z",
  "metadata": {
    "photoCount": 2,
    "detectedTheme": "romantic",
    "aiSuggestion": "영화 포스터 스타일"
  }
}
```

### 3. 사용자 기념품 목록

```http
GET /api/v1/users/{userId}/souvenirs
```

**Response**
```json
{
  "items": [
    {
      "souvenirId": "string",
      "thumbnailUrl": "string",
      "createdAt": "2025-11-06T10:00:00Z",
      "style": "poster"
    }
  ],
  "total": 10
}
```

### 4. 상품별 기본 사진 조회

```http
GET /api/v1/products/{productId}/photos
```

**Response**
```json
{
  "productId": "string",
  "photos": [
    {
      "photoId": "string",
      "url": "string",
      "category": "landscape|hotel|activity|food",
      "tags": ["sunset", "beach", "jeju"]
    }
  ]
}
```

## 데이터 모델 (초안)

### Souvenir
```kotlin
data class Souvenir(
    val id: String,
    val userId: String,
    val reservationId: String,
    val productId: String,
    val style: SouvenirStyle,
    val status: SouvenirStatus,
    val basePhotoIds: List<String>,
    val userPhotoUrls: List<String>,
    val resultImageUrl: String?,
    val metadata: SouvenirMetadata,
    val createdAt: Instant,
    val updatedAt: Instant
)

enum class SouvenirStyle {
    POSTER, MAGAZINE, PHOTOBOOK, MINIMAL_ART
}

enum class SouvenirStatus {
    PROCESSING, COMPLETED, FAILED
}

data class SouvenirMetadata(
    val photoCount: Int,
    val detectedTheme: String?,
    val aiSuggestion: String?,
    val processingTime: Long?
)
```

## AI 처리 플로우

1. **사진 분석**
   - 사용자 사진 개수 확인
   - 사진 내용 분석 (인물/풍경/음식/활동)
   - 여행 테마 추론

2. **스타일 결정**
   - 사진 개수별 기본 스타일 매칭
   - 사진 내용별 세부 스타일 조정
   - 사용자 명시적 선택 우선

3. **베이스 사진 선택**
   - 상품 사진 DB에서 적합한 사진 선택
   - 테마/분위기에 맞는 사진 매칭

4. **이미지 생성**
   - AWS Bedrock 호출
   - 베이스 사진 + 사용자 사진 합성
   - 스타일별 템플릿 적용

5. **결과 저장 및 반환**
   - S3에 결과 이미지 저장
   - URL 생성 및 반환
