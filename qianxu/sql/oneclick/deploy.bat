@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

REM ============================================================
REM QIANXU Java 3.0 一键导入数据库
REM 用法：
REM   1. 改下面 MYSQL_* / DB_NAME 为你的环境
REM   2. 双击或命令行运行本脚本
REM 模式：
REM   MODE=all     全新安装（基库 + 补丁）【默认】
REM   MODE=patch   仅补丁（已有库升级用）
REM   MODE=full    基库 + 补丁 + 演示业务数据（可选，体积大）
REM ============================================================

REM ------ 请按服务器修改 ------
set MYSQL_BIN=mysql
set MYSQL_HOST=127.0.0.1
set MYSQL_PORT=3306
set MYSQL_USER=qianxu_java3
set MYSQL_PWD=ktXMTiAxTTyMyPKk
set DB_NAME=qianxu_java3
set MODE=all
REM ----------------------------

set SCRIPT_DIR=%~dp0
set SQL_DIR=%SCRIPT_DIR%..
set ROOT_DIR=%SCRIPT_DIR%..\..\..
set BASE_SQL=%SQL_DIR%\Qianxu_v3.0.sql
set PATCH_SQL=%SCRIPT_DIR%02_patches_all.sql
set FULL_DATA=%ROOT_DIR%\db-data\qianxu_full_data_export.sql

set MYSQL_CMD=%MYSQL_BIN% -h%MYSQL_HOST% -P%MYSQL_PORT% -u%MYSQL_USER% -p%MYSQL_PWD% --default-character-set=utf8mb4

echo.
echo [QIANXU] host=%MYSQL_HOST% db=%DB_NAME% mode=%MODE%
echo.

if /I "%MODE%"=="patch" goto PATCH_ONLY

echo [1/3] 创建数据库（若不存在）...
%MYSQL_CMD% -e "CREATE DATABASE IF NOT EXISTS `%DB_NAME%` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
if errorlevel 1 goto FAIL

echo [2/3] 导入基库 Qianxu_v3.0.sql （约几分钟，请勿中断）...
if not exist "%BASE_SQL%" (
  echo 找不到基库文件: %BASE_SQL%
  goto FAIL
)
%MYSQL_CMD% %DB_NAME% < "%BASE_SQL%"
if errorlevel 1 goto FAIL

:PATCH_ONLY
echo [补丁] 导入 02_patches_all.sql ...
if not exist "%PATCH_SQL%" (
  echo 找不到补丁文件: %PATCH_SQL%
  goto FAIL
)
%MYSQL_CMD% %DB_NAME% < "%PATCH_SQL%"
if errorlevel 1 goto FAIL

if /I "%MODE%"=="full" (
  echo [可选] 导入演示业务数据 qianxu_full_data_export.sql ...
  if exist "%FULL_DATA%" (
    %MYSQL_CMD% %DB_NAME% < "%FULL_DATA%"
    if errorlevel 1 goto FAIL
  ) else (
    echo 警告: 未找到 %FULL_DATA%，已跳过
  )
)

echo.
echo ========== 全部完成 ==========
echo 域名已设为: http://api.qianxutec.com
echo 请重启 Qianxu-admin / Qianxu-front，并清 Redis 配置缓存（如有）
echo.
pause
exit /b 0

:FAIL
echo.
echo ========== 执行失败，请检查账号/权限/日志 ==========
pause
exit /b 1
