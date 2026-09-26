# -*- coding: utf-8 -*-
"""uni-app 发行微信小程序后的收尾处理。

uni-app 编译期会在 common/main.wxss 末尾注入一段框架内置的
@keyframes shadow-preload，其 background-image 指向外链
https://cdn1.dcloud.net.cn/.../img/shadow-grey.png。
该域名默认不在小程序服务器域名白名单里，会在开发者工具 / 真机调试里
产生「域名不在合法域名列表」的报错。此脚本把它替换成内嵌 base64，
彻底消除外链请求。

用法：
  python mp_postbuild_localize.py [产物目录]
默认产物目录：D:/qianxu-java-3.0/app/unpackage/dist/build/mp-weixin
"""
import base64
import io
import os
import re
import sys
import time

HERE = os.path.dirname(os.path.abspath(__file__))
DEFAULT_DIST = r"D:/qianxu-java-3.0/app/unpackage/dist/build/mp-weixin"

dist = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_DIST
wxss = os.path.join(dist, "common", "main.wxss")

png_path = os.path.join(HERE, "assets", "shadow-grey.png")
with open(png_path, "rb") as f:
    b64_png = base64.b64encode(f.read()).decode()
data_url = "data:image/png;base64," + b64_png

with io.open(wxss, "r", encoding="utf-8", errors="ignore") as f:
    css = f.read()

hits = len(re.findall(r"https?://cdn1\.dcloud\.net\.cn[^\)\"']*", css))
if hits == 0:
    print("main.wxss 未发现 cdn1.dcloud.net.cn 外链，无需处理")
    sys.exit(0)

bak_dir = os.path.join(os.path.dirname(HERE), "backup")
os.makedirs(bak_dir, exist_ok=True)
bak = os.path.join(bak_dir, "main.wxss.bak_" + time.strftime("%Y%m%d_%H%M%S"))
with io.open(bak, "w", encoding="utf-8") as f:
    f.write(css)

css = re.sub(r"https?://cdn1\.dcloud\.net\.cn[^\)\"']*", data_url, css)

with io.open(wxss, "w", encoding="utf-8") as f:
    f.write(css)

left = len(re.findall(r"cdn1\.dcloud\.net\.cn", css))
print("已替换 cdn1.dcloud.net.cn 外链 %d 处 -> base64 内嵌，残留 %d" % (hits, left))
print("备份:", bak)
