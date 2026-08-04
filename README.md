# AI 기반 인터랙티브 리테일 스타일링 솔루션

매장을 방문한 고객이 옷걸이에서 옷을 꺼내면, 옷걸이에 부착된 **인식 센서**가 이를 감지하고 행거 위 스크린/키오스크에서 **AI가 어울리는 코디를 실시간으로 제안**하는 서비스입니다.
온라인의 편리함과 오프라인의 감각적 경험을 연결해 매장 내 고객 경험을 혁신하는 것이 목표입니다.

> 상세 기획은 [docs/PRD.md](docs/PRD.md), 요구사항별 담당은 [docs/REQUIREMENTS.md](docs/REQUIREMENTS.md) 참고

## 팀 구성 및 역할

| 이름 | 파트 | 담당 |
|---|---|---|
| **이승원** | 백엔드 (리드) | 공통 인프라(응답 규격·예외 처리·CORS), **실시간 AI 스타일링 제안** (R-DGAACL) — AI 연동, 코디 생성, 키오스크 실시간 전달 |
| **전유리** | 백엔드 | **스타일/코디 데이터 도메인**, **행동 데이터 수집·분석** (R-ZUQBEM), **관리자 API** (R-UUXNUG) |
| **서의진** | 백엔드 + 하드웨어 | **옷걸이 인식 센서** 구매·프로그래밍, **센서 이벤트 수신 API**, **상품/재고 도메인**, **QR 온·오프라인 연동** (R-SRYMEY) |
| **정소민** | 프론트엔드 (리드) | 키오스크 **코어** — 앱 셸·라우팅·터치 UX·다국어 (R-QWXBTF), **스타일링 제안/가상 피팅 화면** (R-DGAACL) |
| **김주완** | 프론트엔드 | **QR 전송 화면 + 모바일 스타일 뷰** (R-SRYMEY), **관리자 페이지** (R-UUXNUG, R-ZUQBEM 대시보드) |

> **스코프 변경 (2026-08-04)**: 회원 인증 및 개인화 추천(R-PXALGN)은 제외하기로 결정했습니다.
> 이에 따라 로그인 없이 **세션 기반**으로 동작하며, 전유리 담당이던 회원/개인화 대신
> 스타일·코디 데이터 도메인을 맡는 것으로 조정했습니다. *(팀 확인 필요)*

## 기술 스택

| 영역 | 스택 |
|---|---|
| 백엔드 | Java 17, Spring Boot 3.3.5, Spring Data JPA, Gradle 9.2.1 |
| DB | **MySQL 8.0** (개발·테스트·운영 전부 MySQL) |
| 프론트엔드 | React + Vite |
| QR 서비스 | Node.js + Express (서의진 담당, 별도 서비스) |
| 하드웨어 | 옷걸이 인식 센서 (ESP32 기반 예정) |

## 폴더 구조

```
ai-styling-kiosk/
├── docs/                    # PRD, 요구사항 문서
├── hardware/
│   └── hanger-sensor/       # 옷걸이 인식 센서 펌웨어·문서 (서의진)
├── backend/                 # Spring Boot 서버
│   ├── gradlew, gradle/     # Gradle Wrapper (별도 설치 불필요)
│   └── src/main/java/com/hackathon/styling/
│       ├── domain/
│       │   ├── styling/     # AI 스타일링 제안 (이승원)
│       │   ├── analytics/   # 행동 데이터 수집·분석 (전유리)
│       │   ├── admin/       # 관리자 API (전유리)
│       │   ├── product/     # 상품/재고 (서의진)
│       │   ├── device/      # 센서 이벤트 수신 (서의진)
│       │   └── link/        # QR 연동 (서의진)
│       └── global/          # 공통 규격 — 아래 참고 (이승원) ✅ 완료
│           ├── common/      # ApiResponse, BaseTimeEntity, HealthController
│           ├── config/      # CorsConfig, JpaConfig
│           └── error/       # ErrorCode, BusinessException, GlobalExceptionHandler
└── frontend/
    ├── kiosk/               # 키오스크 앱 (정소민)
    │   └── src/
    │       ├── features/styling/   # 스타일링 제안 화면
    │       ├── features/fitting/   # 가상 피팅·코디 상세
    │       ├── features/qr-share/  # QR 전송 화면 (김주완)
    │       └── shared/             # 앱 셸·공통 컴포넌트·i18n
    ├── mobile-view/         # QR로 열리는 모바일 스타일 뷰 (김주완)
    └── admin/               # 관리자 페이지 (김주완)
        └── src/features/{dashboard,products}/
```

## 개발 환경 설정

### 1. 사전 요구사항

- **JDK 17** — Gradle 툴체인이 17을 요구합니다
- **MySQL 8.0** — 로컬에 설치 후 실행 중이어야 합니다
- **Node.js** — 프론트엔드 작업 시

Gradle은 설치할 필요가 없습니다. Wrapper(`gradlew`)가 포함되어 있습니다.

### 2. 데이터베이스 생성

MySQL에 접속해 개발용과 테스트용 스키마를 각각 만듭니다.

```sql
CREATE DATABASE styling DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE styling_test DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 3. 접속 정보 설정

비밀번호를 코드에 넣지 않도록 환경변수로 관리합니다. 기본값은 `root` / 빈 문자열입니다.

```bash
setx DB_USERNAME "root"
setx DB_PASSWORD "본인_MySQL_비밀번호"
```

설정 후 터미널이나 IntelliJ를 **재시작**해야 반영됩니다. IntelliJ를 쓴다면 실행 구성(Run Configuration)의 Environment variables에 넣어도 됩니다.

| 환경변수 | 기본값 | 설명 |
|---|---|---|
| `DB_HOST` | `localhost` | MySQL 호스트 |
| `DB_PORT` | `3306` | MySQL 포트 |
| `DB_NAME` | `styling` | 스키마명 (테스트는 `styling_test`) |
| `DB_USERNAME` | `root` | 계정 |
| `DB_PASSWORD` | (빈 값) | 비밀번호 |
| `SERVER_PORT` | `8080` | 서버 포트 |
| `CORS_ALLOWED_ORIGINS` | `localhost:5173,5174,5175` | 허용할 프론트 주소 |

## 실행 방법

### 백엔드

```bash
cd backend
./gradlew bootRun
```

동작 확인은 브라우저에서 `http://localhost:8080/api/health` 로 접속하면 됩니다.

```bash
curl http://localhost:8080/api/health
```

### 프론트엔드

각 폴더(`frontend/kiosk`, `frontend/admin`, `frontend/mobile-view`)에서 Vite 프로젝트를 초기화한 뒤 실행합니다. 자세한 내용은 각 폴더의 README를 참고하세요.

```bash
npm install && npm run dev
```

## API 공통 응답 규격

**모든 API는 아래 두 형태 중 하나로 응답합니다.** 프론트엔드는 이 구조를 기준으로 파싱하면 됩니다.

성공:

```json
{
  "success": true,
  "data": { "status": "UP" }
}
```

실패:

```json
{
  "success": false,
  "error": {
    "code": "PRODUCT_NOT_FOUND",
    "message": "상품 정보를 찾을 수 없습니다."
  }
}
```

HTTP 상태 코드는 에러 종류에 따라 함께 내려갑니다(400, 404, 500 등). 백엔드에서 새 에러가 필요하면 `global/error/ErrorCode.java`에 담당 도메인 구간을 찾아 추가하세요.

## 브랜치 전략

- `main` — 항상 빌드 가능한 상태 유지. **직접 push 금지**
- `feat/<요구사항ID>-<설명>` — 기능 브랜치 (예: `feat/R-DGAACL-styling-api`)
- 작업 완료 시 `main`으로 Pull Request 생성 → 리뷰 후 머지

```bash
git switch -c feat/R-DGAACL-styling-api
# 작업 후
git push -u origin feat/R-DGAACL-styling-api
```

## 진행 상황

| 항목 | 담당 | 상태 |
|---|---|---|
| 공통 인프라 (응답 규격·예외·CORS·MySQL) | 이승원 | ✅ 완료 |
| QR 사이트 (Express + 모바일 페이지) | 서의진 | 🚧 진행 중 |
| 실시간 AI 스타일링 제안 API | 이승원 | ⬜ 예정 |
| 옷걸이 인식 센서 | 서의진 | ⬜ 예정 |
| 상품/재고 도메인 | 서의진 | ⬜ 예정 |
| 행동 데이터 분석 / 관리자 API | 전유리 | ⬜ 예정 |
| 키오스크 화면 | 정소민 | ⬜ 예정 |
| 관리자 페이지 / 모바일 뷰 | 김주완 | ⬜ 예정 |

## 정리 필요 사항

- **QR 서비스 위치** — 현재 `backend/src/main/java/.../domain/link/`에 `server.js`와 `index.html`이 있습니다. Java 소스 트리라 Gradle 빌드에서 제외되어 실행되지 않습니다. 별도 서비스로 유지한다면 `services/qr-server/`로 옮기는 것이 맞습니다.
- **김주완 GitHub 계정 미등록** — 아이디 확인 후 저장소 협업자로 초대 필요합니다.
