# 服务器试跑步骤（推荐：库在 Docker 内网，不对外暴露）

适合：先在一台测试机验证通不通。默认 **只开放 HTTP_PORT**，MySQL/Redis 不映射到公网。

---

## 0. 机器要求

- Linux（Ubuntu 20.04+ 等），内存建议 ≥ 4G  
- 已装 Docker + Compose  
- 安全组/防火墙放行：`HTTP_PORT`（默认 80）

```bash
docker --version
docker compose version
```

---

## 1. 拉代码

```bash
cd /opt
git clone https://github.com/heguolang/crmeb-java-3.0.git
cd crmeb-java-3.0
git pull
```

---

## 2. 打镜像

### 2A 服务器上构建（需 JDK8 + Maven + Node）

```bash
cd /opt/crmeb-java-3.0
chmod +x deploy/scripts/build-images.sh
./deploy/scripts/build-images.sh 3.0.0
```

### 2B 本机已有 JAR，在能跑 Docker 的机器构建后传到服务器

```bash
cd /path/to/crmeb_java-3.0   # 确保 target 下已有两个 jar
TAG=3.0.0
docker build -f deploy/docker/Dockerfile.admin -t qianxu-admin:$TAG .
docker build -f deploy/docker/Dockerfile.front -t qianxu-front:$TAG .
docker build -f deploy/docker/Dockerfile.admin-web -t qianxu-admin-web:$TAG .

docker save qianxu-admin:$TAG qianxu-front:$TAG qianxu-admin-web:$TAG | gzip > /tmp/qianxu-$TAG.tar.gz
# scp 到服务器后：
gunzip -c qianxu-3.0.0.tar.gz | docker load
```

确认：

```bash
docker images | grep qianxu
```

---

## 3. 配置客户环境

```bash
cd /opt/crmeb-java-3.0/deploy
cp .env.example .env
vi .env
```

至少改：

- `COMPOSE_PROJECT_NAME=qianxu_trial`
- `IMAGE_TAG=3.0.0`
- `HTTP_PORT=80`（若 80 被占用可改 `8088`）
- `MYSQL_ROOT_PASSWORD` / `MYSQL_PASSWORD` / `REDIS_PASSWORD`（换成自己的强密码）

---

## 4. 放初始化 SQL（首次空库会自动执行）

```bash
mkdir -p sql/init data/qianxu/qianxuimage
# 把你们完整初始化 SQL 拷进来，文件名按顺序，例如：
# cp /path/ALL_IN_ONE.sql sql/init/01_all_in_one.sql
```

没有 SQL 时先起库，再按第 6 步手动导入。

---

## 5. 启动（库不对外）

```bash
cd /opt/crmeb-java-3.0/deploy
docker compose up -d
docker compose ps
docker compose logs -f admin
```

看到启动成功后 Ctrl+C。  
此时宿主机 **没有** 3306/6379 监听（更安全）；应用在容器网内访问 `mysql` / `redis`。

---

## 6. 若库是空的，手动导入

```bash
cd /opt/crmeb-java-3.0/deploy
docker compose exec -T mysql mysql -uqianxu -p'你的MYSQL_PASSWORD' qianxu < ./sql/init/01_all_in_one.sql
docker compose restart admin front
```

进库排查（不映射端口时用 exec）：

```bash
docker compose exec mysql mysql -uqianxu -p'你的MYSQL_PASSWORD' qianxu -e "SHOW TABLES;"
```

---

## 7. 验收

```bash
# HTTP_PORT=80 时
curl -I http://127.0.0.1/
# 或
curl -I http://127.0.0.1:8088/
```

浏览器：`http://服务器IP` 或 `http://IP:端口`  
后台账号以导入库为准（常见 admin / 123456，测完改密）。

会员 API：`http://IP/fapi`（如 `/fapi/api/front/...`）。

确认库未对公网开放：

```bash
ss -lntp | grep -E '3306|6379' || echo "OK: 宿主机未监听 3306/6379"
```

---

## 8. 常用命令

```bash
cd /opt/crmeb-java-3.0/deploy
docker compose ps
docker compose logs -f admin
docker compose restart admin front
docker compose down          # 停服务，数据仍在 data/
```

---

## 附录

### 临时要在宿主机连 MySQL（仅本机）

```bash
docker compose -f docker-compose.yml -f docker-compose.ports.yml up -d
# 映射为 127.0.0.1:3306，不要对 0.0.0.0 开放
```

### 改用云数据库 RDS（可选）

在 `.env` 增加（并仍可保留 compose 里的 redis，或一并外置）：

```env
SPRING_DATASOURCE_URL=jdbc:mysql://rds地址:3306/qianxu?characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=qianxu
SPRING_DATASOURCE_PASSWORD=xxx
```

然后 `docker compose up -d`；若完全不用容器 MySQL，可再单独停掉 mysql 服务（进阶用法）。
