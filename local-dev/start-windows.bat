@echo off
REM ============================================================
REM  QIANXU Java 3.0  -  Windows local dev launcher
REM  Starts: MySQL 8.0(3306) -> Redis(6379) -> Admin API(8080)
REM          -> Front API(8081) -> Admin Web(9527)
REM  DB     : Windows service "MySQL80" (auto-start), root/123456, db crmeb
REM  Logs   : D:\qianxu-java-3.0\local-dev\logs
REM ============================================================
setlocal
set "JAVA_HOME=D:\env\java\jdk8u504-b01"
set "PROJ=D:\qianxu-java-3.0"
set "LOGS=%PROJ%\local-dev\logs"
REM MySQL 8.0 runs as a Windows service; we only need "net start" when it is down.
set "MYSQL_SVC=MySQL80"
set "MYSQL_HOME=D:\env\mysql-8.0.29-winx64"
set "REDIS_EXE=D:\env\redis\redis-server.exe"
set "NODE_DIR=C:\Users\Administrator\.workbuddy\binaries\node\versions\22.22.2-3"
REM Avoid inherited SERVER_PORT overriding Spring's server.port
set "SERVER_PORT="
REM Force UTF-8: on Chinese Windows Java 8 defaults to GBK, which garbles all JSON output
set "JAVA_OPTS=-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"
REM demoSite=false -> show full phone numbers (true masks them as 139****0004)
set "DEMO_FLAG=--crmeb.demoSite=false"

if not exist "%LOGS%" mkdir "%LOGS%"

echo [1/5] MySQL 8.0 3306 ...
netstat -ano | findstr /C:":3306 " | findstr LISTENING >nul
if errorlevel 1 (
  net start %MYSQL_SVC% >nul 2>&1
  if errorlevel 1 (
    echo       could not start service %MYSQL_SVC%
    echo       run this script as Administrator, or start "%MYSQL_HOME%\bin\mysqld.exe" manually
  ) else (
    echo       started service %MYSQL_SVC%
  )
) else ( echo       already running )

echo [2/5] Redis 6379 ...
netstat -ano | findstr /C:":6379 " | findstr LISTENING >nul
if errorlevel 1 (
  start "" /B "%REDIS_EXE%" --port 6379 --requirepass 123456 --maxmemory 512mb
  echo       started
) else ( echo       already running )

echo [3/5] Admin API 8080 ...
netstat -ano | findstr /C:":8080 " | findstr LISTENING >nul
if errorlevel 1 (
  start "qianxu-admin" /B "%JAVA_HOME%\bin\java.exe" %JAVA_OPTS% -jar "%PROJ%\qianxu\qianxu-admin\target\Qianxu-admin.jar" --server.port=8080 %DEMO_FLAG% > "%LOGS%\qianxu-admin.log" 2>&1
  echo       starting
) else ( echo       already running )

echo [4/5] Front API 8081 ...
netstat -ano | findstr /C:":8081 " | findstr LISTENING >nul
if errorlevel 1 (
  start "qianxu-front" /B "%JAVA_HOME%\bin\java.exe" %JAVA_OPTS% -jar "%PROJ%\qianxu\qianxu-front\target\Qianxu-front.jar" --server.port=8081 %DEMO_FLAG% > "%LOGS%\qianxu-front.log" 2>&1
  echo       starting
) else ( echo       already running )

echo [5/5] Admin Web 9527 ...
netstat -ano | findstr /C:":9527 " | findstr LISTENING >nul
if errorlevel 1 (
  set "PATH=%NODE_DIR%;%PATH%"
  set "NODE_OPTIONS=--openssl-legacy-provider"
  start "qianxu-admin-web" /B cmd /c "npm --prefix "%PROJ%\admin" run dev -- --port=9527 > "%LOGS%\admin-web.log" 2>&1"
  echo       starting ^(first run may take 1-2 min^)
) else ( echo       already running )

echo.
echo Waiting for services (about 60-90s) ...
timeout /t 75 /nobreak >nul

echo.
echo ================= Service URLs =================
echo Admin web      : http://127.0.0.1:9527   (admin / 123456)
echo Admin API      : http://127.0.0.1:8080   docs http://127.0.0.1:8080/doc.html
echo Front API      : http://127.0.0.1:8081
echo MySQL 8.0      : 127.0.0.1:3306  root / 123456  db crmeb   (service %MYSQL_SVC%)
echo Redis          : 127.0.0.1:6379  password 123456
echo Logs           : %LOGS%
echo ===============================================
endlocal
pause
