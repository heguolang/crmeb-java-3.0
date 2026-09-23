# -*- coding: utf-8 -*-
"""把新脚本并入 oneclick/02_patches_all.sql，并刷新 update_http_domain 分节。
严格二进制读写 + 锚点定位，避免编辑器/换行二次转码污染（历史踩坑）。"""
import io, os

ROOT = r'D:\crmeb-java-3.0'
SQL = os.path.join(ROOT, 'crmeb', 'sql')
PATCH = os.path.join(SQL, 'oneclick', '02_patches_all.sql')

NEW_SECTIONS = ['system_settings_20260923.sql', 'menu_sync_20260923.sql', 'table_comments_20260923.sql']
# 说明：local_default_settings.sql 里那两条会冲掉线上凭证的 REPLACE INTO 已直接
# 从**源文件**停用（2026-09-23），因此这里不再需要跳过它——保持「包内分节 == 源文件」
# 这一不变量，避免以后从零重建补丁包时又把危险语句带回来。
SKIP_REFRESH = set()

# 刷新策略：包里每个分节，只要源文件存在且正文不同，就按源文件刷新
# （历史上手工追加导致包里囤了一批旧注释、旧语句）。
# 新分节仍走 NEW_SECTIONS 追加。

raw = open(PATCH, 'rb').read()
text = raw.decode('utf-8')          # 抛错即说明文件已被污染，直接失败
print('原文件 UTF-8 合法，%d 字符' % len(text))

before_cfg = text.count('BEGIN: system_settings_20260923.sql')
before_menu = text.count('BEGIN: menu_sync_20260923.sql')
print('已存在的分节: system_settings=%d menu_sync=%d' % (before_cfg, before_menu))

# ---- 1. 刷新已有分节的正文（按源文件；跳过 SKIP_REFRESH）----
import re as _re
_present = _re.findall(r'-- ========== BEGIN: (.+?) ==========', text)
REFRESH = [n for n in _present if n not in SKIP_REFRESH and os.path.isfile(os.path.join(SQL, n))]
for name in REFRESH:
    b = '-- ========== BEGIN: %s ==========\n' % name
    e = '\n-- ========== END: %s ==========' % name
    i = text.find(b)
    if i < 0:
        print('  跳过（无该分节）:', name)
        continue
    j = text.find(e, i)
    assert j > 0, '找不到 END 标记: ' + name
    body = open(os.path.join(SQL, name), encoding='utf-8', newline='\n').read().rstrip('\n')
    text = text[:i + len(b)] + body + text[j:]
    print('  已刷新分节正文:', name)

# ---- 2. 追加新分节 ----
add = []
for name in NEW_SECTIONS:
    if ('BEGIN: %s ==========' % name) in text:
        print('  已存在，跳过追加:', name)
        continue
    body = open(os.path.join(SQL, name), encoding='utf-8', newline='\n').read().rstrip('\n')
    add.append('\n\n-- ========== BEGIN: %s ==========\n%s\n-- ========== END: %s =========='
               % (name, body, name))

if add:
    anchor = '\nSET FOREIGN_KEY_CHECKS = 1;'
    k = text.rfind(anchor)
    assert k > 0, '找不到尾部锚点 SET FOREIGN_KEY_CHECKS = 1;'
    text = text[:k] + ''.join(add) + text[k:]
    print('  已追加 %d 个分节' % len(add))

out = text.encode('utf-8')
open(PATCH, 'wb').write(out)
print('写入完成：%d 字节（原 %d 字节）' % (len(out), len(raw)))

# ---- 3. 复检 ----
chk = open(PATCH, 'rb').read().decode('utf-8')
import re
secs = re.findall(r'-- ========== BEGIN: (.+?) ==========', chk)
ends = re.findall(r'-- ========== END: (.+?) ==========', chk)
print('分节数: BEGIN=%d END=%d' % (len(secs), len(ends)))
print('BEGIN/END 顺序一致:', secs == ends)
print('末尾分节:', secs[-3:])
print('引号配对（单引号数为偶数）:', chk.count("'") % 2 == 0)

# ---- 4. 逐分节比对正文与源文件是否一致 ----
print()
print('=== 分节正文与源文件一致性 ===')
ok = True
for name, body in re.findall(r'-- ========== BEGIN: (.+?) ==========\n(.*?)\n-- ========== END: \1 ==========',
                            chk, re.S):
    src = os.path.join(SQL, name)
    if not os.path.isfile(src):
        continue
    want = open(src, encoding='utf-8', newline='\n').read().rstrip('\n')
    if body != want:
        ok = False
        print('  ❌ 正文不一致:', name)
print('  全部一致' if ok else '  存在不一致！')
