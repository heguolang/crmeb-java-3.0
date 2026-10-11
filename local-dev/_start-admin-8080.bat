@echo off
title qianxu-admin-8080
cd /d "D:\WorkBuddy\qianxu-java5.0\qianxu\qianxu-admin\target"
"D:\env\java\jdk8u504-b01\bin\java.exe" -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar "D:\WorkBuddy\qianxu-java5.0\qianxu\qianxu-admin\target\Qianxu-admin.jar" --server.port=8080 --qianxu.demoSite=false > "D:\WorkBuddy\qianxu-java5.0\local-dev\logs\qianxu-admin.log" 2>&1
echo.
echo [qianxu-admin] process exited. Press any key to close.
pause >nul
