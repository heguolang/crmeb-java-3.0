# 黔序商城 · 一客户一套 Docker 交付

适合：**很多客户各部署一套**。同一镜像版本，每客户独立目录、独立库、独立 Redis、独立配置。

## 架构

```
浏览器 ──► Nginx(:80)
              ├─ /              后台 Vue
              ├─ /api/          Qianxu-admin.jar(:8080)
              ├─ /fapi/         Qianxu-front.jar(:8081)
              └─ /qianxuimage/  上传文件卷
                    │
         MySQL(客户库)   Redis(客户库号)
```

| 组件 | 镜像 | 说明 |
|------|------|------|
| admin | `qianxu-admin:TAG` | 后台 API + 定时任务（每套只跑一份） |
| front | `qianxu-front:TAG` | 会员端 API |
| nginx | `qianxu-admin-web:TAG` | 后台前端 + 反代 |
| mysql / redis | 官方镜像 | 数据在 `deploy/data/` |

## 快速开始（单客户）

### 1. 构建镜像（开发机 / CI）

Windows:

```bat
deploy\scripts\build-images.bat 3.0.0
```

Linux/macOS:

```bash
chmod +x deploy/scripts/build-images.sh
./deploy/scripts/build-images.sh 3.0.0
```

需已安装：JDK8、Maven、Docker、Node（构建 admin-web 时用）。

### 2. 准备客户环境

```bat
cd deploy
copy .env.example .env
```

编辑 `.env`：改密码、端口、`IMAGE_TAG=3.0.0`、`COMPOSE_PROJECT_NAME`。

把初始化 SQL 放到 `sql/init/`（见该目录 README）。**仅空库首次启动会自动导入。**

### 3. 启动

```bat
docker compose up -d
docker compose ps
docker compose logs -f admin
```

访问：`http://服务器IP:HTTP_PORT`  
默认管理员以你导入的库为准（常见 `admin` / `123456`，生产务必改密）。

会员 API 基址示例：`http://服务器IP/fapi`（再拼 `/api/front/...`）。

### 4. 升级（全客户同一版本）

```bat
deploy\scripts\build-images.bat 3.0.1
```

每个客户目录：

```bat
REM 改 .env 里 IMAGE_TAG=3.0.1
docker compose pull
docker compose up -d
REM 再按需执行 migration SQL
```

## 新开一个客户

```bat
deploy\scripts\new-customer.bat acme
```

会生成 `customers/acme/`（compose + .env + nginx）。  
同机多客户时务必改：

- `COMPOSE_PROJECT_NAME`
- `HTTP_PORT` / `MYSQL_PORT` / `REDIS_PORT`（避免冲突）
- `REDIS_DB_*`（若共用一台 Redis，库号错开）
- 密码全部换新

重要客户建议 **独立 MySQL 实例或独立机器**，不要只靠端口隔离。

## 配置说明

应用通过环境变量覆盖 JAR 内配置（无需改包）：

| 变量 | 作用 |
|------|------|
| `SPRING_DATASOURCE_*` | 数据库 |
| `SPRING_REDIS_*` | Redis |
| `QIANXU_IMAGEPATH` | 上传根目录（默认 `/data/qianxu/`） |
| `QIANXU_DEMOSITE` | 是否演示站掩码手机号 |
| `JAVA_OPTS_*` | 堆内存等 |

保留天数等业务配置仍在库表 `eb_system_config` / 后台「设置」。

## 运维建议

1. **备份**：定时 `mysqldump` + 备份 `data/qianxu`  
2. **日志**：`data/logs/`；非业务日志归档任务见 `ArchiveLogTask`  
3. **定时任务**：admin 容器每客户只起 1 个，勿水平扩多实例抢 Quartz  
4. **HTTPS**：前面再挂一层主机 Nginx/Caddy 做证书，反代到本 compose 的 `HTTP_PORT`  
5. **不要**把 `.env`、客户数据提交进 Git  

## 目录结构

```
deploy/
  docker-compose.yml
  .env.example
  docker/           Dockerfile + nginx
  scripts/          构建、新客户
  sql/init/         首启 SQL
  data/             运行时数据（gitignore）
customers/          new-customer 生成的客户目录（可选）
```
