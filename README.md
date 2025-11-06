# Memento - AI 여행 기념품 생성 서비스

> "모든 여행이 작품이 되는 경험"

**10X-THON 2025 해커톤 프로젝트**

AI가 여행 사진을 분석하고, 고품질 상품 사진과 결합하여 세상에 단 하나뿐인 예술 작품으로 변환하는 기념품 생성 서비스입니다.

## 📋 목차

- [프로젝트 개요](#프로젝트-개요)
- [핵심 기능](#핵심-기능)
- [기술 스택](#기술-스택)
- [시작하기](#시작하기)
- [프로젝트 구조](#프로젝트-구조)
- [API 문서](#api-문서)
- [개발 가이드](#개발-가이드)
- [문서](#문서)
- [라이선스](#라이선스)

## 🎯 프로젝트 개요

### 서비스 컨셉

기존의 "리뷰 작성 → 기념품 제공" 방식의 문제점:
- 리뷰용 사진은 품질이 낮음
- 리뷰 작성 자체가 귀찮음
- 심리적 반발감 ("리뷰 쓰면 준다"는 조건부 제공)

**Memento의 해결책:**
1. 야놀자의 **고품질 상품 사진**을 기본 제공
2. 사용자가 원하면 **개인 사진 추가** (선택사항)
3. **AI가 자동으로 분석 및 합성**하여 예술 작품 생성
4. 무료 디지털 파일 제공, 마음에 들면 실물 제작 가능

### 차별화 포인트

✅ **실패 없는 품질 보장**: 프로 사진이 베이스
✅ **빠른 생성**: 30초 내 완성
✅ **개인화**: 사진 추가 시 세상에 단 하나
✅ **부담 없음**: 무료 디지털 파일, 실물은 선택

## 🎨 핵심 기능

### 1. 사진 장수별 AI 맞춤 제안

| 사진 개수 | 생성 스타일 | 설명 |
|----------|-----------|------|
| 0장 | Minimal Art | 상품 사진만으로 미니멀한 포스터 |
| 1장 | Movie Poster | 영화 포스터 스타일 |
| 2-3장 | Magazine | 매거진 표지 + 내지 구성 |
| 5장 이상 | Photobook | 스토리텔링 포토북 |

### 2. 이미지 분석 및 테마 추론

- **인물 사진** → 감성 포커스 (따뜻한 톤, 석양 배경)
- **풍경 사진** → 자연 강조 (파노라마, 내셔널지오그래픽 스타일)
- **음식 사진** → 미식 여행 (요리 매거진 스타일)
- **활동 사진** → 다이나믹 (스포츠 매거진 스타일)

### 3. AI 처리 플로우

```
사용자 이미지 업로드
    ↓
S3 저장
    ↓
Gemini AI 분석
  - 이미지 특징 추출 (인물/배경/스타일)
  - 테마 추론
  - Nova Canvas 프롬프트 생성
    ↓
Nova Canvas 이미지 생성
    ↓
결과 이미지 반환
```

## 🛠 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.7
- **Language**: Kotlin 1.9.25
- **Build Tool**: Gradle (Kotlin DSL)
- **JDK**: Java 21

### AI & Cloud
- **Image Analysis**: Google Gemini AI
- **Image Generation**: AWS Bedrock Nova Canvas
- **Storage**: AWS S3
- **Infrastructure**: AWS (Sandbox)

### Development Tools
- **Code Style**: ktlint
- **Testing**: JUnit 5, Kotlin Test

## 🚀 시작하기

### 사전 요구사항

- JDK 21
- Gradle 8.14.3+
- AWS 계정 (Sandbox)
- Google Gemini API Key

### 설치 및 실행

1. **저장소 클론**
   ```bash
   git clone <repository-url>
   cd memento
   ```

2. **환경 변수 설정** (향후)
   ```bash
   cp .env.example .env
   # .env 파일에 AWS, Gemini API 키 설정
   ```

3. **빌드**
   ```bash
   ./gradlew clean build
   ```

4. **실행**
   ```bash
   ./gradlew bootRun
   ```

5. **애플리케이션 접속**
   ```
   http://localhost:9998
   ```

### 개발 환경 검증

```bash
# ktlint 검사 및 자동 포맷
./gradlew ktlintFormat

# 빌드 및 테스트
./gradlew clean build

# 전체 검증 (권장)
./gradlew ktlintFormat && ./gradlew clean build
```

## 📁 프로젝트 구조

```
memento/
├── docs/                           # 문서
│   ├── PROJECT.md                  # 프로젝트 개요
│   ├── HACKATHON.md                # 해커톤 가이드
│   ├── DEVELOPMENT.md              # 개발 가이드
│   ├── CHANGELOG.md                # 변경 이력
│   └── specs/                      # 상세 스펙
│       ├── 01-concept.md           # 서비스 컨셉
│       ├── 02-api.md               # API 명세
│       └── 03-ai-flow.md           # AI 처리 플로우
├── src/
│   ├── main/
│   │   ├── kotlin/com/aws/memento/
│   │   │   ├── MementoApplication.kt       # 메인 애플리케이션
│   │   │   ├── controller/                 # REST API 컨트롤러
│   │   │   │   ├── SouvenirController.kt
│   │   │   │   └── dto/                    # Request/Response DTO
│   │   │   │       ├── SouvenirRequest.kt
│   │   │   │       └── SouvenirResponse.kt
│   │   │   ├── service/                    # 비즈니스 로직
│   │   │   │   ├── SouvenirService.kt      # 기념품 생성 서비스
│   │   │   │   ├── S3Service.kt            # S3 업로드 서비스
│   │   │   │   ├── GeminiService.kt        # Gemini AI 분석 서비스
│   │   │   │   └── NovaCanvasService.kt    # Nova Canvas 생성 서비스
│   │   │   ├── domain/                     # 도메인 모델
│   │   │   │   ├── Souvenir.kt             # 기념품 모델
│   │   │   │   └── ImageAnalysis.kt        # 이미지 분석 결과
│   │   │   └── infrastructure/             # 외부 시스템 연동 (향후)
│   │   └── resources/
│   │       └── application.yml             # 애플리케이션 설정
│   └── test/                               # 테스트 코드
├── build.gradle.kts                        # Gradle 빌드 설정
├── settings.gradle.kts
├── .gitignore
└── README.md                               # 이 파일
```

### 레이어별 책임

#### Controller Layer
- HTTP 요청/응답 처리
- 입력 유효성 검증
- DTO 변환

#### Service Layer
- 비즈니스 로직 구현
- 여러 인프라 서비스 조합
- 트랜잭션 관리

#### Domain Layer
- 핵심 비즈니스 모델
- 도메인 로직
- 비즈니스 규칙

#### Infrastructure Layer
- 외부 시스템 연동
- S3, Gemini AI, Nova Canvas 등

## 📡 API 문서

### Base URL
```
http://localhost:9998/api/v1
```

### 엔드포인트

#### 1. 기념품 생성

**Request:**
```http
POST /souvenirs
Content-Type: application/json

{
  "userId": "user123",
  "images": [
    {
      "fileName": "photo1.jpg",
      "contentType": "image/jpeg",
      "base64Data": "base64_encoded_image_data"
    }
  ],
  "preferredStyle": "REALISTIC",
  "preferredProductType": "POSTER"
}
```

**Response:**
```json
{
  "id": "sov_abc123",
  "userId": "user123",
  "status": "COMPLETED",
  "inputImageUrls": [
    "https://memento-bucket.s3.amazonaws.com/souvenirs/sov_abc123/photo1.jpg"
  ],
  "style": "REALISTIC",
  "productType": "POSTER",
  "analysis": {
    "features": [
      {
        "type": "PERSON",
        "description": "Two people smiling at the beach",
        "importance": 0.9
      }
    ],
    "suggestedStyle": "REALISTIC",
    "suggestedProductType": "POSTER",
    "confidence": 0.85
  },
  "generatedImageUrl": "https://memento-bucket.s3.amazonaws.com/generated/image_123.png",
  "createdAt": "2025-11-06T10:00:00Z",
  "updatedAt": "2025-11-06T10:00:30Z"
}
```

#### 2. 기념품 조회

```http
GET /souvenirs/{id}
```

#### 3. 사용자별 기념품 목록

```http
GET /souvenirs?userId={userId}
```

**상세 API 명세**: [docs/specs/02-api.md](docs/specs/02-api.md)

## 🔧 개발 가이드

### 개발 워크플로우

1. **기능 설계**
   - API 명세 확인/수정
   - 도메인 모델 검토

2. **코드 작성**
   - Domain 모델 (필요시)
   - Service 인터페이스 및 구현
   - Controller 엔드포인트

3. **검증** (필수!)
   ```bash
   ./gradlew ktlintFormat && ./gradlew clean build
   ```

4. **테스트**
   - API 수동 테스트
   - 단위 테스트 작성

5. **문서화** (필수!)
   - `docs/CHANGELOG.md`에 변경 이력 기록
   - README.md 업데이트 (필요시)
   - API 명세 업데이트

### 코드 스타일

- Kotlin 공식 코딩 컨벤션 준수
- ktlint 자동 포맷팅 사용
- 들여쓰기: 4 spaces
- 최대 라인 길이: 120자
- Wildcard import 금지

### Git 커밋 규칙

```
[타입] 간결한 한 줄 설명

예시:
[feat] 기념품 생성 API 구현
[fix] S3 업로드 오류 수정
[refactor] Service 레이어 리팩토링
[docs] API 명세 업데이트
```

**타입:**
- `feat`: 새로운 기능
- `fix`: 버그 수정
- `refactor`: 리팩토링
- `docs`: 문서 변경
- `style`: 코드 포맷팅
- `test`: 테스트 코드
- `chore`: 빌드 설정 등

**상세 개발 가이드**: [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)

## 📚 문서

### 필수 문서
- [PROJECT.md](docs/PROJECT.md) - 프로젝트 개요 및 핵심 정보
- [HACKATHON.md](docs/HACKATHON.md) - 10X-THON 2025 해커톤 가이드
- [DEVELOPMENT.md](docs/DEVELOPMENT.md) - 개발 규칙 및 가이드
- [CHANGELOG.md](docs/CHANGELOG.md) - 변경 이력

### 상세 스펙
- [01-concept.md](docs/specs/01-concept.md) - 서비스 컨셉 및 차별화 전략
- [02-api.md](docs/specs/02-api.md) - API 명세 및 데이터 모델
- [03-ai-flow.md](docs/specs/03-ai-flow.md) - AI 처리 플로우

## 🧪 테스트

### API 테스트 예시

```bash
# 기념품 생성
curl -X POST http://localhost:9998/api/v1/souvenirs \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "images": [
      {
        "fileName": "test.jpg",
        "contentType": "image/jpeg",
        "base64Data": "test_base64_data"
      }
    ],
    "preferredStyle": "REALISTIC",
    "preferredProductType": "POSTER"
  }'

# 기념품 조회
curl http://localhost:9998/api/v1/souvenirs/{souvenirId}

# 사용자별 목록 조회
curl http://localhost:9998/api/v1/souvenirs?userId=user123
```

## 🚧 현재 상태 및 향후 계획

### ✅ 완료
- [x] 프로젝트 초기 설정
- [x] API 스켈레톤 구현
- [x] 도메인 모델 정의
- [x] Service 레이어 인터페이스 정의
- [x] 문서화 체계 구축

### 🔄 진행 중
- [ ] AWS S3 실제 연동
- [ ] Gemini AI API 연동
- [ ] Nova Canvas (AWS Bedrock) 연동

### 📋 향후 계획
- [ ] 데이터 저장소 구현 (DynamoDB/RDS)
- [ ] 에러 핸들링 및 로깅
- [ ] 통합 테스트 작성
- [ ] 성능 최적화
- [ ] 프론트엔드 개발
- [ ] 배포 자동화

## 👥 팀

**10X-THON 2025 해커톤 참가팀**

## 📄 라이선스

이 프로젝트는 10X-THON 2025 해커톤을 위한 프로젝트입니다.

---

**"모든 여행이 작품이 되는 야놀자"** 🎨
