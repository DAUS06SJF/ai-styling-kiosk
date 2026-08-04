# =========================================================
# 백엔드 개발용 MySQL 초기 설정 스크립트
#
# 하는 일 3가지
#   1) styling / styling_test 데이터베이스 생성
#   2) DB_USERNAME, DB_PASSWORD 환경변수 등록
#   3) 접속 확인
#
# 실행 (계정이 root 인 경우):
#   powershell -ExecutionPolicy Bypass -File setup-db.ps1
#
# 계정이 root 가 아니면:
#   powershell -ExecutionPolicy Bypass -File setup-db.ps1 -DbUser 계정명
#
# 물어보는 것은 비밀번호 하나뿐입니다.
# 입력해도 화면에 표시되지 않는 것이 정상입니다.
# =========================================================

param(
    [string]$DbUser = "root"
)

$ErrorActionPreference = "Stop"

# --- MySQL 클라이언트 위치 찾기 -----------------------------------------
$mysqlExe = Get-ChildItem "C:\Program Files\MySQL" -Recurse -Filter "mysql.exe" -ErrorAction SilentlyContinue |
            Select-Object -First 1 -ExpandProperty FullName

if (-not $mysqlExe) {
    Write-Host "[실패] mysql.exe 를 찾지 못했습니다. MySQL Server 가 설치되어 있는지 확인하세요." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "MySQL 클라이언트 : $mysqlExe"
Write-Host "접속 계정        : $DbUser"
Write-Host ""

# --- 비밀번호 입력 (화면에 표시되지 않음) --------------------------------
Write-Host "$DbUser 계정의 MySQL 비밀번호를 입력한 뒤 Enter 를 누르세요." -ForegroundColor Yellow
Write-Host "(입력하는 동안 아무 글자도 보이지 않는 것이 정상입니다)" -ForegroundColor DarkGray
$securePw = Read-Host -AsSecureString

$plainPw = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePw))

if ([string]::IsNullOrEmpty($plainPw)) {
    Write-Host "[실패] 비밀번호가 입력되지 않았습니다." -ForegroundColor Red
    exit 1
}

# 명령행에 비밀번호가 노출되지 않도록 환경변수로 전달한다.
$env:MYSQL_PWD = $plainPw

try {
    # --- 1) 데이터베이스 생성 -------------------------------------------
    $sql = "CREATE DATABASE IF NOT EXISTS styling DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci; " +
           "CREATE DATABASE IF NOT EXISTS styling_test DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

    & $mysqlExe -u $DbUser -e $sql
    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "[실패] 데이터베이스 생성에 실패했습니다." -ForegroundColor Red
        Write-Host "       Access denied 라면 비밀번호가 틀린 것입니다. 다시 실행해 보세요." -ForegroundColor Red
        exit 1
    }
    Write-Host "[완료] styling / styling_test 데이터베이스 생성" -ForegroundColor Green

    # --- 2) 환경변수 등록 -----------------------------------------------
    [Environment]::SetEnvironmentVariable("DB_USERNAME", $DbUser,  "User")
    [Environment]::SetEnvironmentVariable("DB_PASSWORD", $plainPw, "User")
    Write-Host "[완료] DB_USERNAME / DB_PASSWORD 환경변수 등록" -ForegroundColor Green

    # --- 3) 접속 확인 ---------------------------------------------------
    $found = & $mysqlExe -u $DbUser -N -B -e "SHOW DATABASES LIKE 'styling%';"
    Write-Host "[확인] 생성된 데이터베이스 : $($found -join ', ')" -ForegroundColor Green
}
finally {
    # 환경에서 비밀번호 흔적 제거
    Remove-Item Env:\MYSQL_PWD -ErrorAction SilentlyContinue
    $plainPw = $null
}

Write-Host ""
Write-Host "=========================================================" -ForegroundColor Cyan
Write-Host " 설정 완료" -ForegroundColor Cyan
Write-Host ""
Write-Host " PowerShell 과 IntelliJ 를 완전히 껐다 켠 뒤 실행하세요:" -ForegroundColor Cyan
Write-Host "   cd C:\University\ai-styling-kiosk\backend" -ForegroundColor White
Write-Host "   .\gradlew bootRun" -ForegroundColor White
Write-Host ""
Write-Host " 확인 주소 : http://localhost:8080/api/health" -ForegroundColor Cyan
Write-Host "=========================================================" -ForegroundColor Cyan
