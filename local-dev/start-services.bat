@echo off
REM Launch Redis + Admin API(8080) + Front API(8081) for local dev
setlocal
set "JAVA_HOME=D:\env\java\jdk8u504-b01"
set "PROJ=D:\crmeb-java-3.0"
set "LOGS=%PROJ%\local-dev\logs"
set "SERVER_PORT="
if not exist "%LOGS%" mkdir "%LOGS%"

set "JAVA_OPTS=-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"
set "DEMO_FLAG=--crmeb.demoSite=false"

echo [1/3] Redis 6379 ...
netstat -ano | findstr /C:":6379 " | findstr LISTENING >nul
if errorlevel 1 (
  start "crmeb-redis" /B "D:\env\redis\redis-server.exe" --port 6379 --requirepass 123456 --maxmemory 512mb
  echo       started
) else ( echo       already running )

echo [2/3] Admin API 8080 ...
netstat -ano | findstr /C:":8080 " | findstr LISTENING >nul
if errorlevel 1 (
  start "crmeb-admin" /B "%JAVA_HOME%\bin\java.exe" %JAVA_OPTS% -jar "%PROJ%\crmeb\crmeb-admin\target\Crmeb-admin.jar" --server.port=8080 %DEMO_FLAG%
  echo       starting
) else ( echo       already running )

echo [3/3] Front API 8081 ...
netstat -ano | findstr /C:":8081 " | findstr LISTENING >nul
if errorlevel 1 (
  start "crmeb-front" /B "%JAVA_HOME%\bin\java.exe" %JAVA_OPTS% -jar "%PROJ%\crmeb\crmeb-front\target\Crmeb-front.jar" --server.port=8081 %DEMO_FLAG%
  echo       starting
) else ( echo       already running )

endlocal
