package com.qxkj.admin.task.log;

import cn.hutool.core.util.StrUtil;
import com.qxkj.common.utils.QianxuDateUtil;
import com.qxkj.service.service.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 非业务日志归档：将超过保留天数的日志迁入 *_archive 表，减轻主表体积。
 * 建议 cron: 0 20 3 * * ? （每天 03:20）
 * 配置项（可选）log_archive_keep_days，默认 30。
 */
@Component("ArchiveLogTask")
public class ArchiveLogTask {

    private static final Logger logger = LoggerFactory.getLogger(ArchiveLogTask.class);
    private static final String CONFIG_KEEP_DAYS = "log_archive_keep_days";
    private static final int DEFAULT_KEEP_DAYS = 30;
    private static final int BATCH = 2000;
    private static final int MAX_ROUNDS = 200;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * 归档一个月以前的运维/审计类日志（不影响订单、资金、佣金主数据）。
     */
    public void archiveOldLogs() {
        logger.info("---ArchiveLogTask start--- {}", QianxuDateUtil.nowDateTime());
        int keepDays = resolveKeepDays();
        int totalMoved = 0;
        try {
            // datetime 列 create_time
            totalMoved += archiveByDatetime("eb_exception_log", "create_time", keepDays);
            totalMoved += archiveByDatetime("eb_admin_login_log", "create_time", keepDays);
            totalMoved += archiveByDatetime("eb_sensitive_method_log", "create_time", keepDays);
            totalMoved += archiveByDatetime("eb_schedule_job_log", "create_time", keepDays);
            totalMoved += archiveByDatetime("eb_wechat_exceptions", "create_time", keepDays);
            totalMoved += archiveByDatetime("eb_sms_record", "create_time", keepDays);
            totalMoved += archiveByDatetime("eb_wechat_callback", "create_time", keepDays);
            totalMoved += archiveByDatetime("eb_chain_audit_log", "create_time", keepDays);
            totalMoved += archiveByDatetime("eb_chain_risk_log", "create_time", keepDays);
            totalMoved += archiveByDatetime("eb_chain_notify_log", "create_time", keepDays);
            totalMoved += archiveByDatetime("eb_stock_log", "create_time", keepDays);
            totalMoved += archiveByDatetime("eb_stock_change_log", "create_time", keepDays);
            totalMoved += archiveByDatetime("eb_stock_adjust_log", "create_time", keepDays);
            totalMoved += archiveByDatetime("eb_agent_change_log", "create_time", keepDays);
            // chain slide 用 created_at
            totalMoved += archiveByDatetime("eb_chain_slide_log", "created_at", keepDays);
            // 商品行为日志：unix 秒
            totalMoved += archiveByUnixSeconds("eb_store_product_log", "add_time", keepDays);
            // 访问记录：date 字符串 yyyy-MM-dd
            totalMoved += archiveByDateStr("eb_user_visit_record", "date", keepDays);

            logger.info("---ArchiveLogTask done--- keepDays={}, movedRows={}", keepDays, totalMoved);
        } catch (Exception e) {
            logger.error("ArchiveLogTask error: {}", e.getMessage(), e);
        }
    }

    private int resolveKeepDays() {
        try {
            String v = systemConfigService.getValueByKey(CONFIG_KEEP_DAYS);
            if (StrUtil.isNotBlank(v) && v.matches("\\d+")) {
                int n = Integer.parseInt(v);
                if (n >= 7 && n <= 3650) {
                    return n;
                }
            }
        } catch (Exception ignore) {
        }
        return DEFAULT_KEEP_DAYS;
    }

    private int archiveByDatetime(String table, String timeCol, int keepDays) {
        if (!tableExists(table) || !columnExists(table, timeCol) || !columnExists(table, "id")) {
            return 0;
        }
        ensureArchiveTable(table);
        String where = timeCol + " < DATE_SUB(NOW(), INTERVAL ? DAY)";
        return moveBatches(table, where, keepDays);
    }

    private int archiveByUnixSeconds(String table, String timeCol, int keepDays) {
        if (!tableExists(table) || !columnExists(table, timeCol) || !columnExists(table, "id")) {
            return 0;
        }
        ensureArchiveTable(table);
        String where = timeCol + " > 0 AND " + timeCol + " < UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL ? DAY))";
        return moveBatches(table, where, keepDays);
    }

    private int archiveByDateStr(String table, String timeCol, int keepDays) {
        if (!tableExists(table) || !columnExists(table, timeCol) || !columnExists(table, "id")) {
            return 0;
        }
        ensureArchiveTable(table);
        String where = timeCol + " < DATE_FORMAT(DATE_SUB(NOW(), INTERVAL ? DAY), '%Y-%m-%d')";
        return moveBatches(table, where, keepDays);
    }

    private int moveBatches(String table, String whereSql, int keepDays) {
        String archive = table + "_archive";
        int moved = 0;
        for (int round = 0; round < MAX_ROUNDS; round++) {
            List<Integer> ids = jdbcTemplate.query(
                    "SELECT id FROM " + table + " WHERE " + whereSql + " ORDER BY id ASC LIMIT " + BATCH,
                    (rs, i) -> rs.getInt(1),
                    keepDays);
            if (ids == null || ids.isEmpty()) {
                break;
            }
            String in = joinIds(ids);
            int inserted = jdbcTemplate.update(
                    "INSERT IGNORE INTO " + archive + " SELECT * FROM " + table + " WHERE id IN (" + in + ")");
            int deleted = jdbcTemplate.update("DELETE FROM " + table + " WHERE id IN (" + in + ")");
            moved += deleted;
            logger.info("ArchiveLogTask {} -> {} batch insert={} delete={}", table, archive, inserted, deleted);
            if (ids.size() < BATCH) {
                break;
            }
        }
        return moved;
    }

    private void ensureArchiveTable(String table) {
        String archive = table + "_archive";
        if (tableExists(archive)) {
            return;
        }
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS `" + archive + "` LIKE `" + table + "`");
        logger.info("ArchiveLogTask created archive table {}", archive);
    }

    private boolean tableExists(String table) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                Integer.class, table);
        return n != null && n > 0;
    }

    private boolean columnExists(String table, String column) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class, table, column);
        return n != null && n > 0;
    }

    private String joinIds(List<Integer> ids) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(ids.get(i));
        }
        return sb.toString();
    }
}
