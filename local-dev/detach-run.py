#!/usr/bin/env python3
"""detach-run.py —— 在完全脱离当前会话/进程组的方式下启动一个长驻进程。

原理：双 fork + setsid，子进程 stdio 重定向到日志文件，父进程立即退出。
这样 WorkBuddy 会话回收后台任务时杀不到它（没有挂在任务进程组下的活进程）。

用法：
    python3 detach-run.py <日志文件> <命令...>
"""
import os
import sys


def daemonize(log_path, cmd):
    # 第一次 fork
    pid = os.fork()
    if pid > 0:
        # 父进程等第一子进程退出后再退，避免僵尸；真正的服务进程由第二子进程承担
        os.waitpid(pid, 0)
        return

    os.setsid()  # 脱离原会话/进程组

    # 第二次 fork，确保不再是会话首进程（拿不到控制终端，也不会被会话级信号波及）
    pid = os.fork()
    if pid > 0:
        os._exit(0)

    # 重定向 stdio
    log_fd = os.open(log_path, os.O_WRONLY | os.O_CREAT | os.O_APPEND, 0o644)
    devnull = os.open(os.devnull, os.O_RDONLY)
    os.dup2(devnull, 0)
    os.dup2(log_fd, 1)
    os.dup2(log_fd, 2)

    os.execvp(cmd[0], cmd)


def main():
    if len(sys.argv) < 3:
        print("usage: detach-run.py <log_file> <cmd...>", file=sys.stderr)
        sys.exit(2)
    log_path = os.path.abspath(sys.argv[1])
    cmd = sys.argv[2:]
    daemonize(log_path, cmd)


if __name__ == "__main__":
    main()
