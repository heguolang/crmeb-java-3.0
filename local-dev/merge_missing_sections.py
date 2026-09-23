# -*- coding: utf-8 -*-
"""把 02_patches_all.sql 里 ALL_IN_ONE 缺失的 14 个段按依赖顺序搬进 ALL_IN_ONE.sql。

用法:
  python local-dev/merge_missing_sections.py --preview   # 只打印每段的关键语句，不改文件
  python local-dev/merge_missing_sections.py --apply     # 真写（先自动备份）
"""
import re
import shutil
import sys
import time

ROOT = r"D:\crmeb-java-3.0\crmeb\sql\oneclick"
SRC_02 = ROOT + r"\02_patches_all.sql"
DST = ROOT + r"\ALL_IN_ONE.sql"

# 待搬段（02 里的原始顺序，也是依赖顺序）
BLOCK_TARGET = [  # 插到 stock_adjust_log_link_uid.sql 之后
    "stock_exchange_target_type.sql",
]
BLOCK_RESTRUCTURE = [  # 插到 fix_virtual_in_log_20260921.sql 之后
    "menu_restructure_20260922.sql",
    "store_product_group_20260922.sql",
    "product_commission_config_20260922.sql",
    "distributor_level_20260923.sql",
    "distributor_level_upgrade_20260923.sql",
    "product_group_level_source.sql",
    "product_group_theme_20260923.sql",
    "product_group_permission_20260923.sql",
    "fix_duplicate_data_20260923.sql",
    "system_settings_20260923.sql",
    "menu_sync_20260923.sql",
    "table_comments_20260923.sql",
]
BLOCK_TAIL = [  # 插到 distributor_level_order_product_20260923.sql 之后
    "cleanup_deprecated_group_data_20260924.sql",
]

SEG_RE = re.compile(r"(?P<all>-- =+ BEGIN: (?P<name>\S+\.sql) =+.*?-- =+ END: (?P=name) =+)", re.S)


def parse(text):
    return {m.group("name"): m.group("all") for m in SEG_RE.finditer(text)}


def key_lines(seg):
    """挑出关键语句用于预览。"""
    out = []
    for ln in seg.splitlines():
        s = ln.strip()
        if not s or s.startswith("--"):
            continue
        if re.match(r"(?i)(UPDATE|ALTER|DELETE|CREATE|DROP|INSERT|SET|CALL)", s):
            out.append(s[:150])
    return out


def main():
    apply = "--apply" in sys.argv
    src = open(SRC_02, encoding="utf-8").read()
    dst = open(DST, encoding="utf-8").read()

    seg02 = parse(src)
    segdst = parse(dst)
    print("02 段数=%d   ALL_IN_ONE 段数=%d" % (len(seg02), len(segdst)))

    todo = BLOCK_TARGET + BLOCK_RESTRUCTURE + BLOCK_TAIL
    for n in todo:
        if n not in seg02:
            print("!! 02 里找不到段 %s" % n)
            return 1
        if n in segdst:
            print("!! ALL_IN_ONE 里已存在同名段 %s（跳过以免重复）" % n)
            return 1

    if not apply:
        for n in todo:
            kl = key_lines(seg02[n])
            print("\n" + "=" * 70)
            print("段 %s   (%d 行, 关键语句 %d 条)" % (n, seg02[n].count("\n") + 1, len(kl)))
            for x in kl[:14]:
                print("   " + x)
            if len(kl) > 14:
                major = [x for x in kl if re.match(r"(?i)(UPDATE|ALTER|CREATE|DELETE)", x)]
                print("   ... 另有 %d 条；其中写类(UPDATE/ALTER/CREATE/DELETE) %d 条"
                      % (len(kl) - 14, len(major)))
                for x in major[:10]:
                    print("   * " + x)
        return 0

    # ---- apply ----
    bak = ROOT.replace("\\crmeb\\sql\\oneclick", "")  # 占位，实际用下面拼
    bak = r"D:\crmeb-java-3.0\local-dev\backup_20260924\ALL_IN_ONE_before_merge_%s.sql" % \
          time.strftime("%Y%m%d_%H%M%S")
    shutil.copy2(DST, bak)
    print("备份 -> %s" % bak)

    def insert_after(text, anchor_seg, new_segs):
        line = "-- ========== END: %s ==========" % anchor_seg
        i = text.index(line)
        end = i + len(line)
        block = "\n\n" + "\n\n".join(new_segs) + "\n"
        return text[:end] + block + text[end:]

    dst = insert_after(dst, "stock_adjust_log_link_uid.sql",
                       [seg02[n] for n in BLOCK_TARGET])
    dst = insert_after(dst, "fix_virtual_in_log_20260921.sql",
                       [seg02[n] for n in BLOCK_RESTRUCTURE])
    dst = insert_after(dst, "distributor_level_order_product_20260923.sql",
                       [seg02[n] for n in BLOCK_TAIL])

    open(DST, "w", encoding="utf-8", newline="\n").write(dst)
    print("已写入 %s" % DST)

    # 复核
    after = parse(open(DST, encoding="utf-8").read())
    print("合并后 ALL_IN_ONE 段数=%d （原 %d）" % (len(after), len(segdst)))
    miss = [n for n in todo if n not in after]
    print("待搬段缺失=%s" % (miss or "无"))
    return 0


if __name__ == "__main__":
    sys.exit(main())
