@echo off
title qianxu-admin-web-9527
set "NODE_DIR=C:\Users\Administrator\.workbuddy\binaries\node\versions\22.22.2-3"
set "PATH=%NODE_DIR%;%PATH%"
cd /d "D:\qianxu-java-3.0\admin"
if not exist "D:\qianxu-java-3.0\local-dev\logs" mkdir "D:\qianxu-java-3.0\local-dev\logs"
rem 2026-09-28: 去掉 /B —— start /B 共享控制台，本 bat 一退出 node 就被连带杀掉；
rem 改为独立控制台窗口运行（窗口保留可看 webpack 日志，关窗=停服务）。
rem ⚠️ 不能把 --openssl-legacy-provider 放进 NODE_OPTIONS（node 会拒绝并秒退），只能用命令行标志。
start "qianxu-admin-web" "%NODE_DIR%\node.exe" --openssl-legacy-provider "node_modules\@vue\cli-service\bin\vue-cli-service.js" serve --port 9527 >> "D:\qianxu-java-3.0\local-dev\logs\admin-web.log" 2>&1
echo Admin Web 已在新窗口启动（端口 9527），本窗口可关闭。
