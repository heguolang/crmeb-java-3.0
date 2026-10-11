#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
黔序商城 mac 本地开发 —— 服务守护化启动器

背景：WorkBuddy 会话回收时会杀掉整组后台任务，挂在其下的 java/node 长进程会一起陪葬。
做法：双 fork + setsid 让子进程彻底脱离会话进程组，父进程立即返回，
      于是启动命令本身"秒退"，服务不再挂在任何任务之下。

用法：
    python3 local-dev/detach-run.py <日志文件> <命令> [参数...]
示例：
    python3 local-dev/detach-run.py local-dev/logs/qianxu-admin.log \
        "$JAVA_HOME/bin/java" -jar qianxu/qianxu-admin/target/Qianxu-admin.jar --server.port=8080
    # 需要切换工作目录时用 sh -c 包一层（vue-cli-service 必须在 admin/ 下跑）
    python3 local-dev/detach-run.py local-dev/logs/admin-web.log \
        /bin/sh -c "cd admin && exec node ./node_modules/.bin/vue-cli-service serve --port 9527"

停止服务：lsof -ti tcp:<端口> 取 PID 再 kill（本机 ps 在沙箱里看不到进程）。
"""
import os
import sys


def main():
    if len(sys.argv) < 3:
        sys.stderr.write(__doc__)
        return 2

    log_path = sys.argv[1]
    cmd = sys.argv[2:]

    log_dir = os.path.dirname(os.path.abspath(log_path))
    if log_dir:
        os.makedirs(log_dir, exist_ok=True)

    # 第一次 fork：父进程直接返回，让调用方"秒退"
    if os.fork() > 0:
        return 0

    os.setsid()  # 脱离控制终端，成为新会话组长

    # 第二次 fork：确保不是会话组长，彻底脱离（连 SIGHUP 都收不到）
    if os.fork() > 0:
        os._exit(0)

    # 重定向标准流到日志
    fd = os.open(log_path, os.O_WRONLY | os.O_CREAT | os.O_APPEND, 0o644)
    devnull = os.open(os.devnull, os.O_RDONLY)
    os.dup2(devnull, 0)
    os.dup2(fd, 1)
    os.dup2(fd, 2)
    if fd > 2:
        os.close(fd)
    if devnull > 2:
        os.close(devnull)

    try:
        os.execvp(cmd[0], cmd)
    except Exception as exc:  # noqa: BLE001
        sys.stderr.write("detach-run 启动失败: %s\n" % exc)
        os._exit(127)


if __name__ == "__main__":
    sys.exit(main())
