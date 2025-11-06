# 변경 이력

## [2025-11-06] - Health API 및 CORS 설정 추가

### 변경 내용

#### Health Check API
- `HealthController`: Health check 엔드포인트 추가
  - GET `/api/v1/health`: 서버 상태 확인 (status, timestamp)
  - GET `/api/v1`: 서비스 정보 (service, version, status)
- `HealthResponse`: Health check 응답 DTO

#### CORS 설정
- `WebConfig`: CORS 설정 추가
  - Frontend (localhost:9999) 허용
  - GET, POST, PUT, DELETE, OPTIONS 메서드 허용
  - Credentials 지원

### 영향 범위
- **Frontend 연동 기반 구축**
- Controller Layer: HealthController
- Config Layer: WebConfig

### 테스트 방법
```bash
# ktlint 및 빌드 검증
./gradlew ktlintCheck && ./gradlew build -x test

# 애플리케이션 실행
./gradlew bootRun

# Health 엔드포인트 테스트
curl http://localhost:9998/api/v1/health
curl http://localhost:9998/api/v1
```

### 다음 단계
- [ ] Frontend와 실제 연동 테스트
- [ ] AWS S3 실제 연동 구현
- [ ] Gemini AI API 연동 구현
- [ ] Nova Canvas (AWS Bedrock) 연동 구현

---

## [2025-11-06] - 프로젝트 초기 설정 및 API 스켈레톤 구현

### 변경 내용

#### 프로젝트 설정
- Spring Boot 3.5.7 + Kotlin 1.9.25 프로젝트 생성
- Gradle Kotlin DSL 설정
- ktlint 플러그인 추가 및 코드 스타일 설정
- application.yml 설정 (포트 9998)
- .gitignore 설정 (Gradle, IDE, macOS 파일)

#### 도메인 모델
- `Souvenir`: 기념품 도메인 모델
  - 상태: UPLOADING, ANALYZING, GENERATING, COMPLETED, FAILED
  - 스타일: ANIMATION, REALISTIC, CARTOON, WATERCOLOR, OIL_PAINTING, SKETCH
  - 제품 타입: CALENDAR, PHOTOBOOK, POSTER, POSTCARD, THREE_D_PRINT
- `ImageAnalysis`: 이미지 분석 결과 모델
- `ImageFeature`: 이미지 특징 모델

#### Controller Layer
- `SouvenirController`
  - POST `/api/v1/souvenirs`: 기념품 생성
  - GET `/api/v1/souvenirs/{id}`: 기념품 조회
  - GET `/api/v1/souvenirs?userId=`: 사용자별 기념품 목록

#### Service Layer
- `SouvenirService`: 기념품 생성 비즈니스 로직 (스켈레톤)
- `S3Service`: S3 이미지 업로드 인터페이스 (Mock)
- `GeminiService`: Gemini AI 이미지 분석 인터페이스 (Mock)
- `NovaCanvasService`: Nova Canvas 이미지 생성 인터페이스 (Mock)

#### DTO
- `CreateSouvenirRequest`: 기념품 생성 요청
- `SouvenirResponse`: 기념품 응답
- `ImageAnalysisResponse`: 이미지 분석 응답

#### 문서
- `docs/PROJECT.md`: 프로젝트 개요
- `docs/HACKATHON.md`: 10X-THON 2025 해커톤 가이드
- `docs/DEVELOPMENT.md`: 개발 가이드 및 규칙
- `docs/specs/01-concept.md`: 서비스 컨셉
- `docs/specs/02-api.md`: API 명세
- `docs/specs/03-ai-flow.md`: AI 처리 플로우

### 영향 범위
- **전체 프로젝트 구조 생성**
- Controller Layer: SouvenirController
- Service Layer: SouvenirService, S3Service, GeminiService, NovaCanvasService
- Domain Layer: Souvenir, ImageAnalysis, ImageFeature

### 테스트 방법
```bash
# ktlint 및 빌드 검증
./gradlew ktlintFormat && ./gradlew clean build

# 애플리케이션 실행
./gradlew bootRun

# API 테스트 (기본 구조만 동작, AI 연동은 Mock)
curl -X POST http://localhost:9998/api/v1/souvenirs \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "images": [
      {
        "fileName": "test.jpg",
        "contentType": "image/jpeg",
        "base64Data": "test_data"
      }
    ],
    "preferredStyle": "REALISTIC",
    "preferredProductType": "POSTER"
  }'
```

### 다음 단계
- [ ] AWS S3 실제 연동 구현
- [ ] Gemini AI API 연동 구현
- [ ] Nova Canvas (AWS Bedrock) 연동 구현
- [ ] 데이터 저장소 구현 (DynamoDB 또는 RDS)
- [ ] 에러 핸들링 및 로깅
- [ ] 통합 테스트 작성

---
