# qianxuqifu-182 —— 黔序自用副库 · 182 部署资产

`qianxuqifu`（黔序商城 Java 3.0 自用副库）在 **8.163.105.182** 上的部署资产归档，
与该服务器上正在运行的文件保持一致（2026-10-02）。

---

## ⛔ 变更纪律

**任何要落到线上的操作，都必须先征得同意**：改 nginx / reload、启停 Java 服务、
改数据库、清 Redis、上传 jar 或前端产物。纯只读排查不受限。

改配置的正确顺序：**先改这里的文件 → 确认 → 上传 → 验证**。

---

## 目录内容

| 文件 | 线上对应路径 |
|---|---|
| `qifu-service.sh` | `/www/wwwroot/apiqf.qianxutec.com/qifu-service.sh` |
| `nginx/apiqf.qianxutec.com.conf` | `/www/server/panel/vhost/nginx/` |
| `nginx/adminqf.qianxutec.com.conf` | 同上 |
| `nginx/appqf.qianxutec.com.conf` | 同上 |
| `local-service.sh` | （仅本地 macOS，admin 18080 / front 18081） |
| `线上部署说明-182.md` | 完整部署过程与全部坑位 |
| `部署说明.md` | 本地 macOS 环境部署说明 |

---

## ⚠️ 口令已脱敏

`qifu-service.sh` 里的 MySQL 口令已替换成 `__SET_MYSQL_PASSWORD__` 占位符，
**部署前必须替换**（或直接从线上机器取真身）。

真实口令只存在于两处：

- 线上：`/www/wwwroot/apiqf.qianxutec.com/qifu-service.sh`（`chmod 700 root`）
- 本地工作区：`deploy-182/qifu-service.sh`（**不入库、不同步网盘**）

> `local-service.sh` 里的 `root/root` 是本地开发库默认口令，未脱敏。

---

## 关键配置（改之前先看）

### Redis：admin 与 front 必须共用同一个库

| | 主库 | 副库（微信 accessToken） |
|---|---|---|
| 线上 182 | **db12**（admin+front 共用） | **db14** |
| 本地 mac | **db12**（共用） | **db9** |

**为什么必须共用**：prod profile 下 front 的 `asyncConfig=true`（读 Redis 缓存）、
admin 的 `false`（直查库），靠「后台保存时强制刷 Redis」来补
「会员端缓存不更新」这个洞。**两端分成两个库，补洞就被切断** ——
表现为后台改配置、会员端永远读旧值，而且**不报任何错**。
（2026-10-02 踩过，详见 `线上部署说明-182.md` 第六节。）

### 服务脚本

- 端口：admin **21800** / front **21810**
- **只按端口取 PID**，绝不用 `pkill -f`（同机存在同名 jar，会误杀别人的服务）
- 限堆 512m：同机还有主项目 1g+1g、xml168 768m+768m，
  且主项目带 `-XX:+ExitOnOutOfMemoryError`，内存吃满会把它直接打挂
- ⚠️ 起进程必须「脱离会话」：Linux 用 `setsid`；
  macOS 没有该命令时借 python `subprocess.Popen(..., start_new_session=True)`。
  直接 `nohup ... &` 在 Agent / CI 这类非交互环境里会被整组回收 ——
  现象是「日志里明明有 Started，进程却没了」。

### nginx

- `location ^~ /.well-known/acme-challenge/` —— **必须带 `^~`**，
  否则会被 `location ~ ^/api/...` 正则抢先命中，把 ACME 校验反代给 Java，续期必然失败
- 301 跳转必须限定 `if ($scheme = http)`：宝塔风格 vhost 把 `listen 80` 与 `listen 443 ssl`
  写在**同一个 server 块**，server 级 `if` 对两个端口都生效，不加条件会让 https 自己跳自己
- 301 必须放行 ACME 路径，否则证书过期后无法续期（死锁）
