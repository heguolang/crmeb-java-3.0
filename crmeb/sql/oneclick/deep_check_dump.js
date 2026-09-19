const fs = require('fs');
const path = require('path');

const dump = path.join(
  __dirname,
  '_test_dump',
  'crmeb_java3_2026-09-19_15-46-40_mysql_data_zG9tg.sql'
);
const s = fs.readFileSync(dump, 'utf8');

const keys = [
  'localUploadUrl',
  'api_url',
  'front_api_url',
  'site_url',
  'image_upload_type',
  'uploadUrl',
  'frontDomain',
  'adminDomain',
  'siteDomain',
];
const m = s.match(/INSERT INTO `eb_system_config` VALUES .+?;/s);
if (!m) {
  console.log('no config insert');
  process.exit(0);
}
const line = m[0];
const re = /\((\d+),'([^']*)','[^']*',\d+,'((?:\\'|[^'])*)'/g;
const hit = [];
let x;
while ((x = re.exec(line))) {
  const name = x[2];
  const val = x[3].replace(/\\'/g, "'");
  if (
    keys.some((k) => name === k || name.includes(k)) ||
    /127\.0\.0\.1|localhost|https?:\/\//i.test(val)
  ) {
    hit.push({ id: x[1], name, val: val.slice(0, 160) });
  }
}
console.log('=== URL/domain related configs ===');
hit.forEach((h) => console.log(JSON.stringify(h)));

console.log(
  '\nhas product_attr insert',
  /INSERT INTO `eb_store_product_attr` /.test(s)
);
console.log(
  'has product_attr_value insert',
  /INSERT INTO `eb_store_product_attr_value` /.test(s)
);
console.log(
  'product_attr LOCK empty?',
  /LOCK TABLES `eb_store_product_attr` WRITE;\s*\/\*!40000 ALTER TABLE `eb_store_product_attr` DISABLE KEYS \*\/;\s*\/\*!40000 ALTER TABLE `eb_store_product_attr` ENABLE KEYS \*\//.test(
    s
  )
);

const pm = s.match(/INSERT INTO `eb_store_product` VALUES .+?;/s);
if (pm) {
  const ids = [...pm[0].matchAll(/\((\d+),/g)].map((y) => y[1]);
  console.log('product ids', ids.join(','));
  // extract name-ish: second string field after image fields is hard; print price positions
  const rows = [...pm[0].matchAll(/\((\d+),0,'([^']*)'/g)];
  rows.forEach((r) => console.log(' product', r[1], 'image=', r[2].slice(0, 80)));
}

console.log('\nsku_key present', /`sku_key`/.test(s));
console.log('support_virtual present', /`support_virtual`/.test(s));
console.log('support_physical present', /`support_physical`/.test(s));

const so = s.match(/CREATE TABLE `eb_stock_order`[\s\S]*?ENGINE=/);
console.log(
  'eb_stock_order has address_id',
  so ? so[0].includes('address_id') : 'N/A'
);
const se = s.match(/CREATE TABLE `eb_stock_exchange`[\s\S]*?ENGINE=/);
console.log(
  'eb_stock_exchange has address_id',
  se ? se[0].includes('address_id') : 'N/A'
);

const q = s.match(/CREATE TABLE `qrtz_job_details`[\s\S]*?ENGINE=[^;]+;/);
console.log('\nqrtz_job_details DDL tail:\n', q ? q[0].slice(-180) : 'none');

console.log('\n127.0.0.1 count', (s.match(/127\.0\.0\.1/g) || []).length);
console.log('localhost count', (s.match(/localhost/gi) || []).length);

// menu stock level
console.log(
  'menu /stock/level',
  /\/stock\/level/.test(s)
);
console.log('menu 库存等级|库存级别', /库存等/.test(s));

// duplicate config names
const names = [];
const re2 = /\((\d+),'([^']*)'/g;
while ((x = re2.exec(line))) names.push(x[2]);
const cnt = {};
names.forEach((n) => {
  cnt[n] = (cnt[n] || 0) + 1;
});
const dups = Object.entries(cnt).filter(([, c]) => c > 1);
console.log('duplicate config names', dups.length, dups.slice(0, 20));

// check attr_value table structure for required cols
const av = s.match(/CREATE TABLE `eb_store_product_attr_value`[\s\S]*?ENGINE=/);
if (av) {
  console.log('\nattr_value columns sample keys:');
  ['suk', 'unique', 'attr_value', 'ot_price', 'is_del', 'image', 'cost'].forEach(
    (c) => console.log(' ', c, av[0].includes('`' + c + '`'))
  );
}

// check if product spec_type / is_sub issues
const pddl = s.match(/CREATE TABLE `eb_store_product`[\s\S]*?ENGINE=/);
if (pddl) {
  ['spec_type', 'is_sub', 'support_virtual', 'support_physical'].forEach((c) =>
    console.log('product.' + c, pddl[0].includes('`' + c + '`'))
  );
}

// schedule jobs
console.log(
  '\nhas eb_schedule_job insert',
  /INSERT INTO `eb_schedule_job` /.test(s)
);
const sj = s.match(/INSERT INTO `eb_schedule_job` VALUES .+?;/s);
if (sj) console.log('schedule_job snippet', sj[0].slice(0, 400));

const critical = [
  'localUploadUrl',
  'api_url',
  'front_api_url',
  'site_url',
  'sys_switch_daily',
  'sys_switch_integral',
  'sys_switch_combination',
  'image_upload_type',
];
console.log('\n=== critical configs ===');
for (const n of critical) {
  const re = new RegExp(
    "\\((\\d+),'" + n + "','[^']*',\\d+,'((?:\\\\'|[^'])*)'"
  );
  const x = line.match(re);
  console.log(
    n,
    x ? 'id=' + x[1] + ' val=' + x[2].slice(0, 100) : 'MISSING'
  );
}

// product support_* on product table vs stock
const stockProd = s.match(/CREATE TABLE `eb_stock_product`[\s\S]*?ENGINE=/);
if (stockProd) {
  console.log(
    '\nstock_product support_virtual',
    stockProd[0].includes('support_virtual')
  );
}

// check whether FIX needs INSERT for missing api configs
const needInsertDomain = critical
  .slice(0, 4)
  .filter((n) => !new RegExp("\\(\\d+,'" + n + "'").test(line));
console.log('missing domain configs to INSERT:', needInsertDomain);

fs.writeFileSync(
  path.join(__dirname, 'analysis_report.txt'),
  [
    'Deep check ' + new Date().toISOString(),
    'config hits: ' + hit.length,
    ...hit.map((h) => JSON.stringify(h)),
    'products missing attr: ALL (no inserts) ids=101,159,167,168,170,171',
    '127 count: ' + (s.match(/127\.0\.0\.1/g) || []).length,
    'localhost count: ' + (s.match(/localhost/gi) || []).length,
    'NOTIFY has localhost:8080',
    'dups: ' + JSON.stringify(dups),
    'critical:',
    ...critical.map((n) => {
      const re = new RegExp(
        "\\((\\d+),'" + n + "','[^']*',\\d+,'((?:\\\\'|[^'])*)'"
      );
      const x = line.match(re);
      return n + ': ' + (x ? x[1] + '=' + x[2] : 'MISSING');
    }),
  ].join('\n'),
  'utf8'
);
console.log('\nwrote analysis_report.txt');
