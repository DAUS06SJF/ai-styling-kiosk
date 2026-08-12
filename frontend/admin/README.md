# 관리자 페이지 (담당: 김주완)

매장 운영자용 웹 화면입니다.

## 담당 구역

| 폴더 | 내용 |
|---|---|
| `src/features/dashboard` | 주간/월간 인기 스타일·아이템 분석 리포트 대시보드 (R-ZUQBEM) |
| `src/features/products` | 상품/스타일 관리 (R-UUXNUG) |

## 초기화

```bash
npm install
npm run dev
```

백엔드 API 주소는 `.env`의 `VITE_API_BASE_URL`(기본 `http://localhost:8080`)로 관리합니다.

## 구현된 기능

- 상품 등록, 목록/단건 조회, 수정, 삭제
- 상품명·행거 코드 검색, 카테고리 필터, 페이지 이동
- 재고 현황 및 품절/부족 표시
- 행거 코드 중복 오류를 포함한 API 오류 표시

개발 서버는 기본적으로 `http://localhost:5174`에서 실행됩니다. 다른 백엔드 주소를 사용할 때는 `.env`를 생성합니다.

```env
VITE_API_BASE_URL=http://localhost:8080
```
