@echo off
REM 从标准 deploy 目录复制出一套新客户目录（不含 data）
setlocal
set "NAME=%1"
if "%NAME%"=="" (
  echo Usage: new-customer.bat customer_code
  echo Example: new-customer.bat acme
  exit /b 1
)
set "SRC=%~dp0.."
set "DST=%~dp0..\..\customers\%NAME%"
if exist "%DST%" (
  echo Already exists: %DST%
  exit /b 1
)
mkdir "%DST%" 2>nul
xcopy "%SRC%\docker-compose.yml" "%DST%\" /Y >nul
xcopy "%SRC%\.env.example" "%DST%\" /Y >nul
xcopy "%SRC%\docker" "%DST%\docker\" /E /I /Y >nul
mkdir "%DST%\sql\init" 2>nul
xcopy "%SRC%\sql\init\README.md" "%DST%\sql\init\" /Y >nul
copy "%DST%\.env.example" "%DST%\.env" >nul
powershell -NoProfile -Command "(Get-Content '%DST%\.env') -replace 'COMPOSE_PROJECT_NAME=qianxu_demo','COMPOSE_PROJECT_NAME=qianxu_%NAME%' | Set-Content '%DST%\.env'"
echo Created %DST%
echo 1. Edit %DST%\.env passwords and ports
echo 2. Put SQL into %DST%\sql\init\
echo 3. cd %DST% ^&^& docker compose up -d
endlocal
