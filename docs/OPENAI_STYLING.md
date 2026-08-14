# AI 코디 추천 API (로컬 개발용)

행거 센서가 전달한 `hangerCode`로 현재 상품을 찾고, 재고가 있는 상품 중 최대 24개를 OpenAI Responses API에 전달해 코디 상품 3개를 추천한다. 검증된 상품 조합은 GPT Image API에 다시 전달해 코디 이미지를 생성하며, 이미지 파일과 접근 URL을 저장한 뒤 실제 상품 정보와 함께 응답한다.

추천 결과는 다음 두 테이블에 저장된다.

- `styling_recommendations`: 선택 상품, 상황, 분위기, 선호 색상, 코디 이름, 스타일링 팁, 코디 이미지 URL(`kodi`), 사용자가 최종 선택한 코디 이미지 URL(`kodi_selected`)
- `styling_recommendation_items`: 코디에 포함된 추천 상품, 추천 이유, 표시 순서

`spring.jpa.hibernate.ddl-auto=update` 설정 때문에 백엔드를 실행하면 두 테이블이 자동 생성된다.

## 로컬 실행

OpenAI API 키는 코드나 `application.yml`에 넣지 않고 환경변수로만 설정한다.

```powershell
$env:OPENAI_API_KEY="발급받은_API_키"
$env:OPENAI_MODEL="gpt-5.6-luna"
$env:OPENAI_IMAGE_MODEL="gpt-image-2"

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

성공하면 DB에 저장된 코디 ID(`data.id`)와 AI 생성 이미지 URL(`data.kodi`), 선택 상품, 코디 이름, 스타일링 팁, 추천 상품과 추천 이유를 반환한다. `occasion`, `mood`, `preferredColors`는 선택값이고 `hangerCode`만 필수다.

로컬 이미지 파일은 기본적으로 `backend/generated-stylings`에 저장되고 다음 주소로 제공된다.

```text
http://localhost:8080/generated-stylings/{파일명}.png
```

배포할 때는 `STYLING_IMAGE_STORAGE_DIRECTORY`와 `STYLING_IMAGE_PUBLIC_BASE_URL`을 배포 환경에 맞게 설정해야 한다. 서버의 로컬 디스크가 초기화되는 배포 환경에서는 추후 S3 같은 영구 오브젝트 스토리지로 교체해야 한다.

저장된 코디는 반환받은 ID로 다시 조회할 수 있다.

```text
GET http://localhost:8080/api/styling/recommendations/{id}
```

키오스크에서 사용자가 코디를 고른 뒤 **저장하기**를 누르면 다음 API를 호출한다.

```text
POST http://localhost:8080/api/styling/recommendations/{id}/select
```

호출 전 `kodiSelected`는 `null`이며, 정상 처리되면 선택한 코디의 `kodi` 이미지 URL이 `styling_recommendations.kodi_selected`에 복사되어 저장된다. 다음 QR 화면은 응답의 `id`와 `kodiSelected`를 사용한다.

## 비용 관리

- 코디 정보 모델은 `gpt-5.6-luna`, 이미지 생성 모델은 `gpt-image-2`로 설정했다.
- 요청당 후보 상품을 24개, 추천 결과를 3개로 제한했다.
- 모델과 개수는 `OPENAI_MODEL`, `OPENAI_IMAGE_MODEL`, `OPENAI_CANDIDATE_LIMIT`, `OPENAI_RECOMMENDATION_COUNT` 환경변수로 바꿀 수 있다.
- API 키를 프론트엔드에 전달하지 않는다. 프론트엔드는 이 백엔드 API만 호출해야 한다.
- OpenAI 대시보드에서 사용량과 크레딧 만료일을 정기적으로 확인한다.

## 테스트

테스트는 실제 OpenAI 비용이 발생하지 않도록 외부 응답을 모킹한다.

```powershell
cd C:\University\ai-styling-kiosk\backend
.\gradlew.bat test
```
