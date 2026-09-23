# -*- coding: utf-8 -*-
"""
生成本地→线上的「项目配置 + 菜单可见树」幂等收敛脚本。

输入：
  local-dev/tmp/local_config.tsv   本地 eb_system_config（HEX 编码 value）
  local-dev/tmp/menu_full.tsv      本地 eb_system_menu 全表 + 父级 component
  crmeb/sql/Crmeb_v3.0.sql         厂商基库 dump（用于算「与基库不同」的差异集）

输出：
  crmeb/sql/system_settings_20260923.sql
  crmeb/sql/menu_sync_20260923.sql
"""
import re, os, datetime

TMP = r'D:\crmeb-java-3.0\local-dev\tmp'
SQLDIR = r'D:\crmeb-java-3.0\crmeb\sql'
BASE_SQL = os.path.join(SQLDIR, 'Crmeb_v3.0.sql')

PROD_API = 'http://api.qianxutec.com'
PROD_H5 = 'http://app.qianxutec.com'

ADDR_MAP = {
    'api_url': PROD_API,
    'front_api_url': PROD_API,
    'localUploadUrl': PROD_API,
    'site_url': PROD_H5,
}

CRED_HINTS = ('secret', 'key', 'keyid', 'pwd', 'password', 'token', 'mchid',
              'appid', 'app_id', 'appsecret', 'client', 'cert', 'private')

CHANGED_NAMES = ['api_url', 'brokerage_func_status', 'change_color_config', 'config_export_open',
                 'copyright_company_name', 'copyright_internet_record', 'copyright_internet_record_url',
                 'crmeb_tongji_js', 'front_api_url', 'integral_ratio', 'localUploadUrl',
                 'logistics_type', 'mobile_login_logo', 'order_give_integral', 'routine_name',
                 'routine_phone_verification', 'seo_title', 'site_logo_lefttop', 'site_logo_login',
                 'site_logo_square', 'site_name', 'site_url', 'splash_ad_switch',
                 'store_brokerage_is_bubble', 'system_product_copy_type', 'telephone_service_switch',
                 'user_extract_min_price', 'wechat_routine_shipping_switch']

ESC = {'\\': '\\\\', "'": "\\'", '\n': '\\n', '\r': '\\r', '\t': '\\t', '\0': '\\0', '\x1a': '\\Z'}


def q(s):
    """mysqldump 风格转义 → 单行 SQL 字面量（对文件换行转换免疫）。"""
    if s is None:
        return 'NULL'
    return "'" + ''.join(ESC.get(ch, ch) for ch in s) + "'"


# ---------------- 1. 本地配置 ----------------
loc, local_order = {}, []
for line in open(os.path.join(TMP, 'local_config.tsv'), encoding='utf-8'):
    p = line.rstrip('\n').split('\t')
    if len(p) < 6:
        continue
    name = p[1]
    loc[name] = {'title': p[2],
                 'value': bytes.fromhex(p[5]).decode('utf-8', 'replace') if p[5] else ''}
    local_order.append(name)

# ---------------- 2. 基库配置名 ----------------
base_names = set()
with open(BASE_SQL, encoding='utf-8', errors='replace') as f:
    for line in f:
        m = re.search(r"INSERT INTO `eb_system_config` VALUES \(\d+, '([^']*)'", line)
        if m:
            base_names.add(m.group(1))

new_names = [n for n in local_order if n not in base_names]
sync_names = [n for n in new_names if n in loc] + [n for n in CHANGED_NAMES if n in loc]

bad = [n for n in sync_names if any(h in n.lower() for h in CRED_HINTS)]
assert not bad, '凭证类混入同步集: %s' % bad

rows_cfg, addr_log = [], []
for n in sync_names:
    v = loc[n]['value']
    if n in ADDR_MAP:
        if v != ADDR_MAP[n]:
            addr_log.append((n, v, ADDR_MAP[n]))
        v = ADDR_MAP[n]
    rows_cfg.append((n, loc[n]['title'], v))

# ---------------- 3. system_settings_20260923.sql ----------------
chg_lines = '\n'.join(
    '--   %-32s %s  →  %s' % (n, (loc[n]['value'] or '(空)').replace('\n', '\\n')[:34], '生产地址')
    if n in ADDR_MAP else
    '--   %-32s %s' % (n, (loc[n]['value'] or '(空)').replace('\n', '\\n')[:44])
    for n in CHANGED_NAMES if n in loc)

hdr = f"""-- ============================================================
-- CRMEB Java 3.0 项目配置收敛（本地配置 → 线上）
-- 生成时间: {datetime.datetime.now():%Y-%m-%d %H:%M:%S}
-- 生成来源：本地库 eb_system_config 与厂商基库 Crmeb_v3.0.sql 的差异集
--   · 我方新增配置 {len(new_names)} 项（订货商 / 团队奖 / 分销商等级 / 区域代理 / 提现 / 隐藏面板开关等）
--   · 值被我方修改 {len(CHANGED_NAMES)} 项，逐条如下：
{chg_lines}
--
-- ⛔ 刻意【不同步】的东西（保持线上现值）：
--   1. 全部凭证 / 密钥类配置——微信支付商户号与证书、支付宝密钥、短信账号 token、
--      阿里云 / 七牛 / 腾讯云 / 京东存储密钥、地图 key、小票打印密钥、快递查询码等。
--      本地这些值全是厂商占位符（111111 / 1111 / xxxx），推上去会让线上的
--      支付、短信、图片上传立即不可用。下游部署后请在后台自行填写。
--   2. 与厂商基库取值一致的其余配置项——不同步，避免用厂商默认值覆盖线上真实值
--      （例如 consumer_hotline、system_express_app_code 之类线上可能已配置过的项）。
--
-- 地址口径（生产）：接口 / 图片域名 {PROD_API}；移动端站点 {PROD_H5}
--   site_url 的语义是「移动端站点域名」，被微信 H5 支付当作 h5_info.wap_url 使用
--   （OrderPayServiceImpl#getUnifiedorderVo），因此指向 H5 站点而不是接口域名。
--
-- 幂等：临时表 + 按 name 匹配，跑多少遍结果一致；不触碰任何业务数据。
-- ============================================================
SET NAMES utf8mb4;

CREATE TEMPORARY TABLE `tmp_cfg_sync` (
  `name`  varchar(255) NOT NULL,
  `title` varchar(255) NOT NULL DEFAULT '',
  `value` text,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `tmp_cfg_sync` (`name`, `title`, `value`) VALUES
"""

vals = ',\n'.join('(%s, %s, %s)' % (q(n), q(t), q(v)) for n, t, v in rows_cfg)

tail = """;
-- 1) 已存在的项：值或标题不同才写，避免无谓刷新 update_time
UPDATE eb_system_config c
  JOIN tmp_cfg_sync t ON c.`name` = t.`name`
   SET c.`value`       = t.`value`,
       c.`title`       = IF(t.`title` = '', c.`title`, t.`title`),
       c.`update_time` = NOW()
 WHERE NOT (c.`value` <=> t.`value`)
    OR (t.`title` <> '' AND NOT (c.`title` <=> t.`title`));

-- 2) 线上缺失的项：补插（不覆盖任何已有行）
INSERT INTO eb_system_config (`name`, `title`, `form_id`, `value`, `status`, `create_time`, `update_time`)
SELECT t.`name`, t.`title`, 0, t.`value`, 0, NOW(), NOW()
  FROM tmp_cfg_sync t
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT `name` FROM eb_system_config) x
                    WHERE x.`name` = t.`name`);

DROP TEMPORARY TABLE `tmp_cfg_sync`;

-- 3) 自检：下列各项必须全部存在（数量应与本次同步项数一致）
SELECT 'CRMEB system_settings done' AS result, COUNT(*) AS ensured_rows
  FROM eb_system_config WHERE `name` IN (""" + ', '.join(q(n) for n, _, _ in rows_cfg) + """);
"""

with open(os.path.join(SQLDIR, 'system_settings_20260923.sql'), 'w',
          encoding='utf-8', newline='\n') as f:
    f.write(hdr + vals + tail)

print('配置：同步 %d 项（新增 %d + 改值 %d）' % (len(rows_cfg), len(new_names), len(CHANGED_NAMES)))
print('地址归一 %d 处：' % len(addr_log))
for n, a, b in addr_log:
    print('   %-18s %r  ->  %r' % (n, a, b))
print()

# ---------------- 4. 菜单 ----------------
rows = [l.rstrip('\n').split('\t') for l in open(os.path.join(TMP, 'menu_full.tsv'), encoding='utf-8')]
rows = [r for r in rows if len(r) >= 12]

alive = {r[5]: r for r in rows if r[6] in ('M', 'C') and r[5]}
# ↑ 可作为父级的目标：所有 component 非空的 M/C 行，**含已标记删除的**。
#   若只认 is_delte=0 的父级，那些「父级已删除、子级也标记删除」的行
#   （如 /operation/design 下的页面设计 / 一键换色 / 页面装修）会因为找不到父级
#   被当成一级菜单、pid 被归 0，与本地实际状态产生无意义漂移。
menu_rows, skipped = [], []
for r in rows:
    if r[6] not in ('M', 'C'):
        continue
    comp, mtype = r[5], r[6]
    if comp == '':
        skipped.append((r[0], r[2], mtype))
        continue
    pc = r[10]
    if pc and pc not in alive:
        pc = ''
    menu_rows.append({'component': comp, 'menu_type': mtype, 'parent': pc, 'perms': r[4],
                      'name': r[2], 'icon': r[3], 'sort': r[7], 'is_show': r[8], 'is_delte': r[9]})

seen = {}
for m in menu_rows:
    seen.setdefault((m['component'], m['menu_type']), []).append(m)
dups = {k: v for k, v in seen.items() if len(v) > 1}
menu_rows = sorted((v[0] for v in seen.values()), key=lambda m: (len(m['parent']), m['component']))

deleted = [m for m in menu_rows if m['is_delte'] == '1']
hidden = [m for m in menu_rows if m['is_show'] == '0' and m['is_delte'] == '0']
depth1 = [m for m in menu_rows if m['parent']]

mhdr = f"""-- ============================================================
-- CRMEB Java 3.0 后台菜单可见树收敛（本地 → 线上）
-- 生成时间: {datetime.datetime.now():%Y-%m-%d %H:%M:%S}
-- 范围：menu_type 为 M（目录）/ C（菜单）的 {len(menu_rows)} 项，即后台左侧导航的完整可见树。
--       其中标记为删除 {len(deleted)} 项、隐藏 {len(hidden)} 项、有父级归属 {len(depth1)} 项。
--       A（按钮 / 权限点）不在本脚本范围——它们不参与“显示”，且由各功能的 perms
--       脚本（stock / merchant_store / distributor_level / add_*_menu 等）分别维护。
--
-- 定位方式：一律用 `component` + `menu_type` 匹配，**不使用自增 id**
--          （线上 id 与本机可能不同）；父级归属同样按父级 component 反查 id。
--           注：/dashboard 在本地同时存在 M（首页）与 C（控制台）两条，故键必须带 menu_type。
--
-- 幂等：临时表 + NOT EXISTS 补插 + 有差异才写。补插段重复 3 次是为了让
--       多级父目录先于子菜单落库（层级最多 3 层），重复执行结果不变。
--
-- 副作用（符合预期）：线上这些菜单的名称 / 图标 / 排序 / 显隐 / 所属目录 / 删除标记
--       会被收敛为本地当前状态；线上独有的菜单不在映射表内，不会被触碰。
-- ============================================================
SET NAMES utf8mb4;

CREATE TEMPORARY TABLE `tmp_menu_sync` (
  `component`        varchar(200) NOT NULL,
  `menu_type`        varchar(2)   NOT NULL,
  `parent_component` varchar(200) NOT NULL DEFAULT '',
  `perms`            varchar(200) NOT NULL DEFAULT '',
  `name`             varchar(100) NOT NULL,
  `icon`             varchar(255) NOT NULL DEFAULT '',
  `sort`             int          NOT NULL DEFAULT 99999,
  `is_show`          tinyint      NOT NULL DEFAULT 1,
  `is_delte`         tinyint      NOT NULL DEFAULT 0,
  PRIMARY KEY (`component`, `menu_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO `tmp_menu_sync`
  (`component`, `menu_type`, `parent_component`, `perms`, `name`, `icon`, `sort`, `is_show`, `is_delte`) VALUES
"""

mvals = ',\n'.join(
    '(%s, %s, %s, %s, %s, %s, %s, %s, %s)' % (
        q(m['component']), q(m['menu_type']), q(m['parent']), q(m['perms'] or ''),
        q(m['name']), q(m['icon'] or ''), m['sort'] or '99999', m['is_show'], m['is_delte'])
    for m in menu_rows)

CANON = """-- 0) 每个 (component, menu_type) 的「规范行」= id 最小的一条。
--    历史补丁可能已经插出重复菜单（例如 /yunying 曾经每次执行都多插一条），
--    重复行由 fix_duplicate_data_20260923.sql 标记 is_delte=1；
--    这里后续的收敛只作用于规范行，否则会把刚标记删除的重复行又改回未删除，
--    与去重逻辑来回震荡、永远不幂等。
DROP TEMPORARY TABLE IF EXISTS `tmp_menu_canon`;
CREATE TEMPORARY TABLE `tmp_menu_canon` AS
SELECT `component`, `menu_type`, MIN(`id`) AS `id`
  FROM eb_system_menu
 WHERE `component` IS NOT NULL AND `component` <> ''
 GROUP BY `component`, `menu_type`;

"""

INS = """-- 1) 补插线上缺失的菜单。父级必须先落库，故「物化父级查找表 + 补插」重复 3 次
--    （层级最多 3 层）；NOT EXISTS 保证重复执行不出重复行。
--    父级查找表单独物化还有一个原因：MySQL 不允许同一查询里两次引用同一张临时表
--    （否则 ERROR 1137 Can't reopen table），所以不能把 tmp_menu_sync 既当主表又当父表。
DROP TEMPORARY TABLE IF EXISTS `tmp_menu_parent`;
CREATE TEMPORARY TABLE `tmp_menu_parent` AS
SELECT t.`component`, t.`menu_type`, MIN(p.`id`) AS `pid`
  FROM tmp_menu_sync t
  JOIN eb_system_menu p
    ON p.`component` = t.`parent_component`   -- 父级已标记删除也照挂，保持原有归属
 WHERE t.`parent_component` <> ''      -- ← 必须有！否则空父级会去匹配所有 component='' 的按钮行
 GROUP BY t.`component`, t.`menu_type`;

INSERT INTO eb_system_menu (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT COALESCE(mp.`pid`, 0), t.`name`, t.`icon`, t.`perms`, t.`component`,
       t.`menu_type`, t.`sort`, t.`is_show`, t.`is_delte`
  FROM tmp_menu_sync t
  LEFT JOIN `tmp_menu_parent` mp
         ON mp.`component` = t.`component` AND mp.`menu_type` = t.`menu_type`
 WHERE NOT EXISTS (SELECT 1 FROM (SELECT `component`, `menu_type` FROM eb_system_menu) x
                    WHERE x.`component` = t.`component` AND x.`menu_type` = t.`menu_type`)
   AND (t.`parent_component` = '' OR mp.`pid` IS NOT NULL);

"""

mtail = """
-- 2) 收敛规范行的名称 / 图标 / 权限标识 / 排序 / 显隐 / 删除标记
UPDATE eb_system_menu m
  JOIN `tmp_menu_canon` c ON m.`id` = c.`id`
  JOIN tmp_menu_sync t
    ON t.`component` = c.`component` AND t.`menu_type` = c.`menu_type`
   SET m.`name`        = t.`name`,
       m.`icon`        = t.`icon`,
       m.`perms`       = IF(t.`perms` = '', m.`perms`, t.`perms`),
       m.`sort`        = t.`sort`,
       m.`is_show`     = t.`is_show`,
       m.`is_delte`    = t.`is_delte`,
       m.`update_time` = NOW()
 WHERE NOT (m.`name` <=> t.`name`)
    OR NOT (m.`icon` <=> t.`icon`)
    OR NOT (m.`sort` <=> t.`sort`)
    OR NOT (m.`is_show` <=> t.`is_show`)
    OR NOT (m.`is_delte` <=> t.`is_delte`)
    OR (t.`perms` <> '' AND NOT (m.`perms` <=> t.`perms`));

-- 3) 收敛父级归属（沿用最后一次重建的父级查找表）
--    3a) 有父级的：pid 指向父级 component 对应的菜单 id
UPDATE eb_system_menu m
  JOIN `tmp_menu_canon` c ON m.`id` = c.`id`
  JOIN `tmp_menu_parent` mp
    ON mp.`component` = c.`component` AND mp.`menu_type` = c.`menu_type`
   SET m.`pid` = mp.`pid`, m.`update_time` = NOW()
 WHERE NOT (m.`pid` <=> mp.`pid`);

--    3b) 一级目录 / 顶层菜单：pid 归 0
UPDATE eb_system_menu m
  JOIN `tmp_menu_canon` c ON m.`id` = c.`id`
  JOIN tmp_menu_sync t
    ON t.`component` = c.`component` AND t.`menu_type` = c.`menu_type`
   SET m.`pid` = 0, m.`update_time` = NOW()
 WHERE t.`parent_component` = '' AND m.`pid` <> 0;

DROP TEMPORARY TABLE `tmp_menu_parent`;
DROP TEMPORARY TABLE `tmp_menu_canon`;
DROP TEMPORARY TABLE `tmp_menu_sync`;

-- 4) 自检：可见树里应当能查到全部已删除标记
SELECT 'CRMEB menu_sync done' AS result,
       SUM(`menu_type` = 'M') AS dirs,
       SUM(`menu_type` = 'C') AS menus,
       SUM(IFNULL(`is_delte`, 0) = 1 AND `menu_type` IN ('M', 'C')) AS deleted_mark
  FROM eb_system_menu;
"""

with open(os.path.join(SQLDIR, 'menu_sync_20260923.sql'), 'w',
          encoding='utf-8', newline='\n') as f:
    f.write(mhdr + mvals + ';\n\n' + CANON + (INS * 3) + mtail)

print('菜单：收敛 %d 个 M/C 菜单（删除标记 %d / 隐藏 %d / 带父级 %d）'
      % (len(menu_rows), len(deleted), len(hidden), len(depth1)))
if dups:
    print('  跳过重复键:', list(dups))
if skipped:
    print('  跳过（空 component，不显示）:', skipped)
