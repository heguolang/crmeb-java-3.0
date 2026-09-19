/**
 * Build ALL_IN_ONE_TEST.sql from ALL_IN_ONE.sql for test/prod deploy.
 * Fixes:
 *  - utf8mb4_0900_ai_ci -> utf8mb4_general_ci (FK compatible with DB)
 *  - http://127.0.0.1:8080 -> http://api.qianxutec.com (theme baked URLs)
 *  - skip qrtz_* binary INSERT (Baota/phpMyAdmin breaks on JOB_DATA); tables kept empty, jobs re-register from eb_schedule_job
 *  - append dedupe sys_switch_* + domain force + quartz ensure
 */
const fs = require('fs');
const path = require('path');
const readline = require('readline');

const dir = __dirname;
const src = path.join(dir, 'ALL_IN_ONE.sql');
const dest = path.join(dir, 'ALL_IN_ONE_TEST.sql');

const HEADER = `-- ============================================================
-- CRMEB Java 3.0  测试/生产一键库脚本：ALL_IN_ONE_TEST.sql
-- 由 ALL_IN_ONE.sql 自动整理生成，可直接用于测试环境 crmeb_java3
--
-- 相对原版已处理：
--   1) 全库统一 utf8mb4_general_ci（避免 Quartz 外键 3780）
--   2) 主题等数据中的 http://127.0.0.1:8080 替换为 http://api.qianxutec.com
--   3) 去掉 qrtz_job_details 等含二进制 JOB_DATA 的 INSERT（避免宝塔导入截断）
--      Quartz 表结构保留为空，启动 admin 后会按 eb_schedule_job 重新注册
--   4) 末尾强制域名 + sys_switch_* 去重
--
-- 【重要】请用命令行导入，不要用宝塔网页导入大文件：
--   mysql -uroot -p密码 --default-character-set=utf8mb4 --max_allowed_packet=512M < ALL_IN_ONE_TEST.sql
--
-- 适用：全新测试库 / 可清空重建的测试库（会 DROP 同名表！）
-- 已有生产业务数据：不要执行本文件，只执行 02_patches_all.sql
--
-- 执行后：重启 admin + front；Redis database 8 执行 FLUSHDB
-- ============================================================

`;

const FOOTER = `

-- ========== TEST ENV FINAL FIX ==========
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 域名（再写一次，防止 PART3 中途失败）
UPDATE \`eb_system_config\` SET \`value\` = 'http://api.qianxutec.com', \`update_time\` = NOW()
WHERE \`name\` IN ('api_url', 'localUploadUrl', 'front_api_url', 'site_url');

-- 主题/DIY 残留本地域名
UPDATE \`eb_theme\` SET
  \`home_data\` = REPLACE(\`home_data\`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com')
WHERE \`home_data\` LIKE '%127.0.0.1:8080%';
UPDATE \`eb_theme\` SET
  \`category_data\` = REPLACE(\`category_data\`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com')
WHERE \`category_data\` LIKE '%127.0.0.1:8080%';
UPDATE \`eb_theme\` SET
  \`detail_data\` = REPLACE(\`detail_data\`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com')
WHERE \`detail_data\` LIKE '%127.0.0.1:8080%';
UPDATE \`eb_theme\` SET
  \`user_data\` = REPLACE(\`user_data\`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com')
WHERE \`user_data\` LIKE '%127.0.0.1:8080%';
UPDATE \`eb_theme\` SET
  \`theme_data\` = REPLACE(\`theme_data\`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com')
WHERE \`theme_data\` LIKE '%127.0.0.1:8080%';
UPDATE \`eb_theme\` SET
  \`home_default_data\` = REPLACE(\`home_default_data\`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com')
WHERE \`home_default_data\` LIKE '%127.0.0.1:8080%';
UPDATE \`eb_theme\` SET
  \`category_default_data\` = REPLACE(\`category_default_data\`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com')
WHERE \`category_default_data\` LIKE '%127.0.0.1:8080%';
UPDATE \`eb_theme\` SET
  \`detail_default_data\` = REPLACE(\`detail_default_data\`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com')
WHERE \`detail_default_data\` LIKE '%127.0.0.1:8080%';
UPDATE \`eb_theme\` SET
  \`user_default_data\` = REPLACE(\`user_default_data\`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com')
WHERE \`user_default_data\` LIKE '%127.0.0.1:8080%';
UPDATE \`eb_theme\` SET
  \`theme_default_data\` = REPLACE(\`theme_default_data\`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com')
WHERE \`theme_default_data\` LIKE '%127.0.0.1:8080%';
UPDATE \`eb_page_diy\` SET
  \`value\` = REPLACE(\`value\`, 'http://127.0.0.1:8080', 'http://api.qianxutec.com')
WHERE \`value\` LIKE '%127.0.0.1:8080%';

-- 隐藏面板开关去重（同名只留 id 最小的一条）
DELETE t1 FROM \`eb_system_config\` t1
INNER JOIN \`eb_system_config\` t2
  ON t1.name = t2.name AND t1.id > t2.id
WHERE t1.name LIKE 'sys_switch_%';

-- 确保 Quartz 基础表存在且排序规则一致（空表即可，任务从 eb_schedule_job 注册）
DROP TABLE IF EXISTS \`qrtz_blob_triggers\`;
DROP TABLE IF EXISTS \`qrtz_cron_triggers\`;
DROP TABLE IF EXISTS \`qrtz_simple_triggers\`;
DROP TABLE IF EXISTS \`qrtz_simprop_triggers\`;
DROP TABLE IF EXISTS \`qrtz_triggers\`;
DROP TABLE IF EXISTS \`qrtz_job_details\`;
DROP TABLE IF EXISTS \`qrtz_fired_triggers\`;
DROP TABLE IF EXISTS \`qrtz_locks\`;
DROP TABLE IF EXISTS \`qrtz_paused_trigger_grps\`;
DROP TABLE IF EXISTS \`qrtz_scheduler_state\`;

CREATE TABLE \`qrtz_job_details\` (
  \`SCHED_NAME\` varchar(120) NOT NULL,
  \`JOB_NAME\` varchar(200) NOT NULL,
  \`JOB_GROUP\` varchar(200) NOT NULL,
  \`DESCRIPTION\` varchar(250) DEFAULT NULL,
  \`JOB_CLASS_NAME\` varchar(250) NOT NULL,
  \`IS_DURABLE\` varchar(1) NOT NULL,
  \`IS_NONCONCURRENT\` varchar(1) NOT NULL,
  \`IS_UPDATE_DATA\` varchar(1) NOT NULL,
  \`REQUESTS_RECOVERY\` varchar(1) NOT NULL,
  \`JOB_DATA\` blob,
  PRIMARY KEY (\`SCHED_NAME\`,\`JOB_NAME\`,\`JOB_GROUP\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE \`qrtz_triggers\` (
  \`SCHED_NAME\` varchar(120) NOT NULL,
  \`TRIGGER_NAME\` varchar(200) NOT NULL,
  \`TRIGGER_GROUP\` varchar(200) NOT NULL,
  \`JOB_NAME\` varchar(200) NOT NULL,
  \`JOB_GROUP\` varchar(200) NOT NULL,
  \`DESCRIPTION\` varchar(250) DEFAULT NULL,
  \`NEXT_FIRE_TIME\` bigint DEFAULT NULL,
  \`PREV_FIRE_TIME\` bigint DEFAULT NULL,
  \`PRIORITY\` int DEFAULT NULL,
  \`TRIGGER_STATE\` varchar(16) NOT NULL,
  \`TRIGGER_TYPE\` varchar(8) NOT NULL,
  \`START_TIME\` bigint NOT NULL,
  \`END_TIME\` bigint DEFAULT NULL,
  \`CALENDAR_NAME\` varchar(200) DEFAULT NULL,
  \`MISFIRE_INSTR\` smallint DEFAULT NULL,
  \`JOB_DATA\` blob,
  PRIMARY KEY (\`SCHED_NAME\`,\`TRIGGER_NAME\`,\`TRIGGER_GROUP\`),
  KEY \`IDX_QRTZ_T_J\` (\`SCHED_NAME\`,\`JOB_NAME\`,\`JOB_GROUP\`),
  CONSTRAINT \`qrtz_triggers_ibfk_1\` FOREIGN KEY (\`SCHED_NAME\`, \`JOB_NAME\`, \`JOB_GROUP\`)
    REFERENCES \`qrtz_job_details\` (\`SCHED_NAME\`, \`JOB_NAME\`, \`JOB_GROUP\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE \`qrtz_cron_triggers\` (
  \`SCHED_NAME\` varchar(120) NOT NULL,
  \`TRIGGER_NAME\` varchar(200) NOT NULL,
  \`TRIGGER_GROUP\` varchar(200) NOT NULL,
  \`CRON_EXPRESSION\` varchar(200) NOT NULL,
  \`TIME_ZONE_ID\` varchar(80) DEFAULT NULL,
  PRIMARY KEY (\`SCHED_NAME\`,\`TRIGGER_NAME\`,\`TRIGGER_GROUP\`),
  CONSTRAINT \`qrtz_cron_triggers_ibfk_1\` FOREIGN KEY (\`SCHED_NAME\`, \`TRIGGER_NAME\`, \`TRIGGER_GROUP\`)
    REFERENCES \`qrtz_triggers\` (\`SCHED_NAME\`, \`TRIGGER_NAME\`, \`TRIGGER_GROUP\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE \`qrtz_simple_triggers\` (
  \`SCHED_NAME\` varchar(120) NOT NULL,
  \`TRIGGER_NAME\` varchar(200) NOT NULL,
  \`TRIGGER_GROUP\` varchar(200) NOT NULL,
  \`REPEAT_COUNT\` bigint NOT NULL,
  \`REPEAT_INTERVAL\` bigint NOT NULL,
  \`TIMES_TRIGGERED\` bigint NOT NULL,
  PRIMARY KEY (\`SCHED_NAME\`,\`TRIGGER_NAME\`,\`TRIGGER_GROUP\`),
  CONSTRAINT \`qrtz_simple_triggers_ibfk_1\` FOREIGN KEY (\`SCHED_NAME\`, \`TRIGGER_NAME\`, \`TRIGGER_GROUP\`)
    REFERENCES \`qrtz_triggers\` (\`SCHED_NAME\`, \`TRIGGER_NAME\`, \`TRIGGER_GROUP\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE \`qrtz_simprop_triggers\` (
  \`SCHED_NAME\` varchar(120) NOT NULL,
  \`TRIGGER_NAME\` varchar(200) NOT NULL,
  \`TRIGGER_GROUP\` varchar(200) NOT NULL,
  \`STR_PROP_1\` varchar(512) DEFAULT NULL,
  \`STR_PROP_2\` varchar(512) DEFAULT NULL,
  \`STR_PROP_3\` varchar(512) DEFAULT NULL,
  \`INT_PROP_1\` int DEFAULT NULL,
  \`INT_PROP_2\` int DEFAULT NULL,
  \`LONG_PROP_1\` bigint DEFAULT NULL,
  \`LONG_PROP_2\` bigint DEFAULT NULL,
  \`DEC_PROP_1\` decimal(13,4) DEFAULT NULL,
  \`DEC_PROP_2\` decimal(13,4) DEFAULT NULL,
  \`BOOL_PROP_1\` varchar(1) DEFAULT NULL,
  \`BOOL_PROP_2\` varchar(1) DEFAULT NULL,
  PRIMARY KEY (\`SCHED_NAME\`,\`TRIGGER_NAME\`,\`TRIGGER_GROUP\`),
  CONSTRAINT \`qrtz_simprop_triggers_ibfk_1\` FOREIGN KEY (\`SCHED_NAME\`, \`TRIGGER_NAME\`, \`TRIGGER_GROUP\`)
    REFERENCES \`qrtz_triggers\` (\`SCHED_NAME\`, \`TRIGGER_NAME\`, \`TRIGGER_GROUP\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE \`qrtz_blob_triggers\` (
  \`SCHED_NAME\` varchar(120) NOT NULL,
  \`TRIGGER_NAME\` varchar(200) NOT NULL,
  \`TRIGGER_GROUP\` varchar(200) NOT NULL,
  \`BLOB_DATA\` blob,
  PRIMARY KEY (\`SCHED_NAME\`,\`TRIGGER_NAME\`,\`TRIGGER_GROUP\`),
  CONSTRAINT \`qrtz_blob_triggers_ibfk_1\` FOREIGN KEY (\`SCHED_NAME\`, \`TRIGGER_NAME\`, \`TRIGGER_GROUP\`)
    REFERENCES \`qrtz_triggers\` (\`SCHED_NAME\`, \`TRIGGER_NAME\`, \`TRIGGER_GROUP\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE \`qrtz_fired_triggers\` (
  \`SCHED_NAME\` varchar(120) NOT NULL,
  \`ENTRY_ID\` varchar(95) NOT NULL,
  \`TRIGGER_NAME\` varchar(200) NOT NULL,
  \`TRIGGER_GROUP\` varchar(200) NOT NULL,
  \`INSTANCE_NAME\` varchar(200) NOT NULL,
  \`FIRED_TIME\` bigint NOT NULL,
  \`SCHED_TIME\` bigint NOT NULL,
  \`PRIORITY\` int NOT NULL,
  \`STATE\` varchar(16) NOT NULL,
  \`JOB_NAME\` varchar(200) DEFAULT NULL,
  \`JOB_GROUP\` varchar(200) DEFAULT NULL,
  \`IS_NONCONCURRENT\` varchar(1) DEFAULT NULL,
  \`REQUESTS_RECOVERY\` varchar(1) DEFAULT NULL,
  PRIMARY KEY (\`SCHED_NAME\`,\`ENTRY_ID\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE \`qrtz_locks\` (
  \`SCHED_NAME\` varchar(120) NOT NULL,
  \`LOCK_NAME\` varchar(40) NOT NULL,
  PRIMARY KEY (\`SCHED_NAME\`,\`LOCK_NAME\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE \`qrtz_paused_trigger_grps\` (
  \`SCHED_NAME\` varchar(120) NOT NULL,
  \`TRIGGER_GROUP\` varchar(200) NOT NULL,
  PRIMARY KEY (\`SCHED_NAME\`,\`TRIGGER_GROUP\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE \`qrtz_scheduler_state\` (
  \`SCHED_NAME\` varchar(120) NOT NULL,
  \`INSTANCE_NAME\` varchar(200) NOT NULL,
  \`LAST_CHECKIN_TIME\` bigint NOT NULL,
  \`CHECKIN_INTERVAL\` bigint NOT NULL,
  PRIMARY KEY (\`SCHED_NAME\`,\`INSTANCE_NAME\`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;
SELECT 'CRMEB ALL_IN_ONE_TEST ready' AS result;
`;

function transformLine(line) {
  let s = line;
  // skip original header block lines that conflict — keep body
  s = s.split('utf8mb4_0900_ai_ci').join('utf8mb4_general_ci');
  s = s.split('http://127.0.0.1:8080').join('http://api.qianxutec.com');
  s = s.split('http://127.0.0.1:8081').join('http://api.qianxutec.com');
  s = s.split('https://127.0.0.1:8080').join('http://api.qianxutec.com');
  return s;
}

function shouldSkipInsert(line) {
  // Skip binary-heavy quartz job data inserts (Baota truncates on NUL bytes)
  if (/^INSERT INTO `qrtz_job_details`/i.test(line)) return true;
  // cron inserts are fine (no binary) — keep them, but final footer rebuilds empty quartz anyway
  // If we rebuild quartz at end empty, cron inserts earlier will fail if tables dropped mid-file...
  // Strategy: keep CREATE for qrtz in body (with general_ci), skip ONLY job_details INSERT.
  // Cron INSERT needs job_details FK parent rows — so either skip cron inserts too, or keep job inserts without binary.
  // Simplest: skip ALL qrtz_* INSERT lines; structure CREATE remains; FOOTER recreates clean empty tables.
  if (/^INSERT INTO `qrtz_/i.test(line)) return true;
  return false;
}

async function main() {
  if (!fs.existsSync(src)) {
    console.error('Missing', src);
    process.exit(1);
  }
  const out = fs.createWriteStream(dest, { encoding: 'utf8' });
  out.write(HEADER);

  const rl = readline.createInterface({
    input: fs.createReadStream(src, { encoding: 'utf8' }),
    crlfDelay: Infinity
  });

  let skippedInserts = 0;
  let lineNo = 0;
  let skipOriginalHeader = true;

  for await (const line of rl) {
    lineNo++;
    // Drop the original top comment banner until first real SQL we care about,
    // but keep from SET NAMES / CREATE DATABASE onward.
    if (skipOriginalHeader) {
      if (line.startsWith('SET NAMES') || line.startsWith('CREATE DATABASE') || line.startsWith('-- ========== PART 1')) {
        skipOriginalHeader = false;
      } else if (lineNo < 30 && (line.startsWith('--') || line.trim() === '')) {
        continue;
      } else {
        skipOriginalHeader = false;
      }
    }

    if (shouldSkipInsert(line)) {
      skippedInserts++;
      continue;
    }

    out.write(transformLine(line));
    out.write('\n');
  }

  out.write(FOOTER);
  await new Promise((resolve, reject) => {
    out.end(() => resolve());
    out.on('error', reject);
  });

  const st = fs.statSync(dest);
  console.log('Wrote', dest);
  console.log('Size MB', (st.size / 1024 / 1024).toFixed(2));
  console.log('Skipped qrtz INSERT lines', skippedInserts);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
