#!/bin/bash
# QIANXU Java 3.0 本地一键停止脚本（仅停止应用，保留 MySQL / Redis）
set -u

echo "==> 停止管理端前端"
pkill -f "vue-cli-service serve" 2>/dev/null && echo "    已停止" || echo "    未运行"

echo "==> 停止后台 API (8080)"
pkill -f "Qianxu-admin.jar" 2>/dev/null && echo "    已停止" || echo "    未运行"

echo "==> 停止前台 API (8081)"
pkill -f "Qianxu-front.jar" 2>/dev/null && echo "    已停止" || echo "    未运行"

echo ""
echo "说明：会员端 H5 由 HBuilderX 运行，请在其内置浏览器中停止，或退出 HBuilderX。"
echo ""
echo "MySQL(3306) 与 Redis(6379) 保持运行。如需停止："
echo "  MySQL: pkill -f 'mysqld --datadir=/opt/homebrew/var/mysql8'"
echo "  Redis: redis-cli -a 123456 shutdown nosave"
