# Git 협업 가이드

5명이 같이 작업하기 때문에 규칙 없이 하면 서로 코드를 덮어쓰거나 빌드가 깨집니다.
아래 흐름만 지키면 사고가 거의 나지 않습니다.

## 딱 3가지 규칙

1. **`main`에 직접 push 하지 않는다.** 항상 브랜치를 만들어 PR로 합칩니다.
2. **브랜치를 만들기 전에 반드시 `git pull`** 을 한다. 충돌의 대부분이 이걸 안 해서 생깁니다.
3. **브랜치는 짧게 유지한다.** 하루이틀 안에 머지될 크기로 자릅니다.

---

## 처음 한 번만 — 저장소 받아오기

```bash
git clone https://github.com/DAUS06SJF/ai-styling-kiosk.git
cd ai-styling-kiosk
```

이미 받아둔 사람은 다시 받을 필요 없습니다.

---

## 매번 반복하는 작업 흐름

### 1. 최신 main 받아오기

작업 시작 전에는 **항상** 여기부터. 다른 팀원이 올린 변경사항을 먼저 가져와야 합니다.

```bash
git switch main
git pull origin main
```

### 2. 내 작업 브랜치 만들기

```bash
git switch -c feat/R-DGAACL-styling-api
```

`switch -c`의 `-c`는 create(새로 만들기)입니다. 이름 규칙은 아래 표를 참고하세요.

### 3. 작업하고 커밋하기

한 번에 몰아서 하지 말고, 의미 있는 단위로 나눠서 여러 번 커밋하는 게 좋습니다.

```bash
git add .
git commit -m "스타일링 추천 API 기본 구조 추가"
```

`git add .`는 변경된 파일 전부를 담는다는 뜻입니다. 특정 파일만 담으려면 `git add 파일경로`.

### 4. GitHub에 내 브랜치 올리기

```bash
git push -u origin feat/R-DGAACL-styling-api
```

`-u`는 처음 한 번만 붙이면 됩니다. 이후 같은 브랜치에서는 `git push`만 해도 됩니다.

### 5. PR(Pull Request) 만들기

GitHub 저장소에 들어가면 노란 배너에 **`Compare & pull request`** 버튼이 떠 있습니다.
누르고 → 무엇을 왜 바꿨는지 적고 → **`Create pull request`** 클릭.

팀원이 확인하고 **`Merge pull request`** 를 누르면 `main`에 합쳐집니다.

### 6. 머지된 뒤 정리

```bash
git switch main
git pull origin main
git branch -d feat/R-DGAACL-styling-api
```

그리고 다시 1번부터 반복합니다.

---

## 브랜치 이름 규칙

`<종류>/<요구사항ID>-<간단한설명>` 형태로 짓습니다. 영어 소문자와 하이픈만 사용하세요.

| 종류 | 언제 쓰나 | 예시 |
|---|---|---|
| `feat/` | 새 기능 개발 | `feat/R-DGAACL-styling-api` |
| `fix/` | 버그 수정 | `fix/R-SRYMEY-qr-expire` |
| `docs/` | 문서만 수정 | `docs/readme-update` |
| `refactor/` | 기능 변화 없이 구조 개선 | `refactor/product-entity` |

### 팀원별 예시

| 팀원 | 담당 | 브랜치 예시 |
|---|---|---|
| 이승원 | AI 스타일링 제안 | `feat/R-DGAACL-styling-api` |
| 전유리 | 행동 데이터 분석 | `feat/R-ZUQBEM-analytics` |
| 서의진 | 센서 이벤트 수신 | `feat/HW-SENSOR-hanger-event` |
| 정소민 | 키오스크 UI | `feat/R-QWXBTF-kiosk-ui` |
| 김주완 | 관리자 대시보드 | `feat/R-UUXNUG-admin-dashboard` |

> 브랜치 이름에 한글을 쓰면 환경에 따라 깨질 수 있으니 영어로 지어주세요.

---

## 커밋 메시지 쓰는 법

제목은 **무엇을 했는지** 한 줄로. 필요하면 빈 줄을 두고 이유를 덧붙입니다.

```
좋은 예:  스타일링 추천 API에 캐싱 적용
          센서 이벤트 중복 수신 문제 수정

나쁜 예:  수정
          ㅁㄴㅇㄹ
          asdf
```

나중에 "이 코드 왜 이렇게 됐지?" 를 추적할 때 커밋 메시지가 유일한 단서가 됩니다.

---

## 충돌(Conflict)이 났을 때

PR 화면에 `This branch has conflicts that must be resolved` 가 뜨면 내 브랜치에서 이렇게 합니다.

```bash
git switch main
git pull origin main
git switch -          # 직전 브랜치(내 작업 브랜치)로 돌아가기
git merge main
```

충돌한 파일을 열면 이런 표시가 있습니다.

```
<<<<<<< HEAD
내가 쓴 코드
=======
다른 사람이 쓴 코드
>>>>>>> main
```

**둘 중 남길 코드만 두고 `<<<<<<<`, `=======`, `>>>>>>>` 세 줄은 지웁니다.**
둘 다 필요하면 둘 다 남기고 표시만 지우면 됩니다. 정리한 뒤:

```bash
git add .
git commit -m "main 브랜치와 충돌 해결"
git push
```

PR이 자동으로 갱신됩니다.

---

## 자주 쓰는 명령어

| 명령어 | 설명 |
|---|---|
| `git status` | 지금 상태 확인 (제일 자주 씀) |
| `git branch` | 내 브랜치 목록 보기 |
| `git switch 브랜치명` | 브랜치 이동 |
| `git log --oneline -10` | 최근 커밋 10개 보기 |
| `git diff` | 아직 커밋 안 한 변경 내용 보기 |
| `git restore 파일명` | 특정 파일 수정 취소 (되돌리기) |

---

## 이럴 땐 어떻게 하나요

**Q. 실수로 main에서 작업해버렸어요 (아직 커밋 전)**

작업 내용을 그대로 새 브랜치로 옮길 수 있습니다.

```bash
git switch -c feat/R-DGAACL-styling-api
```

**Q. 커밋 메시지를 잘못 썼어요 (아직 push 전)**

```bash
git commit --amend -m "제대로 된 메시지"
```

이미 push했다면 그냥 두세요. 고치려다 더 꼬입니다.

**Q. 남이 올린 최신 코드를 내 브랜치에도 반영하고 싶어요**

```bash
git switch main && git pull origin main && git switch - && git merge main
```

**Q. pull 했더니 `Please commit your changes or stash them` 이라고 나와요**

작업 중인 내용이 있어서 그렇습니다. 커밋하거나, 잠시 치워두세요.

```bash
git stash        # 잠시 치워두기
git pull origin main
git stash pop    # 다시 꺼내기
```
