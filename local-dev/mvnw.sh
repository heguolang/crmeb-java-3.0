#!/usr/bin/env bash
# QIANXU 本地 Maven 包装脚本（MSYS/Git-Bash 下 mvn 脚本会因路径转换失败，故直调 classworlds）
# 用法: bash local-dev/mvnw.sh -DskipTests -pl qianxu-common,qianxu-admin -am clean package
export PATH="/usr/bin:/bin:/c/Windows/System32:$PATH"
export MSYS_NO_PATHCONV=1
JAVA_EXE="D:/env/java/jdk8u504-b01/bin/java.exe"
MVN_HOME="D:\\env\\apache-maven-3.9.16"
PROJ_DIR="D:\\qianxu-java-3.0\\qianxu"
exec "$JAVA_EXE" \
  -classpath "$MVN_HOME\\boot\\plexus-classworlds-2.11.0.jar" \
  "-Dclassworlds.conf=$MVN_HOME\\bin\\m2.conf" \
  "-Dmaven.home=$MVN_HOME" \
  "-Dmaven.repo.local=D:\\env\\maven-repo" \
  "-Dmaven.multiModuleProjectDirectory=$PROJ_DIR" \
  org.codehaus.plexus.classworlds.launcher.Launcher "$@"
