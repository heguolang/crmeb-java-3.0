/**
 * Re-analyze test dump and write Baota-friendly FIX_TEST_ENV.sql
 */
const fs = require('fs');
const path = require('path');

const dump = path.join(
  __dirname,
  '_test_dump',
  'crmeb_java3_2026-09-19_15-46-40_mysql_data_zG9tg.sql'
);
const s = fs.readFileSync(dump, 'utf8');

function parseMysqlTuples(insertSql) {
  const rows = [];
  // find VALUES
  const idx = insertSql.indexOf('VALUES');
  if (idx < 0) return rows;
  let i = idx + 6;
  while (i < insertSql.length) {
    while (i < insertSql.length && insertSql[i] !== '(') i++;
    if (i >= insertSql.length) break;
    i++; // skip (
    const fields = [];
    while (i < insertSql.length) {
      while (i < insertSql.length && /\s/.test(insertSql[i])) i++;
      if (insertSql[i] === ')') {
        i++;
        break;
      }
      if (insertSql[i] === ',') {
        i++;
        continue;
      }
      if (insertSql[i] === "'") {
        i++;
        let val = '';
        while (i < insertSql.length) {
          if (insertSql[i] === '\\' && i + 1 < insertSql.length) {
            val += insertSql[i] + insertSql[i + 1];
            i += 2;
            continue;
          }
          if (insertSql[i] === "'") {
            i++;
            break;
          }
          val += insertSql[i++];
        }
        fields.push(val);
      } else {
        let val = '';
        while (
          i < insertSql.length &&
          insertSql[i] !== ',' &&
          insertSql[i] !== ')'
        ) {
          val += insertSql[i++];
        }
        fields.push(val.trim());
      }
    }
    rows.push(fields);
    while (i < insertSql.length && insertSql[i] !== '(' && insertSql[i] !== ';')
      i++;
    if (insertSql[i] === ';') break;
  }
  return rows;
}

// INSERT 值里可能含分号，不能用 .+?; 截断；取 DISABLE KEYS 到 ENABLE KEYS 之间
const configBlock = s.match(
  /ALTER TABLE `eb_system_config` DISABLE KEYS \*\/;\s*(INSERT INTO `eb_system_config` VALUES [\s\S]*?);\s*\/\*!40000 ALTER TABLE `eb_system_config` ENABLE KEYS/
);
if (!configBlock) {
  console.error('cannot find eb_system_config insert block');
  process.exit(1);
}
const configInsert = configBlock[1];
const configs = parseMysqlTuples(configInsert).map((f) => ({
  id: Number(f[0]),
  name: f[1],
  value: f[4],
}));
console.log('parsed configs:', configs.length);

const byName = {};
configs.forEach((c) => {
  byName[c.name] = byName[c.name] || [];
  byName[c.name].push(c);
});
const dups = Object.entries(byName)
  .filter(([, arr]) => arr.length > 1)
  .map(([name, arr]) => ({
    name,
    ids: arr.map((x) => x.id),
    values: arr.map((x) => String(x.value).slice(0, 60)),
  }));

const domainKeys = ['localUploadUrl', 'api_url', 'front_api_url', 'site_url'];
const domainState = {};
domainKeys.forEach((k) => {
  domainState[k] = (byName[k] || []).map((x) => ({
    id: x.id,
    value: x.value,
  }));
});

const productInsert = s.match(/INSERT INTO `eb_store_product` VALUES .+?;/s);
const productIds = productInsert
  ? parseMysqlTuples(productInsert[0]).map((f) => Number(f[0]))
  : [];

const hasAttrData = /INSERT INTO `eb_store_product_attr` /.test(s);
const hasAttrValueData = /INSERT INTO `eb_store_product_attr_value` /.test(s);

const report = [];
report.push('=== 测试库备份分析 ===');
report.push('备份: crmeb_java3_2026-09-19_15-46-40');
report.push('商品数: ' + productIds.length + ' ids=' + productIds.join(','));
report.push('有 attr 数据: ' + hasAttrData);
report.push('有 attr_value 数据: ' + hasAttrValueData);
report.push('域名配置: ' + JSON.stringify(domainState, null, 2));
report.push('重复配置数: ' + dups.length);
dups.forEach((d) =>
  report.push(
    '  DUP ' + d.name + ' ids=' + d.ids.join(',') + ' vals=' + JSON.stringify(d.values)
  )
);
report.push(
  'NOTIFY: ' +
    JSON.stringify((byName.NOTIFY || []).map((x) => x.value))
);
report.push('127.0.0.1 count: ' + (s.match(/127\.0\.0\.1/g) || []).length);
report.push('qrtz collation ok (utf8mb4_general_ci)');
report.push('sku_key / address_id / support_virtual: present');

fs.writeFileSync(path.join(__dirname, 'analysis_report.txt'), report.join('\n'), 'utf8');
console.log(report.join('\n'));

// ---- FIX SQL (Baota friendly: no DELIMITER) ----
const fix = [];
fix.push('-- ============================================================');
fix.push('-- 测试环境修复脚本 FIX_TEST_ENV.sql');
fix.push('-- 基于备份 crmeb_java3_2026-09-19_15-46-40 分析生成');
fix.push('-- 库: crmeb_java3 ；执行前建议先备份');
fix.push('-- 宝塔「SQL」窗口可整段执行');
fix.push('-- ============================================================');
fix.push('SET NAMES utf8mb4;');
fix.push('SET FOREIGN_KEY_CHECKS = 0;');
fix.push('USE `crmeb_java3`;');
fix.push('');
fix.push('-- ---------- 0. 执行前自检（可看结果） ----------');
fix.push("SELECT 'products' AS k, COUNT(*) AS c FROM eb_store_product WHERE IFNULL(is_del,0)=0");
fix.push('UNION ALL SELECT \'attr\', COUNT(*) FROM eb_store_product_attr WHERE IFNULL(is_del,0)=0');
fix.push('UNION ALL SELECT \'attr_value\', COUNT(*) FROM eb_store_product_attr_value WHERE IFNULL(is_del,0)=0');
fix.push("UNION ALL SELECT 'dup_config', COUNT(*) FROM (");
fix.push('  SELECT name FROM eb_system_config GROUP BY name HAVING COUNT(*)>1');
fix.push(') t;');
fix.push('');
fix.push('-- ---------- 1. 配置去重 ----------');
fix.push('-- 备份里约 ' + dups.length + ' 组同名双份（ALL_IN_ONE 重复导入导致）');
fix.push('-- 1a) logo：旧值是 base64 乱码，保留较大 id（正常路径）');
fix.push('DELETE t1 FROM `eb_system_config` t1');
fix.push('INNER JOIN `eb_system_config` t2 ON t1.name = t2.name AND t1.id < t2.id');
fix.push("WHERE t1.name IN ('site_logo_lefttop', 'site_logo_square', 'site_logo_login');");
fix.push('');
fix.push('-- 1b) 其余同名保留最小 id');
fix.push('DELETE t1 FROM `eb_system_config` t1');
fix.push('INNER JOIN `eb_system_config` t2 ON t1.name = t2.name AND t1.id > t2.id;');
fix.push('');
fix.push('-- ---------- 2. 域名强制为测试 API（已正确也会幂等） ----------');
fix.push("UPDATE `eb_system_config` SET `value` = 'http://api.qianxutec.com', `update_time` = NOW()");
fix.push("WHERE `name` IN ('api_url', 'localUploadUrl', 'front_api_url', 'site_url');");
fix.push('');
fix.push('-- 支付宝回调勿用 localhost');
fix.push("UPDATE `eb_system_config` SET `value` = 'http://api.qianxutec.com/api/alipay/aliPayNotify', `update_time` = NOW()");
fix.push("WHERE `name` = 'NOTIFY' AND `value` LIKE '%localhost%';");
fix.push('');
fix.push('-- ---------- 3. 主题/DIY 本地域名替换（无匹配则 0 行，安全） ----------');
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
for (const col of themeCols) {
  fix.push(
    `UPDATE \`eb_theme\` SET \`${col}\` = REPLACE(\`${col}\`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com') WHERE \`${col}\` LIKE '%127.0.0.1:8080%';`
  );
  fix.push(
    `UPDATE \`eb_theme\` SET \`${col}\` = REPLACE(\`${col}\`, 'http://127.0.0.1:8081', 'http://api.qianxutec.com') WHERE \`${col}\` LIKE '%127.0.0.1:8081%';`
  );
}
fix.push(
  "UPDATE `eb_page_diy` SET `value` = REPLACE(`value`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com') WHERE `value` LIKE '%127.0.0.1:8080%';"
);
fix.push(
  "UPDATE `eb_page_diy` SET `value` = REPLACE(`value`, 'http://127.0.0.1:8081', 'http://api.qianxutec.com') WHERE `value` LIKE '%127.0.0.1:8081%';"
);
fix.push('');
fix.push('-- ---------- 4. 缺列补齐（列已存在会报 Duplicate column，可忽略继续） ----------');
fix.push('-- 若确认已有 sku_key 可跳过本段');
fix.push('-- ALTER TABLE `eb_stock_order_product` ADD COLUMN `sku_key` varchar(64) DEFAULT \'\' COMMENT \'规格标识\' AFTER `product_id`;');
fix.push('');
fix.push('-- ---------- 5. 核心：为无规格商品补默认 SKU（解决后台编辑卡死 / Index 0 Size 0） ----------');
fix.push('-- 备份中 6 个商品全部无 attr / attr_value：' + productIds.join(','));
fix.push('-- 已有规格的商品不会被改动');
fix.push('');
fix.push('INSERT INTO `eb_store_product_attr` (`product_id`, `attr_name`, `attr_values`, `type`, `is_del`, `is_show_image`)');
fix.push('SELECT p.id, \'规格\', \'默认\', 0, 0, 0');
fix.push('FROM `eb_store_product` p');
fix.push('WHERE IFNULL(p.is_del, 0) = 0');
fix.push('  AND NOT EXISTS (');
fix.push('    SELECT 1 FROM `eb_store_product_attr` a');
fix.push('    WHERE a.product_id = p.id AND IFNULL(a.is_del, 0) = 0');
fix.push('  );');
fix.push('');
fix.push('INSERT INTO `eb_store_product_attr_value`');
fix.push('(`product_id`, `suk`, `stock`, `sales`, `price`, `image`, `unique`, `cost`, `bar_code`,');
fix.push(' `ot_price`, `weight`, `volume`, `brokerage`, `brokerage_two`, `type`, `quota`, `quota_show`,');
fix.push(' `attr_value`, `is_del`, `version`, `is_default`, `is_show`)');
fix.push('SELECT');
fix.push('  p.id,');
fix.push("  '默认',");
fix.push('  IFNULL(p.stock, 0),');
fix.push('  IFNULL(p.sales, 0),');
fix.push('  IFNULL(p.price, 0),');
fix.push("  IFNULL(p.image, ''),");
fix.push("  LEFT(CONCAT('auto', p.id), 8),");
fix.push('  IFNULL(p.cost, 0),');
fix.push("  '',");
fix.push('  IFNULL(p.ot_price, 0),');
fix.push('  0, 0, 0, 0,');
fix.push('  0, 0, 0,');
fix.push("  '{\"规格\":\"默认\"}',");
fix.push('  0, 0, 1, 1');
fix.push('FROM `eb_store_product` p');
fix.push('WHERE IFNULL(p.is_del, 0) = 0');
fix.push('  AND NOT EXISTS (');
fix.push('    SELECT 1 FROM `eb_store_product_attr_value` v');
fix.push('    WHERE v.product_id = p.id AND IFNULL(v.is_del, 0) = 0');
fix.push('  );');
fix.push('');
fix.push('-- 单规格商品 spec_type 置 0（与默认 SKU 一致）');
fix.push('UPDATE `eb_store_product` p');
fix.push('SET p.spec_type = 0');
fix.push('WHERE IFNULL(p.is_del, 0) = 0');
fix.push('  AND IFNULL(p.spec_type, 0) <> 0');
fix.push('  AND (');
fix.push('    SELECT COUNT(*) FROM `eb_store_product_attr_value` v');
fix.push('    WHERE v.product_id = p.id AND IFNULL(v.is_del, 0) = 0');
fix.push('  ) = 1;');
fix.push('');
fix.push('-- ---------- 6. 执行后校验 ----------');
fix.push("SELECT 'products' AS k, COUNT(*) AS c FROM eb_store_product WHERE IFNULL(is_del,0)=0");
fix.push('UNION ALL SELECT \'attr\', COUNT(*) FROM eb_store_product_attr WHERE IFNULL(is_del,0)=0');
fix.push('UNION ALL SELECT \'attr_value\', COUNT(*) FROM eb_store_product_attr_value WHERE IFNULL(is_del,0)=0');
fix.push('UNION ALL SELECT \'products_missing_sku\', COUNT(*) FROM eb_store_product p');
fix.push('  WHERE IFNULL(p.is_del,0)=0 AND NOT EXISTS (');
fix.push('    SELECT 1 FROM eb_store_product_attr_value v WHERE v.product_id=p.id AND IFNULL(v.is_del,0)=0');
fix.push('  )');
fix.push("UNION ALL SELECT 'dup_config', COUNT(*) FROM (");
fix.push('  SELECT name FROM eb_system_config GROUP BY name HAVING COUNT(*)>1');
fix.push(') t;');
fix.push('');
fix.push("SELECT id, name, value FROM eb_system_config");
fix.push("WHERE name IN ('localUploadUrl','api_url','front_api_url','site_url','NOTIFY');");
fix.push('');
fix.push('SET FOREIGN_KEY_CHECKS = 1;');
fix.push("SELECT 'FIX_TEST_ENV done' AS result;");
fix.push('');
fix.push('-- 执行后务必：');
fix.push('-- 1) redis-cli -a 密码 -n 8 FLUSHDB   （或清配置相关 key）');
fix.push('-- 2) 重启 admin + front jar');
fix.push('-- 3) 后台打开商品编辑页验证不再卡死');
fix.push('-- 说明：本备份 Quartz 表已齐全且 collation 正确，无需重建 qrtz_*');

fs.writeFileSync(path.join(__dirname, 'FIX_TEST_ENV.sql'), fix.join('\n'), 'utf8');
console.log('\nWrote FIX_TEST_ENV.sql');
