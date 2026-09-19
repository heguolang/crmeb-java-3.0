/**
 * Analyze test DB dump for known CRMEB issues; generate fix SQL.
 */
const fs = require('fs');
const path = require('path');
const readline = require('readline');

const dump = path.join(
  __dirname,
  '_test_dump',
  'crmeb_java3_2026-09-19_15-46-40_mysql_data_zG9tg.sql'
);
const reportPath = path.join(__dirname, '_test_dump', 'analysis_report.txt');
const fixPath = path.join(__dirname, 'FIX_TEST_ENV.sql');

const issues = [];
const notes = [];

// track CREATE TABLE columns
const tables = {}; // name -> Set(columns)
let currentCreate = null;
let createBuf = '';

// data scans
let localUrlHits = 0;
let localUrlTables = new Set();
const configNameCounts = {}; // name -> count from INSERT eb_system_config
const productIds = new Set();
const productAttrProductIds = new Set();
const productAttrValueProductIds = new Set();
let qrtzTables = new Set();
let hasSupportVirtual = false;
let hasAddressIdOnOrder = false;
let hasAddressIdOnExchange = false;
let menu691 = false;
let scheduleJobCount = 0;
let qrtzJobDetailsInsert = 0;

function addIssue(level, msg) {
  issues.push(`[${level}] ${msg}`);
}

function parseCreateColumns(sql) {
  const cols = new Set();
  const re = /`([^`]+)`\s+(?:[a-zA-Z(0-9_,.\s]+)/g;
  // crude: lines starting with `col`
  for (const line of sql.split('\n')) {
    const m = line.trim().match(/^`([^`]+)`\s/);
    if (m) cols.add(m[1]);
  }
  return cols;
}

function extractInsertTuples(line, table) {
  // very light: count occurrences of patterns
  return line;
}

async function main() {
  if (!fs.existsSync(dump)) {
    console.error('dump missing', dump);
    process.exit(1);
  }
  const rl = readline.createInterface({
    input: fs.createReadStream(dump, { encoding: 'utf8' }),
    crlfDelay: Infinity,
  });

  let inCreate = false;
  let createName = '';
  let createLines = [];
  let lineNo = 0;

  for await (const line of rl) {
    lineNo++;

    if (/CREATE TABLE/i.test(line)) {
      const m = line.match(/CREATE TABLE\s+(?:IF NOT EXISTS\s+)?`([^`]+)`/i);
      if (m) {
        inCreate = true;
        createName = m[1];
        createLines = [line];
      }
      continue;
    }
    if (inCreate) {
      createLines.push(line);
      if (/\)\s*ENGINE=/i.test(line) || /;/.test(line) && /ENGINE=/i.test(createLines.join('\n'))) {
        const body = createLines.join('\n');
        tables[createName] = parseCreateColumns(body);
        if (createName.startsWith('qrtz_')) qrtzTables.add(createName);
        if (createName === 'eb_stock_product_rel' && tables[createName].has('support_virtual')) {
          hasSupportVirtual = true;
        }
        if (createName === 'eb_stock_order' && tables[createName].has('address_id')) {
          hasAddressIdOnOrder = true;
        }
        if (createName === 'eb_stock_exchange' && tables[createName].has('address_id')) {
          hasAddressIdOnExchange = true;
        }
        inCreate = false;
        createName = '';
        createLines = [];
      }
      continue;
    }

    if (line.includes('127.0.0.1:8080') || line.includes('127.0.0.1:8081')) {
      localUrlHits++;
      const tm = line.match(/INSERT INTO `([^`]+)`/i);
      if (tm) localUrlTables.add(tm[1]);
      else if (line.includes('eb_theme')) localUrlTables.add('eb_theme?');
    }

    // INSERT INTO `eb_system_config` ... VALUES (...),(...);
    if (/INSERT INTO `eb_system_config`/i.test(line)) {
      // match name fields: ,'some_name',
      // values format typically: (id,'name',... or (id,"name"
      const re = /\((\d+),'([^']+)'/g;
      let m;
      while ((m = re.exec(line))) {
        const name = m[2];
        configNameCounts[name] = (configNameCounts[name] || 0) + 1;
      }
    }

    if (/INSERT INTO `eb_system_menu`/i.test(line) && line.includes(",691,")) {
      menu691 = true;
    }
    if (/INSERT INTO `eb_system_menu`/i.test(line) && /'层级/.test(line)) {
      menu691 = true;
    }
    if (/INSERT INTO `eb_system_menu`/i.test(line) && line.includes('/stock/level')) {
      menu691 = true;
    }

    if (/INSERT INTO `eb_store_product`/i.test(line) && !/attr/.test(line.split('`')[0])) {
      const re = /\((\d+),/g;
      let m;
      // only first number of each tuple - fragile for multi-line
      const parts = line.split('),(');
      parts.forEach((p, idx) => {
        const mm = p.match(/(?:^|\()\s*(\d+)\s*,/);
        if (mm) productIds.add(Number(mm[1]));
      });
    }

    if (/INSERT INTO `eb_store_product_attr`[^_]/i.test(line)) {
      // product_id often 2nd or later - use all ints near product_id columns hard
      // Fall back: extract ,'xxx' skip; get numbers after first (
      // Better: look for pattern typical dump column order
    }

    if (/INSERT INTO `eb_store_product_attr`\s/i.test(line) || /INSERT INTO `eb_store_product_attr`\(/i.test(line)) {
      // count tuples
    }

    if (/INSERT INTO `eb_schedule_job`/i.test(line)) {
      const n = (line.match(/\),\(/g) || []).length + (line.includes('VALUES') ? 1 : 0);
      scheduleJobCount += Math.max(n, 1);
    }

    if (/INSERT INTO `qrtz_job_details`/i.test(line)) {
      qrtzJobDetailsInsert++;
    }
  }

  // second pass for product attr linkage - simpler grep style on file chunks
  const contentSample = fs.readFileSync(dump, { encoding: 'utf8' });
  // product_id from attr table inserts
  {
    const reBlock = /INSERT INTO `eb_store_product_attr`[\s\S]*?;/gi;
    let m;
    while ((m = reBlock.exec(contentSample))) {
      const block = m[0];
      // column list
      const colsM = block.match(/INSERT INTO `eb_store_product_attr`\s*\(([^)]+)\)/i);
      if (colsM) {
        const cols = colsM[1].split(',').map((c) => c.replace(/`/g, '').trim());
        const pi = cols.indexOf('product_id');
        const tuples = block.match(/\([^)]+\)/g) || [];
        for (const t of tuples) {
          if (t.includes('INSERT')) continue;
          const vals = splitSqlTuple(t);
          if (pi >= 0 && vals[pi]) productAttrProductIds.add(Number(String(vals[pi]).replace(/'/g, '')));
        }
      }
    }
  }
  {
    const reBlock = /INSERT INTO `eb_store_product_attr_value`[\s\S]*?;/gi;
    let m;
    while ((m = reBlock.exec(contentSample))) {
      const block = m[0];
      const colsM = block.match(/INSERT INTO `eb_store_product_attr_value`\s*\(([^)]+)\)/i);
      if (colsM) {
        const cols = colsM[1].split(',').map((c) => c.replace(/`/g, '').trim());
        const pi = cols.indexOf('product_id');
        const tuples = block.match(/\((?:[^;()]|\([^)]*\))*\)/g) || [];
        // simpler split by ),(
        const raw = block.substring(block.indexOf('VALUES') + 6);
        const parts = raw.split(/\),\s*\(/);
        for (const p of parts) {
          const vals = splitSqlTuple('(' + p.replace(/^\(/, '').replace(/\);?\s*$/, '') + ')');
          if (pi >= 0 && vals[pi] != null) {
            const id = Number(String(vals[pi]).replace(/'/g, ''));
            if (!Number.isNaN(id)) productAttrValueProductIds.add(id);
          }
        }
      }
    }
  }
  // products from INSERT with column list
  {
    const reBlock = /INSERT INTO `eb_store_product`\s*\(([^)]+)\)\s*VALUES\s*/gi;
    let m;
    while ((m = reBlock.exec(contentSample))) {
      const cols = m[1].split(',').map((c) => c.replace(/`/g, '').trim());
      const idIdx = cols.indexOf('id');
      // find end of this insert - next INSERT or ;
      const start = m.index + m[0].length;
      let end = contentSample.indexOf(';\n', start);
      if (end < 0) end = contentSample.indexOf(';', start);
      const valuesPart = contentSample.slice(start, end);
      const parts = valuesPart.split(/\),\s*\(/);
      for (const p of parts) {
        const vals = splitSqlTuple('(' + p.replace(/^\(/, '').replace(/\)$/, '') + ')');
        if (idIdx >= 0 && vals[idIdx] != null) {
          const id = Number(String(vals[idIdx]).replace(/'/g, ''));
          if (!Number.isNaN(id)) productIds.add(id);
        }
      }
    }
  }

  // re-scan config duplicates properly
  Object.keys(configNameCounts).forEach((k) => delete configNameCounts[k]);
  {
    const reBlock = /INSERT INTO `eb_system_config`\s*\(([^)]+)\)\s*VALUES\s*/gi;
    let m;
    while ((m = reBlock.exec(contentSample))) {
      const cols = m[1].split(',').map((c) => c.replace(/`/g, '').trim());
      const nameIdx = cols.indexOf('name');
      const start = m.index + m[0].length;
      let end = contentSample.indexOf(';\n', start);
      if (end < 0) end = contentSample.indexOf(';', start);
      const valuesPart = contentSample.slice(start, end);
      const parts = valuesPart.split(/\),\s*\(/);
      for (const p of parts) {
        const vals = splitSqlTuple('(' + p.replace(/^\(/, '').replace(/\)$/, '') + ')');
        if (nameIdx >= 0 && vals[nameIdx] != null) {
          const name = String(vals[nameIdx]).replace(/^'|'$/g, '');
          configNameCounts[name] = (configNameCounts[name] || 0) + 1;
        }
      }
    }
  }

  // domain config values
  const domainConfigs = {};
  {
    const reBlock = /INSERT INTO `eb_system_config`\s*\(([^)]+)\)\s*VALUES\s*/gi;
    let m;
    while ((m = reBlock.exec(contentSample))) {
      const cols = m[1].split(',').map((c) => c.replace(/`/g, '').trim());
      const nameIdx = cols.indexOf('name');
      const valueIdx = cols.indexOf('value');
      const start = m.index + m[0].length;
      let end = contentSample.indexOf(';\n', start);
      if (end < 0) end = contentSample.indexOf(';', start);
      const valuesPart = contentSample.slice(start, end);
      const parts = valuesPart.split(/\),\s*\(/);
      for (const p of parts) {
        const vals = splitSqlTuple('(' + p.replace(/^\(/, '').replace(/\)$/, '') + ')');
        if (nameIdx < 0 || valueIdx < 0) continue;
        const name = String(vals[nameIdx] || '').replace(/^'|'$/g, '');
        if (['localUploadUrl', 'api_url', 'front_api_url', 'site_url'].includes(name)) {
          domainConfigs[name] = String(vals[valueIdx] || '').replace(/^'|'$/g, '');
        }
      }
    }
  }

  // menu /stock/level
  const hasLevelMenu = /\/stock\/level/.test(contentSample);
  const hasSupportVirtualCol =
    tables['eb_stock_product_rel'] && tables['eb_stock_product_rel'].has('support_virtual');
  const hasSupportPhysicalCol =
    tables['eb_stock_product_rel'] && tables['eb_stock_product_rel'].has('support_physical');
  const hasOrderAddress =
    tables['eb_stock_order'] && tables['eb_stock_order'].has('address_id');
  const hasExchangeAddress =
    tables['eb_stock_exchange'] && tables['eb_stock_exchange'].has('address_id');

  const missingQrtz = [
    'qrtz_job_details',
    'qrtz_triggers',
    'qrtz_cron_triggers',
    'qrtz_simple_triggers',
    'qrtz_simprop_triggers',
    'qrtz_blob_triggers',
    'qrtz_fired_triggers',
    'qrtz_locks',
    'qrtz_paused_trigger_grps',
    'qrtz_scheduler_state',
  ].filter((t) => !tables[t]);

  const dupConfigs = Object.entries(configNameCounts)
    .filter(([, c]) => c > 1)
    .sort((a, b) => b[1] - a[1]);

  const productsMissingAttr = [...productIds].filter((id) => !productAttrProductIds.has(id));
  const productsMissingAttrValue = [...productIds].filter((id) => !productAttrValueProductIds.has(id));

  // theme localhost
  const themeHasLocal = /INSERT INTO `eb_theme`[\s\S]{0,500000}?127\.0\.0\.1:8080/.test(contentSample);
  const diyHasLocal = /INSERT INTO `eb_page_diy`[\s\S]{0,500000}?127\.0\.0\.1:8080/.test(contentSample);

  // ---- report ----
  const report = [];
  report.push('=== Test DB dump analysis ===');
  report.push(`Tables: ${Object.keys(tables).length}`);
  report.push(`Products: ${productIds.size}`);
  report.push(`Products with attr: ${productAttrProductIds.size}`);
  report.push(`Products with attr_value: ${productAttrValueProductIds.size}`);
  report.push(`Products missing attr: ${productsMissingAttr.length}`);
  report.push(`Products missing attr_value: ${productsMissingAttrValue.length}`);
  if (productsMissingAttrValue.length && productsMissingAttrValue.length <= 30) {
    report.push(`  ids: ${productsMissingAttrValue.join(',')}`);
  } else if (productsMissingAttrValue.length) {
    report.push(`  sample ids: ${productsMissingAttrValue.slice(0, 20).join(',')}`);
  }
  report.push(`localUploadUrl etc: ${JSON.stringify(domainConfigs)}`);
  report.push(`localhost URL hits (lines): ${localUrlHits}, tables: ${[...localUrlTables].join(',')}`);
  report.push(`theme has 127.0.0.1:8080: ${themeHasLocal}`);
  report.push(`diy has 127.0.0.1:8080: ${diyHasLocal}`);
  report.push(`support_virtual col: ${hasSupportVirtualCol}`);
  report.push(`support_physical col: ${hasSupportPhysicalCol}`);
  report.push(`eb_stock_order.address_id: ${hasOrderAddress}`);
  report.push(`eb_stock_exchange.address_id: ${hasExchangeAddress}`);
  report.push(`missing qrtz tables: ${missingQrtz.join(',') || 'none'}`);
  report.push(`qrtz tables present: ${[...qrtzTables].join(',')}`);
  report.push(`level menu /stock/level: ${hasLevelMenu}`);
  report.push(`duplicate config names: ${dupConfigs.length}`);
  dupConfigs.slice(0, 40).forEach(([n, c]) => report.push(`  ${n} x${c}`));

  fs.writeFileSync(reportPath, report.join('\n'), 'utf8');
  console.log(report.join('\n'));

  // ---- generate fix SQL ----
  const fix = [];
  fix.push('-- ============================================================');
  fix.push('-- 测试环境修复脚本 FIX_TEST_ENV.sql');
  fix.push('-- 基于备份 crmeb_java3_2026-09-19_15-46-40 分析结果生成');
  fix.push('-- 在测试库 crmeb_java3 执行；建议先备份');
  fix.push('-- ============================================================');
  fix.push('SET NAMES utf8mb4;');
  fix.push('SET FOREIGN_KEY_CHECKS = 0;');
  fix.push('USE `crmeb_java3`;');
  fix.push('');

  // columns
  fix.push('-- ---------- 1. 缺列补齐（幂等） ----------');
  fix.push(addColProc());
  if (!hasSupportVirtualCol) {
    fix.push("CALL add_col_if_missing('eb_stock_product_rel','support_virtual','support_virtual tinyint(1) NOT NULL DEFAULT 1 COMMENT \\'是否支持虚拟库存\\'');");
  }
  if (!hasSupportPhysicalCol) {
    fix.push("CALL add_col_if_missing('eb_stock_product_rel','support_physical','support_physical tinyint(1) NOT NULL DEFAULT 1 COMMENT \\'是否支持实体库存\\'');");
  }
  if (!hasOrderAddress) {
    fix.push("CALL add_col_if_missing('eb_stock_order','address_id','address_id int(11) DEFAULT NULL COMMENT \\'下单地址ID\\' AFTER `mark`');");
    fix.push("CALL add_col_if_missing('eb_stock_order','real_name','real_name varchar(64) DEFAULT \\'\\' COMMENT \\'收货人\\'');");
    fix.push("CALL add_col_if_missing('eb_stock_order','phone','phone varchar(32) DEFAULT \\'\\' COMMENT \\'收货电话\\'');");
    fix.push("CALL add_col_if_missing('eb_stock_order','user_address','user_address varchar(500) DEFAULT \\'\\' COMMENT \\'收货地址\\'');");
    fix.push("CALL add_col_if_missing('eb_stock_order','stock_type','stock_type tinyint(1) DEFAULT 1 COMMENT \\'1实体2虚拟\\'');");
    fix.push("CALL add_col_if_missing('eb_stock_order','order_type','order_type tinyint(1) DEFAULT 1 COMMENT \\'1采购2提货3换货\\'');");
    fix.push("CALL add_col_if_missing('eb_stock_order','cancel_time','cancel_time datetime DEFAULT NULL COMMENT \\'取消时间\\'');");
  }
  fix.push("CALL add_col_if_missing('eb_stock_order_product','sku_key','sku_key varchar(64) DEFAULT \\'\\' COMMENT \\'规格标识\\'');");
  if (!hasExchangeAddress) {
    fix.push("CALL add_col_if_missing('eb_stock_exchange','address_id','address_id int(11) NOT NULL DEFAULT 0');");
    fix.push("CALL add_col_if_missing('eb_stock_exchange','real_name','real_name varchar(64) NOT NULL DEFAULT \\'\\'');");
    fix.push("CALL add_col_if_missing('eb_stock_exchange','phone','phone varchar(20) NOT NULL DEFAULT \\'\\'');");
    fix.push("CALL add_col_if_missing('eb_stock_exchange','user_address','user_address varchar(255) NOT NULL DEFAULT \\'\\'');");
  }
  fix.push('DROP PROCEDURE IF EXISTS add_col_if_missing;');
  fix.push('');

  // domain
  fix.push('-- ---------- 2. 域名强制为测试 API ----------');
  fix.push("UPDATE `eb_system_config` SET `value` = 'http://api.qianxutec.com', `update_time` = NOW()");
  fix.push("WHERE `name` IN ('api_url', 'localUploadUrl', 'front_api_url', 'site_url');");
  fix.push('');

  // theme replace
  fix.push('-- ---------- 3. 主题/DIY 本地域名替换 ----------');
  const themeCols = [
    'home_data',
    'category_data',
    'detail_data',
    'user_data',
    'theme_data',
    'home_default_data',
    'category_default_data',
    'detail_default_data',
    'user_default_data',
    'theme_default_data',
  ];
  for (const c of themeCols) {
    fix.push(`UPDATE \`eb_theme\` SET \`${c}\` = REPLACE(\`${c}\`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com') WHERE \`${c}\` LIKE '%127.0.0.1:8080%';`);
    fix.push(`UPDATE \`eb_theme\` SET \`${c}\` = REPLACE(\`${c}\`, 'http://127.0.0.1:8081', 'http://api.qianxutec.com') WHERE \`${c}\` LIKE '%127.0.0.1:8081%';`);
  }
  fix.push("UPDATE `eb_page_diy` SET `value` = REPLACE(`value`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com') WHERE `value` LIKE '%127.0.0.1:8080%';");
  fix.push("UPDATE `eb_page_diy` SET `value` = REPLACE(`value`, 'http://127.0.0.1:8081', 'http://api.qianxutec.com') WHERE `value` LIKE '%127.0.0.1:8081%';");
  fix.push('');

  // dedupe config
  fix.push('-- ---------- 4. 配置去重（同名保留最小 id） ----------');
  fix.push('DELETE t1 FROM `eb_system_config` t1');
  fix.push('INNER JOIN `eb_system_config` t2 ON t1.name = t2.name AND t1.id > t2.id;');
  fix.push('');

  // menu level
  if (!hasLevelMenu) {
    fix.push('-- ---------- 5. 补层级设置菜单 ----------');
    fix.push("INSERT INTO `eb_system_menu` (`id`, `pid`, `name`, `component`, `perms`, `menu_type`, `sort`, `icon`, `is_show`, `is_delte`, `create_time`)");
    fix.push("SELECT 691, 652, '层级设置', '/stock/level', 'admin:stock:level:list', 'C', 5, '', 1, 0, NOW()");
    fix.push('FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_menu` WHERE `id` = 691 OR `component` = \'/stock/level\');');
    fix.push('INSERT INTO `eb_system_role_menu` (`rid`, `menu_id`)');
    fix.push('SELECT 1, 691 FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `eb_system_role_menu` WHERE `rid` = 1 AND `menu_id` = 691);');
    fix.push('');
  }

  // quartz rebuild
  fix.push('-- ---------- 6. Quartz 表重建（空表，启动后按 eb_schedule_job 注册） ----------');
  fix.push(quartzRebuild());
  fix.push('');

  // product attr fix note - generate default sku for products missing attr_value
  if (productsMissingAttrValue.length) {
    fix.push('-- ---------- 7. 为缺少规格的商品补默认规格（避免详情/后台卡死） ----------');
    fix.push('-- 仅处理：无 attr_value 记录的商品；已有规格的不会动');
    const ids = productsMissingAttrValue;
    // do in SQL using NOT EXISTS rather than hardcoding all if too many
    fix.push(`-- 备份中检测到约 ${ids.length} 个商品缺 attr_value`);
    fix.push(productDefaultSkuSql());
    fix.push('');
  }

  fix.push('SET FOREIGN_KEY_CHECKS = 1;');
  fix.push("SELECT 'FIX_TEST_ENV done' AS result;");
  fix.push('-- 执行后：redis-cli -a 密码 -n 8 FLUSHDB；重启 admin + front');

  fs.writeFileSync(fixPath, fix.join('\n'), 'utf8');
  console.log('\nWrote', fixPath);
}

function splitSqlTuple(tuple) {
  // tuple like (1,'a',NULL,'b,c')
  const s = tuple.trim().replace(/^\(/, '').replace(/\)$/, '');
  const out = [];
  let cur = '';
  let inQ = false;
  for (let i = 0; i < s.length; i++) {
    const ch = s[i];
    if (ch === "'" && s[i - 1] !== '\\') {
      inQ = !inQ;
      cur += ch;
      continue;
    }
    if (ch === ',' && !inQ) {
      out.push(cur.trim());
      cur = '';
      continue;
    }
    cur += ch;
  }
  if (cur.length) out.push(cur.trim());
  return out;
}

function addColProc() {
  return `DROP PROCEDURE IF EXISTS add_col_if_missing;
DELIMITER $$
CREATE PROCEDURE add_col_if_missing(IN t varchar(64), IN c varchar(64), IN ddl text)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = t AND COLUMN_NAME = c
  ) THEN
    SET @s = CONCAT('ALTER TABLE ', t, ' ADD COLUMN ', ddl);
    PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;
  END IF;
END$$
DELIMITER ;`;
}

function productDefaultSkuSql() {
  return `-- 补 attr（无规格定义的）
INSERT INTO eb_store_product_attr (product_id, attr_name, attr_values, type, is_del, is_show_image)
SELECT p.id, '规格', '默认', 0, 0, 0
FROM eb_store_product p
WHERE IFNULL(p.is_del,0) = 0
  AND NOT EXISTS (SELECT 1 FROM eb_store_product_attr a WHERE a.product_id = p.id AND IFNULL(a.is_del,0)=0);

-- 补 attr_value（无 SKU 的）
INSERT INTO eb_store_product_attr_value
(product_id, suk, stock, sales, price, image, cost, \`unique\`, bar_code, weight, volume, brokerage, brokerage_two, type, quota, quota_show, attr_value, is_del, ot_price)
SELECT
  p.id,
  '默认',
  IFNULL(p.stock, 0),
  IFNULL(p.sales, 0),
  IFNULL(p.price, 0),
  IFNULL(p.image, ''),
  IFNULL(p.cost, 0),
  CONCAT('auto', p.id),
  '',
  0,
  0,
  0,
  0,
  0,
  0,
  0,
  '{\\\"规格\\\":\\\"默认\\\"}',
  0,
  IFNULL(p.ot_price, 0)
FROM eb_store_product p
WHERE IFNULL(p.is_del,0) = 0
  AND NOT EXISTS (
    SELECT 1 FROM eb_store_product_attr_value v
    WHERE v.product_id = p.id AND IFNULL(v.is_del,0)=0
  );`;
}

function quartzRebuild() {
  return `DROP TABLE IF EXISTS qrtz_blob_triggers;
DROP TABLE IF EXISTS qrtz_cron_triggers;
DROP TABLE IF EXISTS qrtz_simple_triggers;
DROP TABLE IF EXISTS qrtz_simprop_triggers;
DROP TABLE IF EXISTS qrtz_triggers;
DROP TABLE IF EXISTS qrtz_job_details;
DROP TABLE IF EXISTS qrtz_fired_triggers;
DROP TABLE IF EXISTS qrtz_locks;
DROP TABLE IF EXISTS qrtz_paused_trigger_grps;
DROP TABLE IF EXISTS qrtz_scheduler_state;

CREATE TABLE qrtz_job_details (
  SCHED_NAME varchar(120) NOT NULL,
  JOB_NAME varchar(200) NOT NULL,
  JOB_GROUP varchar(200) NOT NULL,
  DESCRIPTION varchar(250) DEFAULT NULL,
  JOB_CLASS_NAME varchar(250) NOT NULL,
  IS_DURABLE varchar(1) NOT NULL,
  IS_NONCONCURRENT varchar(1) NOT NULL,
  IS_UPDATE_DATA varchar(1) NOT NULL,
  REQUESTS_RECOVERY varchar(1) NOT NULL,
  JOB_DATA blob,
  PRIMARY KEY (SCHED_NAME, JOB_NAME, JOB_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE qrtz_triggers (
  SCHED_NAME varchar(120) NOT NULL,
  TRIGGER_NAME varchar(200) NOT NULL,
  TRIGGER_GROUP varchar(200) NOT NULL,
  JOB_NAME varchar(200) NOT NULL,
  JOB_GROUP varchar(200) NOT NULL,
  DESCRIPTION varchar(250) DEFAULT NULL,
  NEXT_FIRE_TIME bigint DEFAULT NULL,
  PREV_FIRE_TIME bigint DEFAULT NULL,
  PRIORITY int DEFAULT NULL,
  TRIGGER_STATE varchar(16) NOT NULL,
  TRIGGER_TYPE varchar(8) NOT NULL,
  START_TIME bigint NOT NULL,
  END_TIME bigint DEFAULT NULL,
  CALENDAR_NAME varchar(200) DEFAULT NULL,
  MISFIRE_INSTR smallint DEFAULT NULL,
  JOB_DATA blob,
  PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
  KEY IDX_QRTZ_T_J (SCHED_NAME, JOB_NAME, JOB_GROUP),
  CONSTRAINT qrtz_triggers_ibfk_1 FOREIGN KEY (SCHED_NAME, JOB_NAME, JOB_GROUP)
    REFERENCES qrtz_job_details (SCHED_NAME, JOB_NAME, JOB_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE qrtz_cron_triggers (
  SCHED_NAME varchar(120) NOT NULL,
  TRIGGER_NAME varchar(200) NOT NULL,
  TRIGGER_GROUP varchar(200) NOT NULL,
  CRON_EXPRESSION varchar(200) NOT NULL,
  TIME_ZONE_ID varchar(80) DEFAULT NULL,
  PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
  CONSTRAINT qrtz_cron_triggers_ibfk_1 FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
    REFERENCES qrtz_triggers (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE qrtz_simple_triggers (
  SCHED_NAME varchar(120) NOT NULL,
  TRIGGER_NAME varchar(200) NOT NULL,
  TRIGGER_GROUP varchar(200) NOT NULL,
  REPEAT_COUNT bigint NOT NULL,
  REPEAT_INTERVAL bigint NOT NULL,
  TIMES_TRIGGERED bigint NOT NULL,
  PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
  CONSTRAINT qrtz_simple_triggers_ibfk_1 FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
    REFERENCES qrtz_triggers (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE qrtz_simprop_triggers (
  SCHED_NAME varchar(120) NOT NULL,
  TRIGGER_NAME varchar(200) NOT NULL,
  TRIGGER_GROUP varchar(200) NOT NULL,
  STR_PROP_1 varchar(512) DEFAULT NULL,
  STR_PROP_2 varchar(512) DEFAULT NULL,
  STR_PROP_3 varchar(512) DEFAULT NULL,
  INT_PROP_1 int DEFAULT NULL,
  INT_PROP_2 int DEFAULT NULL,
  LONG_PROP_1 bigint DEFAULT NULL,
  LONG_PROP_2 bigint DEFAULT NULL,
  DEC_PROP_1 decimal(13,4) DEFAULT NULL,
  DEC_PROP_2 decimal(13,4) DEFAULT NULL,
  BOOL_PROP_1 varchar(1) DEFAULT NULL,
  BOOL_PROP_2 varchar(1) DEFAULT NULL,
  PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
  CONSTRAINT qrtz_simprop_triggers_ibfk_1 FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
    REFERENCES qrtz_triggers (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE qrtz_blob_triggers (
  SCHED_NAME varchar(120) NOT NULL,
  TRIGGER_NAME varchar(200) NOT NULL,
  TRIGGER_GROUP varchar(200) NOT NULL,
  BLOB_DATA blob,
  PRIMARY KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP),
  CONSTRAINT qrtz_blob_triggers_ibfk_1 FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
    REFERENCES qrtz_triggers (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE qrtz_fired_triggers (
  SCHED_NAME varchar(120) NOT NULL,
  ENTRY_ID varchar(95) NOT NULL,
  TRIGGER_NAME varchar(200) NOT NULL,
  TRIGGER_GROUP varchar(200) NOT NULL,
  INSTANCE_NAME varchar(200) NOT NULL,
  FIRED_TIME bigint NOT NULL,
  SCHED_TIME bigint NOT NULL,
  PRIORITY int NOT NULL,
  STATE varchar(16) NOT NULL,
  JOB_NAME varchar(200) DEFAULT NULL,
  JOB_GROUP varchar(200) DEFAULT NULL,
  IS_NONCONCURRENT varchar(1) DEFAULT NULL,
  REQUESTS_RECOVERY varchar(1) DEFAULT NULL,
  PRIMARY KEY (SCHED_NAME, ENTRY_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE qrtz_locks (
  SCHED_NAME varchar(120) NOT NULL,
  LOCK_NAME varchar(40) NOT NULL,
  PRIMARY KEY (SCHED_NAME, LOCK_NAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE qrtz_paused_trigger_grps (
  SCHED_NAME varchar(120) NOT NULL,
  TRIGGER_GROUP varchar(200) NOT NULL,
  PRIMARY KEY (SCHED_NAME, TRIGGER_GROUP)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE qrtz_scheduler_state (
  SCHED_NAME varchar(120) NOT NULL,
  INSTANCE_NAME varchar(200) NOT NULL,
  LAST_CHECKIN_TIME bigint NOT NULL,
  CHECKIN_INTERVAL bigint NOT NULL,
  PRIMARY KEY (SCHED_NAME, INSTANCE_NAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;`;
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
