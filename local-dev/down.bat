@echo off
REM ============================================================
REM  qianxu local one-click stop
REM  kills processes LISTENING on 8080 / 8081 / 9527 (local dev only)
REM  outbound connections to remote :8080 are NOT matched (LISTENING filter)
REM  NOTE: keep this file ASCII only - cmd parses it as GBK
REM ============================================================
setlocal
for %%p in (8080 8081 9527) do call :kill %%p
echo done.
endlocal
goto :eof

:kill
for /f "tokens=5" %%a in ('netstat -ano ^| findstr /C:":%1 " ^| findstr LISTENING') do (
  taskkill /F /PID %%a >nul 2>&1
  if not errorlevel 1 echo   killed :%1 (pid %%a^)
)
goto :eof
