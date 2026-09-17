#!/usr/bin/env python3
"""静态服务 + SPA 回退，用于本地预览 uni-app H5 构建产物（history 路由直接访问不 404）。

用法：
    python3 local-dev/serve-spa.py <dist目录> [端口]
"""
import os
import sys
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer


class SpaHandler(SimpleHTTPRequestHandler):
    def do_GET(self):
        path = self.translate_path(self.path)
        # 静态文件存在就直接返回，否则回退到 index.html（交给前端路由处理）
        if not os.path.exists(path) or os.path.isdir(path):
            self.path = '/index.html'
        return SimpleHTTPRequestHandler.do_GET(self)

    def end_headers(self):
        self.send_header('Cache-Control', 'no-store')
        SimpleHTTPRequestHandler.end_headers(self)

    def log_message(self, fmt, *args):
        sys.stderr.write("%s - %s\n" % (self.address_string(), fmt % args))


def main():
    root = sys.argv[1] if len(sys.argv) > 1 else '.'
    port = int(sys.argv[2]) if len(sys.argv) > 2 else 8124
    os.chdir(root)
    srv = ThreadingHTTPServer(('127.0.0.1', port), SpaHandler)
    print(f"serving {root} at http://127.0.0.1:{port}/", flush=True)
    srv.serve_forever()


if __name__ == '__main__':
    main()
