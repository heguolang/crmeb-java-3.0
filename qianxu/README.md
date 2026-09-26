# 黔序商城后端（qianxu-java5.0）

贵州黔序科技有限公司 · 软著登记号 2025SR2146980

## 环境
1. Java JDK 1.8
2. Redis 5+
3. MySQL 5.7+（推荐 8.0）

## 模块
- `qianxu-common` 公共模型与工具
- `qianxu-service` 业务服务层
- `qianxu-admin` 平台管理端接口（默认端口 20000）
- `qianxu-front` 移动端接口（默认端口 20001）

## 初始化
1. 导入 `sql/qianxu-V5.0.sql`（含结构 + 初始数据）
2. 修改 `qianxu-admin/qianxu-front` 的 `application-prod.yml` 数据库/Redis 连接
3. `mvn -DskipTests package` 打包后运行各模块 jar

## 部署
1. jar 上传到 web 目录（宝塔域名指向目录）
2. 同级目录执行 `./start.sh`，看到 `Completed 200 OK` 即启动成功
3. 反向代理地址：`http://127.0.0.1:20000`（admin）/ `http://127.0.0.1:20001`（front）
4. 端口 20000 不可被 web 服务占用

## 版权
本项目为黔序科技商业软件，未经许可不得去除版权声明。
