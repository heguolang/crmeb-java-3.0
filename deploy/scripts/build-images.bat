@echo off
setlocal
cd /d "%~dp0..\.."
set "TAG=%1"
if "%TAG%"=="" set "TAG=3.0.0"
set "JAVA_HOME=%JAVA_HOME%"
if "%JAVA_HOME%"=="" set "JAVA_HOME=D:\environment\jdk1.8"
set "MVN=D:\environment\apache-maven-3.9.5-bin\apache-maven-3.9.5\bin\mvn.cmd"
if not exist "%MVN%" set "MVN=mvn"

echo [1/4] Maven package admin + front ...
call "%MVN%" -f qianxu\pom.xml -pl qianxu-admin,qianxu-front -am -DskipTests package
if errorlevel 1 exit /b 1

echo [2/4] Docker image qianxu-admin:%TAG% ...
docker build -f deploy\docker\Dockerfile.admin -t qianxu-admin:%TAG% .
if errorlevel 1 exit /b 1

echo [3/4] Docker image qianxu-front:%TAG% ...
docker build -f deploy\docker\Dockerfile.front -t qianxu-front:%TAG% .
if errorlevel 1 exit /b 1

echo [4/4] Docker image qianxu-admin-web:%TAG% ...
docker build -f deploy\docker\Dockerfile.admin-web -t qianxu-admin-web:%TAG% .
if errorlevel 1 exit /b 1

echo.
echo OK. Images:
echo   qianxu-admin:%TAG%
echo   qianxu-front:%TAG%
echo   qianxu-admin-web:%TAG%
echo Next: cd deploy ^&^& copy .env.example .env ^&^& docker compose up -d
endlocal
