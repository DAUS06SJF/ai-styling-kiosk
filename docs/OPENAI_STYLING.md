# AI 코디 추천 API (로컬 개발용)

행거 센서가 전달한 `hangerCode`로 현재 상품을 찾고, 재고가 있는 상품 중 최대 24개를 OpenAI Responses API에 전달해 코디 상품 3개를 추천한다. OpenAI가 존재하지 않는 상품 ID를 반환하더라도 서버에서 제거한 뒤 실제 DB 상품 정보로 응답한다.

## 로컬 실행

OpenAI API 키는 코드나 `application.yml`에 넣지 않고 환경변수로만 설정한다.

```powershell
$env:OPENAI_API_KEY="발급받은_API_키"
$env:OPENAI_MODEL="gpt-5.6-luna"

cd C:\University\ai-styling-kiosk\backend
.\gradlew.bat bootRun
```

기본 MySQL 접속 정보는 `localhost:3306`, DB 이름은 `styling`이다. 로컬 MySQL 계정이 기본값과 다르면 실행 전에 `DB_USERNAME`, `DB_PASSWORD`를 설정한다. 공용 Aiven DB를 사용할 때는 기존 `docs/DATABASE.md`의 환경변수를 함께 설정한다.

API 키가 없더라도 애플리케이션은 실행된다. 이 상태에서 추천 API를 호출하면 HTTP 503과 `OPENAI_NOT_CONFIGURED` 오류를 반환한다.

## API 호출

`POST http://localhost:8080/api/styling/recommendations`

```powershell
$body = @{
  hangerCode = "H-0001"
  occasion = "데이트"
  mood = "미니멀하고 고급스러운 분위기"
  preferredColors = @("Black", "White")
} | ConvertTo-Json

Invoke-RestMethod `
  -Uri "http://localhost:8080/api/styling/recommendations" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

성공하면 선택 상품, 코디 이름, 스타일링 팁, 추천 상품과 추천 이유를 반환한다. `occasion`, `mood`, `preferredColors`는 선택값이고 `hangerCode`만 필수다.

## 비용 관리

- 기본 모델은 비용을 줄이기 위해 `gpt-5.6-luna`로 설정했다.
- 요청당 후보 상품을 24개, 추천 결과를 3개로 제한했다.
- 모델과 개수는 `OPENAI_MODEL`, `OPENAI_CANDIDATE_LIMIT`, `OPENAI_RECOMMENDATION_COUNT` 환경변수로 바꿀 수 있다.
- API 키를 프론트엔드에 전달하지 않는다. 프론트엔드는 이 백엔드 API만 호출해야 한다.
- OpenAI 대시보드에서 사용량과 크레딧 만료일을 정기적으로 확인한다.

## 테스트

테스트는 실제 OpenAI 비용이 발생하지 않도록 외부 응답을 모킹한다.

```powershell
cd C:\University\ai-styling-kiosk\backend
.\gradlew.bat test
```
