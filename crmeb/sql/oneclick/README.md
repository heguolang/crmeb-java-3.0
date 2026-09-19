# CRMEB SQL 一键部署

目录：`crmeb/sql/oneclick/`

| 文件 | 作用 |
|------|------|
| `deploy.bat` / `deploy.sh` | 一键执行入口 |
| `02_patches_all.sql` | **所有业务补丁合并版**（团队/代理/订货一~三期/换货/虚拟库存/线下销售/门店/隐藏运维面板/本地设置快照/HTTP 域名等，2026-09-19 更新） |
| `00_create_database.sql` | 仅建库（脚本里已自动建库，一般不用单独跑） |

基库仍用：`crmeb/sql/Crmeb_v3.0.sql`（约 3.4MB，不并入补丁文件）。

---

## 你当前测试/生产库（已有数据）——只跑补丁

Linux / 宝塔：

```bash
cd /www/wwwroot/api.qianxutec.com   # 或你放代码的位置
# 把 oneclick 目录和 Crmeb_v3.0.sql 上传到服务器后：
cd crmeb/sql/oneclick
chmod +x deploy.sh
# 改 deploy.sh 顶部账号密码后：
./deploy.sh patch
```

或直接：

```bash
mysql -uroot -p你的密码 --default-character-set=utf8mb4 crmeb_java3 < 02_patches_all.sql
```

---

## 全新空库

```bash
./deploy.sh all          # 基库 + 补丁
# 或
./deploy.sh full         # 再加演示业务数据（db-data，可选）
```

Windows 改 `deploy.bat` 顶部配置后双击，或：

```bat
deploy.bat
```

默认 `MODE=all`；只要补丁时把 bat 里 `set MODE=patch`。

---

## 默认连接（与 application-prod.yml 一致）

- 库名：`crmeb_java3`
- 用户：`crmeb_java3`
- 域名补丁末尾写入：`http://api.qianxutec.com`

执行完请 **重启 jar**，若开了 Redis 配置缓存请清一下。

---

## 刻意未合并的脚本

| 脚本 | 原因 |
|------|------|
| `export_admin_settings.sql` | 会写成 `127.0.0.1` |
| `update_deploy_config.sql` | 本地开发域名 |
| `reset_default_category.sql` | 会清空分类，生产危险 |
| `sql/migration/*` | 旧版，已被根目录幂等补丁覆盖 |

原分散的单个 `.sql` 仍保留在 `crmeb/sql/`，可单独查阅；日常部署用本目录即可。
