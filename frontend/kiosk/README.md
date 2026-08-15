# 키오스크 앱 (담당: 정소민 · 김주완)

매장 터치스크린 키오스크에서 실행되는 고객용 화면입니다. 기능(feature)별로 폴더를 나눠 두 명이 나눠 개발합니다.

## 담당 구역

| 폴더 | 담당 | 내용 |
|---|---|---|
| `src/features/styling` | 정소민 | 실시간 AI 스타일링 제안 화면 (R-DGAACL) — 센서 이벤트로 갱신되는 메인 화면 |
| `src/features/fitting` | 정소민 | 가상 피팅·코디 상세/시뮬레이션 화면 |
| `src/features/personalization` | 김주완 | 로그인, 개인화 추천 목록, 좋아요/싫어요 피드백 UI (R-PXALGN) |
| `src/features/qr-share` | 김주완 | 스타일 QR 전송 화면 (R-SRYMEY) |
| `src/shared` | 정소민 | 공통 컴포넌트, 앱 셸/라우팅, 터치 UX 가이드, 다국어(i18n) (R-QWXBTF) |

## 초기화

```bash
npm create vite@latest . -- --template react-ts
# "Ignore files and continue" 선택 (README, src 폴더가 이미 있음)
npm install
npm run dev
```

백엔드 API 주소는 `.env`의 `VITE_API_BASE_URL`(기본 `http://localhost:8080`)로 관리합니다.
실시간 코디 WebSocket 주소는 `VITE_WS_URL`(기본 `ws://localhost:8080/ws/styling`)로 관리합니다.
설정 예시는 `frontend/.env.example`을 참고합니다.

코디 화면은 진입 시 `GET /api/styling/recommendations?mood={스타일}&limit=4`로 기존 이미지를
조회하고, 연결 이후에는 WebSocket의 `STYLING_RECOMMENDATION_CREATED` 이벤트를 받아 새 이미지를
자동으로 추가합니다. 저장하기를 누르면 `POST /api/styling/recommendations/{id}/select`를 호출합니다.

패드에서 실행할 때는 두 주소의 `localhost`를 백엔드가 실행되는 컴퓨터 또는 배포 서버의 IP/도메인으로
바꾸고, 백엔드의 `CORS_ALLOWED_ORIGINS`에도 패드가 접속한 프론트 주소를 추가해야 합니다.
