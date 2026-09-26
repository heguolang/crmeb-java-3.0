@echo off
title qianxu-admin-web-9527
set "NODE_DIR=C:\Users\Administrator\.workbuddy\binaries\node\versions\22.22.2-3"
set "PATH=%NODE_DIR%;%PATH%"
cd /d "D:\qianxu-java-3.0\admin"
if not exist "D:\qianxu-java-3.0\local-dev\logs" mkdir "D:\qianxu-java-3.0\local-dev\logs"
start "qianxu-admin-web" /B "%NODE_DIR%\node.exe" "node_modules\@vue\cli-service\bin\vue-cli-service.js" serve > "D:\qianxu-java-3.0\local-dev\logs\admin-web.log" 2>&1
