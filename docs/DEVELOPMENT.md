# 개발 가이드

## 기본 설정

### 애플리케이션 포트
- **포트**: 9998
- **설정 파일**: `src/main/resources/application.properties`

```properties
server.port=9998
```

### 로컬 실행
```bash
./gradlew bootRun
```

애플리케이션이 `http://localhost:9998`에서 실행됩니다.

## 개발 규칙 (필수)

### 코드 작성 후 검증 프로세스

모든 코드 개발 후 **반드시** 다음 순서로 검증합니다:

1. **ktlint 검사**
2. **빌드 테스트**
3. **실행 확인** (필요시)

### 1️⃣ ktlint 검사

#### 검사 실행
```bash
./gradlew ktlintCheck
```

#### 자동 포맷팅
```bash
./gradlew ktlintFormat
```

**규칙**:
- ktlint 에러가 있으면 절대 커밋하지 않음
- 자동 포맷팅으로 해결 안 되는 경우 수동 수정 필수

### 2️⃣ 빌드 테스트

```bash
./gradlew clean build
```

**규칙**:
- 빌드 실패 시 원인 파악 후 수정
- 테스트 실패 시 테스트 코드 수정 또는 구현 수정
- 경고(warning)도 가능한 해결

### 3️⃣ 전체 검증 스크립트

편의를 위해 한 번에 실행:

```bash
./gradlew ktlintFormat && ./gradlew clean build
```

## 코드 스타일

### Kotlin 코딩 컨벤션
- [Kotlin 공식 코딩 컨벤션](https://kotlinlang.org/docs/coding-conventions.html) 준수
- ktlint 기본 설정 사용

### 주요 규칙
- 들여쓰기: 4 spaces (탭 아님)
- 최대 라인 길이: 120자
- import 순서: 알파벳 순
- 사용하지 않는 import 제거

## 프로젝트 구조

```
memento/
├── docs/
│   ├── PROJECT.md              # 프로젝트 개요
│   ├── HACKATHON.md            # 해커톤 규칙
│   ├── DEVELOPMENT.md          # 개발 가이드 (이 파일)
│   └── specs/
│       ├── 01-concept.md       # 서비스 컨셉
│       ├── 02-api.md           # API 명세
│       └── 03-ai-flow.md       # AI 처리 플로우
├── src/
│   └── main/
│       ├── kotlin/com/aws/memento/
│       │   ├── MementoApplication.kt
│       │   ├── controller/           # REST API 엔드포인트
│       │   │   ├── SouvenirController.kt
│       │   │   └── dto/              # Request/Response DTO
│       │   ├── service/              # 비즈니스 로직
│       │   │   ├── SouvenirService.kt
│       │   │   ├── S3Service.kt
│       │   │   ├── GeminiService.kt
│       │   │   └── NovaCanvasService.kt
│       │   ├── domain/               # 도메인 모델
│       │   │   ├── Souvenir.kt
│       │   │   └── ImageAnalysis.kt
│       │   └── infrastructure/       # 외부 연동 (향후)
│       └── resources/
│           └── application.properties
├── build.gradle.kts
└── settings.gradle.kts
```

## 레이어별 책임

### Controller Layer
- HTTP 요청/응답 처리
- 입력 유효성 검증
- DTO 변환

### Service Layer
- 비즈니스 로직 구현
- 트랜잭션 관리
- 여러 인프라 서비스 조합

### Domain Layer
- 핵심 비즈니스 모델
- 도메인 로직
- 비즈니스 규칙

### Infrastructure Layer
- 외부 시스템 연동
- S3, Gemini AI, Nova Canvas 등

## API 테스트

### 기념품 생성 테스트

```bash
curl -X POST http://localhost:9998/api/v1/souvenirs \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "images": [
      {
        "fileName": "photo1.jpg",
        "contentType": "image/jpeg",
        "base64Data": "base64_encoded_data_here"
      }
    ],
    "preferredStyle": "REALISTIC",
    "preferredProductType": "POSTER"
  }'
```

### 기념품 조회 테스트

```bash
curl http://localhost:9998/api/v1/souvenirs/{souvenirId}
```

### 사용자별 목록 조회

```bash
curl http://localhost:9998/api/v1/souvenirs?userId=user123
```

## 개발 워크플로우

### 새 기능 개발 시

1. **기능 설계**
   - API 명세 확인/수정 (docs/specs/02-api.md)
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
   - 단위 테스트 작성 (시간 여유 있을 때)
   - API 수동 테스트 (curl 또는 Postman)

5. **문서 업데이트** (필수!)
   - API 변경사항 docs/specs/02-api.md 반영
   - 새로운 규칙은 docs/DEVELOPMENT.md 반영
   - **개발 완료 후 반드시 변경 이력을 문서화**

### 📝 개발 완료 후 문서화 규칙 (필수)

**모든 개발 완료 후 다음을 기록합니다:**

1. **변경 이력 기록**
   - 파일: `docs/CHANGELOG.md`
   - 형식:
     ```markdown
     ## [날짜] - 기능명

     ### 변경 내용
     - 추가된 기능/파일
     - 수정된 로직
     - 삭제된 코드

     ### 영향 범위
     - 변경된 레이어 (Controller/Service/Domain 등)
     - 관련 API 엔드포인트

     ### 테스트 방법
     - 검증 명령어
     - 테스트 시나리오
     ```

2. **README.md 업데이트**
   - 새로운 API 엔드포인트 추가 시 업데이트
   - 새로운 의존성 추가 시 설치 방법 업데이트
   - 환경 변수 변경 시 Configuration 섹션 업데이트

3. **API 명세 업데이트**
   - `docs/specs/02-api.md`에 새 엔드포인트 추가
   - Request/Response 예시 업데이트

**예시:**
```markdown
## [2025-11-06] - 기념품 생성 API 구현

### 변경 내용
- SouvenirController 추가 (POST /api/v1/souvenirs)
- SouvenirService 비즈니스 로직 구현
- S3Service, GeminiService, NovaCanvasService 인터페이스 정의

### 영향 범위
- Controller Layer: SouvenirController
- Service Layer: SouvenirService, S3Service, GeminiService, NovaCanvasService
- Domain Layer: Souvenir, ImageAnalysis

### 테스트 방법
```bash
./gradlew ktlintFormat && ./gradlew clean build
./gradlew bootRun
curl -X POST http://localhost:9998/api/v1/souvenirs ...
```
```

## Git 커밋 규칙

### 커밋 메시지 포맷
```
타입: 간결한 한 줄 설명

예시:
feat: 기념품 생성 API 구현
fix: S3 업로드 오류 수정
refactor: Service 레이어 리팩토링
docs: API 명세 업데이트
```

### 타입
- `feat`: 새로운 기능
- `fix`: 버그 수정
- `refactor`: 리팩토링
- `docs`: 문서 변경
- `style`: 코드 포맷팅
- `test`: 테스트 코드
- `chore`: 빌드 설정 등

### 커밋 전 체크리스트
- [ ] ktlint 통과
- [ ] 빌드 성공
- [ ] 관련 문서 업데이트
- [ ] 불필요한 주석 제거
- [ ] 디버그 코드 제거

## 트러블슈팅

### ktlint 에러 발생 시
```bash
./gradlew ktlintFormat
```
대부분의 포맷 문제는 자동으로 해결됩니다.

### 빌드 실패 시
1. 컴파일 에러 확인
2. 의존성 문제 확인
3. Gradle 캐시 정리
   ```bash
   ./gradlew clean
   ```

### 포트 충돌 시
```bash
lsof -i :9998
```
해당 프로세스 종료 후 재실행

## 유용한 Gradle 명령어

```bash
# 빌드 (테스트 포함)
./gradlew build

# 테스트만 실행
./gradlew test

# 애플리케이션 실행
./gradlew bootRun

# 의존성 트리 확인
./gradlew dependencies

# ktlint 검사
./gradlew ktlintCheck

# ktlint 자동 포맷
./gradlew ktlintFormat

# 전체 검증 (권장)
./gradlew ktlintFormat && ./gradlew clean build
```

## 참고 자료

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Kotlin 공식 문서](https://kotlinlang.org/docs/home.html)
- [ktlint 문서](https://pinterest.github.io/ktlint/)
- [AWS SDK for Java](https://aws.amazon.com/sdk-for-java/)
