-- ============================================================
-- 需求6：管理员登录日志 + 操作日志（菜单/权限）
-- 幂等：可重复执行
-- ============================================================

-- ---------- 1. 管理员登录日志表 ----------
CREATE TABLE IF NOT EXISTS `eb_admin_login_log` (
  `id`            int          NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `admin_id`      int          NOT NULL DEFAULT 0 COMMENT '管理员id（登录失败时为0）',
  `admin_account` varchar(32)  NOT NULL DEFAULT '' COMMENT '登录账号',
  `ip`            varchar(50)           DEFAULT '' COMMENT '登录IP',
  `location`      varchar(100)          DEFAULT '' COMMENT '登录地点',
  `browser`       varchar(100)          DEFAULT '' COMMENT '浏览器',
  `os`            varchar(100)          DEFAULT '' COMMENT '操作系统',
  `status`        tinyint      NOT NULL DEFAULT 1 COMMENT '状态 1成功 0失败',
  `msg`           varchar(255)          DEFAULT '' COMMENT '提示信息',
  `create_time`   timestamp    NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_account` (`admin_account`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员登录日志表';

-- ---------- 2. 菜单：设置(12) 下新增「日志管理」分组 ----------
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT 12, '日志管理', '', '', '/operation/logManager', 'M', 8, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM (SELECT * FROM `eb_system_menu`) t
  WHERE t.`component` = '/operation/logManager'
);

SET @log_parent := (SELECT id FROM (SELECT * FROM `eb_system_menu`) t WHERE t.`component` = '/operation/logManager' LIMIT 1);

-- 管理员登录日志（菜单 C）
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT @log_parent, '管理员登录日志', '', 'admin:log:login:list', '/operation/logManager/adminLoginLog', 'C', 1, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM (SELECT * FROM `eb_system_menu`) t WHERE t.`perms` = 'admin:log:login:list'
);

-- 管理员操作日志（菜单 C）
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT @log_parent, '管理员操作日志', '', 'admin:log:sensitive:list', '/operation/logManager/adminOperateLog', 'C', 2, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM (SELECT * FROM `eb_system_menu`) t
  WHERE t.`perms` = 'admin:log:sensitive:list' AND t.`pid` <> 0
);

-- ---------- 3. 清理历史脏数据 ----------
-- 初始 SQL 里 admin:log:sensitive:list 是 pid=0 的孤儿记录（id=569），
-- 上面已按 perms 新建了挂到「日志管理」下的正式菜单，这里删掉孤儿避免重复。
DELETE FROM `eb_system_menu` WHERE `perms` = 'admin:log:sensitive:list' AND `pid` = 0;

-- 登录日志页面的操作级权限点
INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT @log_parent, '登录日志详情', '', 'admin:log:login:info', '', 'A', 1, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM (SELECT * FROM `eb_system_menu`) t WHERE t.`perms` = 'admin:log:login:info'
);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT @log_parent, '删除登录日志', '', 'admin:log:login:delete', '', 'A', 2, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM (SELECT * FROM `eb_system_menu`) t WHERE t.`perms` = 'admin:log:login:delete'
);

INSERT INTO `eb_system_menu` (`pid`, `name`, `icon`, `perms`, `component`, `menu_type`, `sort`, `is_show`, `is_delte`)
SELECT @log_parent, '删除操作日志', '', 'admin:log:sensitive:delete', '', 'A', 3, 1, 0
WHERE NOT EXISTS (
  SELECT 1 FROM (SELECT * FROM `eb_system_menu`) t WHERE t.`perms` = 'admin:log:sensitive:delete'
);

-- ---------- 4. 校验 ----------
SELECT id, pid, name, perms, component, menu_type, sort, is_show
FROM eb_system_menu
WHERE component = '/operation/logManager'
   OR perms LIKE 'admin:log:%'
   OR pid = (SELECT id FROM (SELECT * FROM `eb_system_menu`) t WHERE t.`component` = '/operation/logManager' LIMIT 1)
ORDER BY menu_type, sort, id;
