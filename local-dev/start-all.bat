@echo off
REM ==================================================================
REM  QIANXU Java 3.0 - START ALL local services
REM    Redis 6379 | Admin API 8080 | Front API 8081 | Admin Web 9527
REM
REM  Usage : double-click, or run from anywhere.
REM  Safe   : idempotent - any port already LISTENING is skipped.
REM  Note   : output is ASCII-only on purpose (GBK console safe).
REM ==================================================================
setlocal

set "JAVA_HOME=D:\env\java\jdk8u504-b01"
set "PROJ=D:\qianxu-java-3.0"
set "NODE_DIR=C:\Users\Administrator\.workbuddy\binaries\node\versions\22.22.2-3"
set "LOGS=%PROJ%\local-dev\logs"
set "JAVA_OPTS=-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"

if not exist "%LOGS%" mkdir "%LOGS%"

echo ===================================================
echo  QIANXU start-all   (Redis 6379 / Admin 8080 / Front 8081 / Web 9527)
echo ===================================================

REM ---------- Redis 6379 ----------
netstat -ano | findstr /C:":6379 " | findstr LISTENING >nul
if errorlevel 1 (
  start "qianxu-redis" /MIN "D:\env\redis\redis-server.exe" --port 6379 --requirepass 123456 --maxmemory 512mb
  echo [6379] Redis      -> starting
) else (
  echo [6379] Redis      -> already up
)

REM ---------- Admin API 8080 ----------
netstat -ano | findstr /C:":8080 " | findstr LISTENING >nul
if errorlevel 1 (
  start "qianxu-admin" /MIN "%JAVA_HOME%\bin\java.exe" %JAVA_OPTS% -jar "%PROJ%\qianxu\qianxu-admin\target\Qianxu-admin.jar" --server.port=8080
  echo [8080] Admin API  -> starting (~60s)
) else (
  echo [8080] Admin API  -> already up
)

REM ---------- Front API 8081 ----------
netstat -ano | findstr /C:":8081 " | findstr LISTENING >nul
if errorlevel 1 (
  start "qianxu-front" /MIN "%JAVA_HOME%\bin\java.exe" %JAVA_OPTS% -jar "%PROJ%\qianxu\qianxu-front\target\Qianxu-front.jar" --server.port=8081
  echo [8081] Front API  -> starting (~60s)
) else (
  echo [8081] Front API  -> already up
)

REM ---------- Admin Web 9527 (vue dev server) ----------
netstat -ano | findstr /C:":9527 " | findstr LISTENING >nul
if errorlevel 1 (
  start "qianxu-web" /MIN cmd /c "set NODE_OPTIONS=--openssl-legacy-provider&& cd /d "%PROJ%\admin"&& "%NODE_DIR%\node.exe" node_modules\@vue\cli-service\bin\vue-cli-service.js serve --port=9527"
  echo [9527] Admin Web  -> starting (webpack ~25s)
) else (
  echo [9527] Admin Web  -> already up
)

echo.
echo Waiting 75s for boot, then checking ports ...
timeout /t 75 /nobreak >nul

echo.
echo ---------------- PORT STATUS ----------------
for %%P in (3306 6379 8080 8081 9527) do (
  netstat -ano | findstr /C:":%%P " | findstr LISTENING >nul
  if errorlevel 1 (
    echo   %%P : DOWN
  ) else (
    echo   %%P : UP
  )
)
echo ----------------------------------------------
echo Done. Admin:  http://127.0.0.1:9527
echo       Api  :  http://127.0.0.1:8080   Front: http://127.0.0.1:8081
endlocal
