# AI 기반 인터랙티브 리테일 스타일링 솔루션

매장을 방문한 고객이 옷걸이에서 옷을 선택하면, 행거 위 스크린/키오스크에서 AI가 고객 취향에 맞는 코디를 실시간으로 제안하는 서비스입니다.
온라인의 편리함과 오프라인의 감각적 경험을 연결해 매장 내 고객 경험을 혁신하는 것이 목표입니다.

> 상세 기획은 [docs/PRD.md](docs/PRD.md), 요구사항별 담당은 [docs/REQUIREMENTS.md](docs/REQUIREMENTS.md) 참고

## 팀 구성 및 역할

| 이름 | 파트 | 담당 |
|---|---|---|
| **이승원** | 백엔드 (리드) | 프로젝트 초기 세팅·공통 인프라(예외 처리, 응답 규격, CI), **실시간 AI 스타일링 제안 API** (R-DGAACL) — AI 연동, 코디 생성/제안 |
| **전유리** | 백엔드 | **회원/인증 + 개인화 추천** (R-PXALGN), **행동 데이터 수집·분석** (R-ZUQBEM), **관리자 API** (R-UUXNUG) |
| **서의진** | 백엔드 + 하드웨어 | **옷걸이 인식 센서** 구매·프로그래밍(`hardware/hanger-sensor`), **센서 이벤트 수신 API**(`domain/device`), **상품/재고 도메인**, **QR 온-오프라인 연동** (R-SRYMEY) |
| **정소민** | 프론트엔드 (리드) | 키오스크 **코어** — 앱 셸·라우팅·터치 UX·다국어 (R-QWXBTF), **스타일링 제안/가상 피팅 화면** (R-DGAACL) |
| **김주완** | 프론트엔드 | 키오스크 **개인화·피드백 화면** (R-PXALGN), **QR 전송 화면 + 모바일 스타일 뷰** (R-SRYMEY), **관리자 페이지** (R-UUXNUG, R-ZUQBEM 대시보드) |

## 기술 스택

- **백엔드**: Java 17, Spring Boot 3.x, Spring Data JPA, Gradle
- **프론트엔드**: React (Vite) — `frontend/kiosk`(키오스크), `frontend/admin`(관리자)
- **DB**: 개발 단계 H2 → 추후 MySQL/PostgreSQL 전환

## 폴더 구조

```
ai-styling-kiosk/
├── docs/               # PRD, 요구사항 문서
├── hardware/
│   └── hanger-sensor/  # 옷걸이 인식 센서 펌웨어/문서 (서의진)
├── backend/            # Spring Boot 서버 (단일 서버, 도메인별 패키지 분리)
│   └── src/main/java/com/hackathon/styling/
│       ├── domain/
│       │   ├── styling/     # AI 스타일링 제안 (이승원)
│       │   ├── member/      # 회원/인증/개인화 (전유리)
│       │   ├── analytics/   # 행동 데이터 수집·분석 (전유리)
│       │   ├── admin/       # 관리자 API (전유리)
│       │   ├── product/     # 상품/재고 (서의진)
│       │   ├── device/      # 센서 이벤트 수신 (서의진)
│       │   └── link/        # QR 온-오프라인 연동 (서의진)
│       └── global/          # 공통 설정·예외·응답 규격 (이승원)
├── frontend/
│   ├── kiosk/          # 키오스크 앱 (정소민·김주완 분담)
│   │   └── src/
│   │       ├── features/
│   │       │   ├── styling/          # 스타일링 제안 화면 (정소민)
│   │       │   ├── fitting/          # 가상 피팅/코디 상세 (정소민)
│   │       │   ├── personalization/  # 로그인·개인화·피드백 (김주완)
│   │       │   └── qr-share/         # QR 전송 화면 (김주완)
│   │       └── shared/               # 앱 셸·공통 컴포넌트·i18n (정소민)
│   ├── mobile-view/    # QR로 열리는 모바일 스타일 뷰 (김주완)
│   └── admin/          # 관리자 페이지 (김주완)
│       └── src/features/
│           ├── dashboard/   # 분석 리포트 대시보드
│           └── products/    # 상품/스타일 관리
└── README.md
```

## 시작하기

### 백엔드

```bash
cd backend
# Gradle Wrapper 최초 1회 생성 (로컬에 Gradle 설치되어 있을 때)
gradle wrapper
./gradlew bootRun
```

### 프론트엔드

각 폴더(`frontend/kiosk`, `frontend/admin`)의 README를 참고해 Vite 프로젝트를 초기화합니다.

## 브랜치 전략 (제안)

- `main`: 배포 가능한 상태 유지
- `develop`: 통합 브랜치
- `feat/<요구사항ID>-<설명>`: 기능 브랜치 (예: `feat/R-DGAACL-styling-api`)
