@echo off
REM ============================================================
REM  qianxu local one-click start (fast path: no build, no wait, idempotent)
REM
REM  Usage:
REM    up.bat          backend only: MySQL(3306)+Redis(6379) if down, admin:8080 + front:8081
REM    up.bat web      also start admin-web dev:9527 (first run 1-2 min, for UI work)
REM
REM  Prereq: jars exist under target (only rebuild after Java code changes):
REM    cd qianxu ^&^& bash ../local-dev/mvnw.sh -o -DskipTests -pl qianxu-common,qianxu-service,qianxu-admin,qianxu-front -am clean package
REM  Stop: down.bat    Logs: local-dev\logs\
REM  NOTE: keep this file ASCII only - cmd parses it as GBK, Chinese chars break it
REM ============================================================
setlocal
set "JAVA_HOME=D:\env\java\jdk8u504-b01"
set "PROJ=D:\WorkBuddy\qianxu-java5.0"
set "LOGS=%PROJ%\local-dev\logs"
set "MYSQL_SVC=MySQL80"
set "REDIS_EXE=D:\env\redis\redis-server.exe"
set "NODE_DIR=C:\Users\Administrator\.workbuddy\binaries\node\versions\22.22.2-6"
rem MUST clear SERVER_PORT: inherited env value would hijack --server.port
set "SERVER_PORT="
set "JAVA_OPTS=-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"
set "DEMO_FLAG=--qianxu.demoSite=false"
if not exist "%LOGS%" mkdir "%LOGS%"

echo [1/4] MySQL 3306 ...
netstat -ano | findstr /C:":3306 " | findstr LISTENING >nul
if errorlevel 1 (
  net start %MYSQL_SVC% >nul 2>&1
  if errorlevel 1 (echo       [!] FAIL: run as Administrator or start mysqld manually) else (echo       started service %MYSQL_SVC%)
) else ( echo       already running )

echo [2/4] Redis 6379 ...
netstat -ano | findstr /C:":6379 " | findstr LISTENING >nul
if errorlevel 1 (
  start "" /B "%REDIS_EXE%" --port 6379 --requirepass 123456 --maxmemory 512mb >nul 2>&1
  echo       started
) else ( echo       already running )

echo [3/4] Admin API 8080 ...
if not exist "%PROJ%\qianxu\qianxu-admin\target\Qianxu-admin.jar" (
  echo       [!] jar missing - run the mvn package command above first
) else (
  netstat -ano | findstr /C:":8080 " | findstr LISTENING >nul
  if errorlevel 1 (
    start "qianxu-admin" /MIN "%JAVA_HOME%\bin\java.exe" %JAVA_OPTS% -jar "%PROJ%\qianxu\qianxu-admin\target\Qianxu-admin.jar" --server.port=8080 %DEMO_FLAG% > "%LOGS%\qianxu-admin.log" 2>&1
    echo       starting... ^(ready in ~30s^)
  ) else ( echo       already running )
)

echo [4/4] Front API 8081 ...
if not exist "%PROJ%\qianxu\qianxu-front\target\Qianxu-front.jar" (
  echo       [!] jar missing - run the mvn package command above first
) else (
  netstat -ano | findstr /C:":8081 " | findstr LISTENING >nul
  if errorlevel 1 (
    start "qianxu-front" /MIN "%JAVA_HOME%\bin\java.exe" %JAVA_OPTS% -jar "%PROJ%\qianxu\qianxu-front\target\Qianxu-front.jar" --server.port=8081 %DEMO_FLAG% > "%LOGS%\qianxu-front.log" 2>&1
    echo       starting... ^(ready in ~30s^)
  ) else ( echo       already running )
)

if /I "%~1"=="web" (
  echo [+] Admin Web 9527 ...
  netstat -ano | findstr /C:":9527 " | findstr LISTENING >nul
  if errorlevel 1 (
    start "qianxu-admin-web" "%NODE_DIR%\node.exe" --openssl-legacy-provider "%PROJ%\admin\node_modules\@vue\cli-service\bin\vue-cli-service.js" serve --port 9527 >> "%LOGS%\admin-web.log" 2>&1
    echo       starting... ^(first run 1-2 min^)
  ) else ( echo       already running )
)

echo.
echo ================= Service URLs =================
echo   Admin web : http://127.0.0.1:9527   ^(needs "up.bat web", admin/123456^)
echo   Admin API : http://127.0.0.1:8080   docs: /doc.html
echo   Front API : http://127.0.0.1:8081
echo   MySQL     : 127.0.0.1:3306  root/123456  db=qianxuqifu
echo   Redis     : 127.0.0.1:6379  password 123456
echo   Logs      : %LOGS%
echo ================================================
echo   backend ready in ~30s. stop with: down.bat
endlocal
