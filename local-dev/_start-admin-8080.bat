@echo off
title crmeb-admin-8080
cd /d "D:\crmeb-java-3.0\crmeb\crmeb-admin\target"
"D:\env\java\jdk8u504-b01\bin\java.exe" -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar "D:\crmeb-java-3.0\crmeb\crmeb-admin\target\Crmeb-admin.jar" --server.port=8080 --crmeb.demoSite=false > "D:\crmeb-java-3.0\local-dev\logs\crmeb-admin.log" 2>&1
echo.
echo [crmeb-admin] process exited. Press any key to close.
pause >nul
