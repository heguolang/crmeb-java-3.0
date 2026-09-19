@echo off
title crmeb-admin-web-9527
set "NODE_DIR=C:\Users\Administrator\.workbuddy\binaries\node\versions\22.22.2-3"
set "PATH=%NODE_DIR%;%PATH%"
cd /d "D:\crmeb-java-3.0\admin"
if not exist "D:\crmeb-java-3.0\local-dev\logs" mkdir "D:\crmeb-java-3.0\local-dev\logs"
start "crmeb-admin-web" /B "%NODE_DIR%\node.exe" "node_modules\@vue\cli-service\bin\vue-cli-service.js" serve > "D:\crmeb-java-3.0\local-dev\logs\admin-web.log" 2>&1
