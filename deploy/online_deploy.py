#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
QIANXU Java 3.0 —— 线上（宝塔 Linux）部署辅助脚本
=================================================
用 SSH 直连服务器完成：探测 -> 上传 -> 备份 -> 校验。
（**落位 + 重启不在本脚本内**，见下方 ⚠）

凭据一律走环境变量，不落盘：
    CRM_SSH_HOST    服务器 IP        默认 8.163.105.182
    CRM_SSH_PORT    端口            默认 22
    CRM_SSH_USER    用户            默认 root
    CRM_SSH_PASS    密码（二选一）
    CRM_SSH_KEY     私钥路径        默认 ~/.ssh/id_ed25519
    CRM_REDIS_PASS  Redis 密码      探针用；不设则跳过 keyspace 扫描
    CRM_DB_USER     数据库用户      默认 root
    CRM_DB_PASS     数据库密码      backup / sql 用；不设则跳过
    CRM_DB_NAME     库名            默认 qianxu_java3

用法：
    python online_deploy.py probe           # 只读探测，不改任何东西
    python online_deploy.py upload          # 上传产物到 /tmp/<staging>
    python online_deploy.py backup          # 备份 jar + 前端 + （可选）mysqldump
    python online_deploy.py sql    --yes    # 执行 02_patches_all.sql（先读它的警告）
    python online_deploy.py verify          # 部署后校验
    python online_deploy.py all             # 上述全套 + 打印落位步骤

    ⚠ apply（覆盖 jar + 前端 + 重启）**不在本脚本实现**，只打印步骤：
      线上停进程必须用**完整 jar 路径**匹配，否则会误杀服务器上并存的
      另一套 QIANXU（admin.xml168.cn）。这套动作风险高，按
      `qianxu-build-deploy` 技能「线上部署」章节的步骤逐条手工执行、
      每步核对输出，不要图省事一次性批量跑。

安全约定：
    * 本脚本**不做**任何 delete/rm 业务数据操作；
    * 覆盖前一律先备份到 /www/backup/qianxu_deploy_<ts>/；
    * 破坏性动作需要额外加 --yes 才真正执行。
"""

import os
import sys
import time
import posixpath
import paramiko

HOST = os.environ.get("CRM_SSH_HOST", "8.163.105.182")
PORT = int(os.environ.get("CRM_SSH_PORT", "22"))
USER = os.environ.get("CRM_SSH_USER", "root")
PASS = os.environ.get("CRM_SSH_PASS")
KEY = os.path.expanduser(os.environ.get("CRM_SSH_KEY", "~/.ssh/id_ed25519"))
REDIS_PASS = os.environ.get("CRM_REDIS_PASS")          # 探针用，不设则跳过
DB_USER = os.environ.get("CRM_DB_USER", "root")
DB_PASS = os.environ.get("CRM_DB_PASS")                # 执行 SQL 用
DB_NAME = os.environ.get("CRM_DB_NAME", "qianxu_java3")

ONLINE_ADMIN = "/www/wwwroot/api.qianxutec.com"        # admin/front jar 所在
ONLINE_WEB = "/www/wwwroot/admin.qianxutec.com"        # 后台前端 dist 所在

LOCAL = "D:/qianxu-java-3.0"
ART = {
    "admin_jar": LOCAL + "/qianxu/qianxu-admin/target/Qianxu-admin.jar",
    "front_jar": LOCAL + "/qianxu/qianxu-front/target/Qianxu-front.jar",
    "admin_dist": LOCAL + "/admin/dist_online",
    "sql": LOCAL + "/qianxu/sql/oneclick/02_patches_all.sql",
}

TS = time.strftime("%Y%m%d_%H%M%S")
STAGING = "/tmp/qianxu_deploy_" + TS
BACKUP = "/www/backup/qianxu_deploy_" + TS


def log(msg):
    print(msg, flush=True)


def connect():
    cli = paramiko.SSHClient()
    cli.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    kw = dict(hostname=HOST, port=PORT, username=USER, timeout=20,
              banner_timeout=25, auth_timeout=25)
    if PASS:
        kw["password"] = PASS
    else:
        kw["key_filename"] = KEY
        kw["look_for_keys"] = True
    cli.connect(**kw)
    return cli


def run(cli, cmd, quiet=False, timeout=300):
    """执行远端命令，返回 (exit_code, stdout, stderr)"""
    if not quiet:
        log("  $ " + cmd)
    stdin, stdout, stderr = cli.exec_command(cmd, timeout=timeout, get_pty=False)
    out = stdout.read().decode("utf-8", "replace")
    err = stderr.read().decode("utf-8", "replace")
    code = stdout.channel.recv_exit_status()
    if not quiet and out.strip():
        for line in out.rstrip().splitlines():
            log("    " + line)
    if err.strip():
        for line in err.rstrip().splitlines():
            log("    ! " + line)
    return code, out, err


def sftp_put_dir(sftp, local_dir, remote_dir):
    """递归上传目录"""
    made = set()
    count = 0
    for root, dirs, files in os.walk(local_dir):
        rel = os.path.relpath(root, local_dir).replace("\\", "/")
        rdir = remote_dir if rel == "." else posixpath.join(remote_dir, rel)
        if rdir not in made:
            try:
                sftp.mkdir(rdir)
            except IOError:
                pass
            made.add(rdir)
        for f in files:
            sftp.put(os.path.join(root, f), posixpath.join(rdir, f))
            count += 1
    return count


# ---------------------------------------------------------------- probe
def cmd_probe(cli):
    log("=" * 70)
    log("只读探测：%s" % HOST)
    log("=" * 70)

    log("\n[1] 主机信息")
    run(cli, "hostname; cat /etc/os-release 2>/dev/null | head -2; uptime | tr -s ' '")

    log("\n[2] Java 进程（关键：看 jar 路径 + 启动参数）")
    run(cli, "ps -ef | grep java | grep -v grep | cut -c1-260")

    log("\n[3] 服务管理方式（systemd / 宝塔 / 脚本）")
    run(cli, "systemctl list-units --type=service --no-pager 2>/dev/null | grep -iE 'qianxu|java' | head -10")
    run(cli, "ls -la /www/server/panel/vhost/ 2>/dev/null | head -5")
    run(cli, "ls -d /www/server/panel/plugin/*java* 2>/dev/null; ls /www/wwwroot 2>/dev/null")

    log("\n[4] 站点目录")
    run(cli, "ls -la /www/wwwroot/api.qianxutec.com/ 2>/dev/null | head -20")
    run(cli, "find /www/wwwroot -maxdepth 3 -name '*.jar' 2>/dev/null | head -10")
    run(cli, "find /www/wwwroot -maxdepth 4 -name 'index.html' 2>/dev/null | head -10")

    log("\n[5] nginx 站点根目录")
    run(cli, "nginx -T 2>/dev/null | grep -nE 'server_name|root ' | head -40 || "
             "grep -rnE 'server_name|root ' /www/server/panel/vhost/nginx/*.conf 2>/dev/null | head -40")

    log("\n[6] 图片资源")
    run(cli, "ls /www/wwwroot/api.qianxutec.com/qianxuimage/ 2>/dev/null | head; "
             "find /www/wwwroot/api.qianxutec.com/qianxuimage -type f 2>/dev/null | wc -l")

    log("\n[7] 磁盘与内存")
    run(cli, "df -h / /www 2>/dev/null; free -m | head -3")

    log("\n[8] 线上 Redis 实际情况（回答「为什么 db8 没有 config_list」）")
    if not REDIS_PASS:
        log("  （未设 CRM_REDIS_PASS，跳过 keyspace 扫描）")
    else:
        rc = "/www/server/redis/src/redis-cli"
        auth = "-a \"$CRM_REDIS_PASS\" --no-auth-warning"
        run(cli, "CRM_REDIS_PASS='%s' %s %s INFO keyspace 2>/dev/null" % (REDIS_PASS, rc, auth))
        run(cli, "for i in $(seq 0 15); do "
                 "n=$(CRM_REDIS_PASS='%s' %s %s -n $i HLEN config_list 2>/dev/null); "
                 "[ -n \"$n\" ] && [ \"$n\" != \"0\" ] && echo \"  db$i config_list 有 $n 个字段\"; done; "
                 "echo '  (扫描完毕)'" % (REDIS_PASS, rc, auth))

    log("\n[9] 数据库")
    run(cli, "which mysql; mysql -uroot -p'$(grep -m1 -oP \"(?<=password=).*\" /www/server/panel/config/mysql.json 2>/dev/null)' -e 'select 1' 2>/dev/null | head -2")

    log("\n[10] 当前部署版本线索（jar 时间戳）")
    run(cli, "find /www/wwwroot /www/server /opt /home -maxdepth 4 -name 'Qianxu*.jar' -exec ls -la {} \\; 2>/dev/null | head -10")

    log("\n探测完成，未改动任何东西。")


# ---------------------------------------------------------------- backup
def cmd_backup(cli, yes):
    """备份 jar + 后台前端 + （可选）数据库。纯读操作，不需要 --yes。"""
    log("\n备份到 %s" % BACKUP)
    run(cli, "mkdir -p %s/jar %s/dist" % (BACKUP, BACKUP))

    log("\n[1] 备份 jar")
    for j in ("Qianxu-admin.jar", "Qianxu-front.jar"):
        run(cli, "cp -p %s/%s %s/jar/ 2>/dev/null && echo '  已备份 %s' || echo '  跳过 %s（不存在）'"
                 % (ONLINE_ADMIN, j, BACKUP, j, j))

    log("\n[2] 备份后台前端 dist")
    run(cli, "cd %s && cp -rp index.html favicon.ico dist.zip %s/dist/ 2>/dev/null; "
             "cp -rp static %s/dist/ 2>/dev/null; "
             "echo '  dist 备份文件数:' $(find %s/dist -type f 2>/dev/null | wc -l)"
             % (ONLINE_WEB, BACKUP, BACKUP, BACKUP))

    log("\n[3] 备份数据库")
    if not DB_PASS:
        log("  （未设 CRM_DB_PASS，跳过 mysqldump）")
    else:
        run(cli, "/www/server/mysql/bin/mysqldump -u%s -p%s --single-transaction --routines "
                 "--triggers --events %s 2>/dev/null | gzip > %s/qianxu_%s.sql.gz && "
                 "ls -la %s/qianxu_%s.sql.gz"
                 % (DB_USER, DB_PASS, DB_NAME, BACKUP, DB_NAME, BACKUP, DB_NAME))

    run(cli, "du -sh %s" % BACKUP)
    log("\n备份完成：%s" % BACKUP)


# ---------------------------------------------------------------- upload
def cmd_upload(cli):
    log("\n检查本地产物")
    for k in ("admin_jar", "front_jar"):
        p = ART[k]
        if not os.path.exists(p):
            log("  ✗ 缺少 %s ：%s" % (k, p))
            return False
        log("  ✓ %-10s %8.1f MB  %s" % (k, os.path.getsize(p) / 1048576.0, p))
    if not os.path.isdir(ART["admin_dist"]):
        log("  ✗ 缺少 admin_dist：%s（先跑 npm 构建）" % ART["admin_dist"])
        return False
    idx = os.path.join(ART["admin_dist"], "index.html")
    log("  %s admin_dist index.html %s" % ("✓" if os.path.exists(idx) else "✗",
                                          "存在" if os.path.exists(idx) else "缺失（产物不完整！）"))
    if not os.path.exists(idx):
        return False

    run(cli, "mkdir -p %s" % STAGING)
    sftp = cli.open_sftp()
    try:
        log("\n上传 jar ...")
        for k in ("admin_jar", "front_jar"):
            sftp.put(ART[k], posixpath.join(STAGING, os.path.basename(ART[k])))
            log("  ✓ %s" % os.path.basename(ART[k]))
        log("\n上传后台前端 ...")
        n = sftp_put_dir(sftp, ART["admin_dist"], posixpath.join(STAGING, "admin_dist"))
        log("  ✓ %d 个文件" % n)
        log("\n上传 SQL ...")
        sftp.put(ART["sql"], posixpath.join(STAGING, "02_patches_all.sql"))
        log("  ✓ 02_patches_all.sql")
    finally:
        sftp.close()
    run(cli, "ls -la %s" % STAGING)
    log("\n上传完成：%s" % STAGING)
    return True


# ---------------------------------------------------------------- sql
def cmd_sql(cli, yes):
    sql_remote = posixpath.join(STAGING, "02_patches_all.sql")
    log("\n在线上库执行增量补丁")
    log("")
    log("  ⚠⚠ 停手看一眼：02_patches_all.sql **不要整包跑**。")
    log("     其中两块会强制覆盖线上配置：")
    log("       update_http_domain  -> api_url / localUploadUrl / site_url")
    log("       hidden_super_admin  -> sys_switch_* 三个模块开关")
    log("     线上若手工调过这些，整包跑会把它们打回原形。")
    log("     正确做法：先从 SQL 里抽出全部表名 + 列名，逐个查线上 information_schema")
    log("     算差集，只补真正缺的那几列（见 qianxu-build-deploy 技能「线上部署」章节）。")
    log("")
    code, out, _ = run(cli, "test -f %s && echo OK" % sql_remote, quiet=True)
    if "OK" not in out:
        log("  ✗ 先执行 upload")
        return
    if not yes:
        log("  （未加 --yes，仅预演，未执行）")
        return
    if not DB_PASS:
        log("  ✗ 未设 CRM_DB_PASS，无法执行")
        return
    run(cli, "/www/server/mysql/bin/mysql -u%s -p%s %s < %s"
             % (DB_USER, DB_PASS, DB_NAME, sql_remote))


# ---------------------------------------------------------------- apply
def cmd_apply(cli, yes):
    """已停用。落位 + 重启风险高，必须逐步手工执行并核对输出。"""
    log("\n落位（覆盖 jar 与后台前端）+ 重启 —— 本脚本不代劳")
    log("")
    log("  原因：服务器上**并存放着两套 QIANXU**（本项目 api.qianxutec.com 与")
    log("        admin.xml168.cn）。停进程若只按 jar 名匹配，会连别人那套一起杀掉。")
    log("        必须用**完整路径**匹配：grep \"$D/Qianxu-admin.jar\"。这类动作不适合")
    log("        放进脚本一键跑，请按 qianxu-build-deploy 技能「线上部署」章节逐步执行。")
    log("")
    log("  参照参数（本次实测）：")
    log("    站点目录 D = %s" % ONLINE_ADMIN)
    log("    后台前端 W = %s" % ONLINE_WEB)
    log("    暂存目录   = %s" % STAGING)
    log("    备份目录   = %s" % BACKUP)
    log("    启动方式   = setsid nohup java -jar <jar> --spring.profiles.active=prod")
    log("    admin 端口 = 21700    front 端口 = 21710")


# ---------------------------------------------------------------- verify
def cmd_verify(cli):
    log("\n部署后校验")
    run(cli, "curl -s -o /dev/null -w '  api/front/index  HTTP %%{http_code}\\n' "
             "http://127.0.0.1:21710/api/front/index")
    run(cli, "curl -s -o /dev/null -w '  api/admin 登录页  HTTP %%{http_code}\\n' "
             "http://127.0.0.1:21700/api/admin/login/pic")
    run(cli, "curl -s http://127.0.0.1:21710/api/front/index | grep -o '\"logoUrl\":\"[^\"]*\"' | head -1")


def main():
    mode = sys.argv[1] if len(sys.argv) > 1 else "probe"
    yes = "--yes" in sys.argv
    cli = connect()
    try:
        if mode == "probe":
            cmd_probe(cli)
        elif mode == "upload":
            cmd_upload(cli)
        elif mode == "backup":
            cmd_backup(cli, yes)
        elif mode == "sql":
            cmd_sql(cli, yes)
        elif mode == "apply":
            cmd_apply(cli, yes)
        elif mode == "verify":
            cmd_verify(cli)
        elif mode == "all":
            cmd_probe(cli)
            if not cmd_upload(cli):
                log("\n产物不全，已中止。")
                return
            cmd_backup(cli, yes)
            cmd_sql(cli, yes)
            cmd_apply(cli, yes)      # 只打印手工步骤，不会自动覆盖
            cmd_verify(cli)
        else:
            log("未知模式：%s（可选：probe / upload / backup / sql / apply / verify / all）" % mode)
    finally:
        cli.close()


if __name__ == "__main__":
    main()
