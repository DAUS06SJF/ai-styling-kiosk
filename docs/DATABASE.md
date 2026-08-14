# 공용 개발 MySQL

개발·시연용 공용 DB는 Aiven MySQL을 사용한다.

## 접속 정보

| 항목 | 값 |
|---|---|
| Host | `ai-styling-mysql-ai-styling-kiosk.l.aivencloud.com` |
| Port | `28226` |
| Database | `styling` |
| Username | `styling_team` |
| SSL mode | `REQUIRED` |

비밀번호는 저장소에 커밋하지 않는다. Aiven Console의 `ai-styling-mysql` 서비스에서
`Connect > Users > styling_team > Copy`로 복사해 팀의 비공개 채널이나 비밀번호
관리 도구로만 공유한다.

## 백엔드 실행

PowerShell에서 현재 세션에만 환경변수를 설정한 뒤 실행한다.

```powershell
$env:DB_HOST='ai-styling-mysql-ai-styling-kiosk.l.aivencloud.com'
$env:DB_PORT='28226'
$env:DB_NAME='styling'
$env:DB_USERNAME='styling_team'
$env:DB_PASSWORD='<Aiven에서 복사한 비밀번호>'
$env:DB_SSL_MODE='REQUIRED'

cd backend
.\gradlew.bat bootRun
```

첫 실행에서 JPA가 테이블을 만들고 `data.sql`의 상품 시드 데이터를 적재한다.
비밀번호를 코드, 문서, 커밋, PR 또는 공개 채팅에 넣지 않는다.

## 테스트 DB 보호

테스트는 `TEST_DB_*` 환경변수를 별도로 사용하며 기본값은 로컬
`styling_test`이다. 공용 `styling` DB를 테스트 대상으로 지정하지 않는다.
테스트 설정은 `create-drop`이므로 지정한 스키마의 테이블을 만들었다가 삭제한다.
