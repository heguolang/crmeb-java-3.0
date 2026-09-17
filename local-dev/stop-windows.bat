@echo off
REM ============================================================
REM  CRMEB Java 3.0  -  stop local dev services (Windows)
REM  Usage: stop-windows.bat            -> stop apps only
REM         stop-windows.bat full       -> also stop Redis + MariaDB
REM ============================================================
setlocal

echo Stopping Admin API / Front API (java) ...
powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \"Name='java.exe'\" | Where-Object { $_.CommandLine -like '*Crmeb-admin.jar*' -or $_.CommandLine -like '*Crmeb-front.jar*' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }"

echo Stopping Admin Web (vue-cli-service on 9527) ...
powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \"Name='node.exe'\" | Where-Object { $_.CommandLine -like '*vue-cli-service*' -or $_.CommandLine -like '*crmeb-java-3.0\admin*' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }"

if /I "%1"=="full" (
  echo Stopping Redis ...
  powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \"Name='redis-server.exe'\" | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }"
  echo Stopping MariaDB ...
  powershell -NoProfile -Command "Get-CimInstance Win32_Process -Filter \"Name='mariadbd.exe'\" | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }"
)

echo Done.
endlocal
pause
