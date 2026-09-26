-- ============================================================
-- QIANXU Java 3.0 后台菜单可见树收敛（本地 → 线上）
-- 生成时间: 2026-09-23 17:53:13
-- 范围：menu_type 为 M（目录）/ C（菜单）的 125 项，即后台左侧导航的完整可见树。
--       其中标记为删除 5 项、隐藏 3 项、有父级归属 111 项。
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
('/appSetting', 'M', '', '', '应用', 's-promotion', 120, 1, 0),
('/content', 'M', '', '', '内容', 's-management', 130, 1, 0),
('/dashboard', 'M', '', '', '首页', 'menu', 200, 1, 0),
('/design', 'M', '', '', '装修', 's-home', 85, 1, 0),
('/financial', 'M', '', '', '财务', 's-finance', 90, 1, 0),
('/hidden', 'M', '', '', '系统', 'warning', 88, 1, 0),
('/javaMobile', 'M', '', '', '移动端管理', 'phone', 80, 1, 0),
('/maintain', 'M', '', '', '维护', 's-open', 60, 1, 0),
('/marketing', 'M', '', '', '营销', 's-marketing', 110, 1, 0),
('/operation', 'M', '', '', '设置', 's-tools', 70, 1, 0),
('/order', 'M', '', '', '订单', 's-order', 160, 1, 0),
('/store', 'M', '', '', '商品', 's-goods', 180, 1, 0),
('/user', 'M', '', '', '用户', 'user-solid', 140, 1, 0),
('/yunying', 'M', '', '', '运营', 's-operation', 190, 1, 0),
('/user/grade', 'C', '/user', 'admin:system:user:level:list', '用户等级', '', 2, 1, 0),
('/user/group', 'C', '/user', 'admin:user:group:list', '用户分组', '', 1, 1, 0),
('/user/index', 'C', '/user', 'admin:user:list', '用户管理 ', '', 100, 1, 0),
('/user/label', 'C', '/user', 'admin:user:tag:list', '用户标签', '', 1, 1, 0),
('/daili/agentList', 'C', '/daili', 'admin:agent:list', '代理商管理', '', 4, 1, 0),
('/daili/agentReward', 'C', '/daili', 'admin:agent:reward:list', '代理奖励明细', '', 2, 1, 0),
('/daili/agentSetting', 'C', '/daili', 'admin:agent:setting:list', '区域代理设置', '', 3, 1, 0),
('/daili/changeLog', 'C', '/daili', 'admin:agent:changelog:list', '代理商变更记录', '', 1, 1, 0),
('/order/index', 'C', '/order', 'admin:order:list', '订单管理', '', 1, 1, 0),
('/stock/agent', 'C', '/stock', 'admin:stock:agent:list', '订货商管理', '', 9, 1, 0),
('/stock/changelog', 'C', '/stock', 'admin:stock:agent:list', '订货商变更记录', '', 3, 1, 0),
('/stock/exchange', 'C', '/stock', 'admin:stock:exchange:list', '换货管理', '', 5, 1, 0),
('/stock/level', 'C', '/stock', 'admin:stock:level:list', '订货商级别设置', '', 8, 0, 0),
('/stock/order', 'C', '/stock', 'admin:stock:order:list', '订货商订单', '', 4, 1, 0),
('/stock/product', 'C', '/stock', 'admin:stock:product:list', '商品与库存', '', 7, 1, 0),
('/stock/report', 'C', '/stock', 'admin:stock:report:list', '数据报表', '', 10, 1, 0),
('/stock/reward', 'C', '/stock', 'admin:stock:reward:list', '订货商资金记录', '', 6, 1, 0),
('/stock/setting', 'C', '/stock', 'admin:stock:setting:list', '订货商设置', '', 8, 1, 0),
('/stock/withdraw', 'C', '/stock', 'admin:stock:withdraw:list', '提现管理', '', 6, 0, 1),
('/store/attr', 'C', '/store', 'admin:product:rule:list', '商品规格', '', 3, 1, 0),
('/store/comment', 'C', '/store', 'admin:product:reply:list', '商品评论', '', 2, 1, 0),
('/store/guarantee', 'C', '/store', '', '保障服务', '', 1, 1, 0),
('/store/index', 'C', '/store', 'admin:product:list', '商品管理', '', 6, 1, 0),
('/store/productGroup', 'C', '/store', 'admin:store:product:group:list', '商品分组', '', 4, 1, 0),
('/store/sort', 'C', '/store', '', '商品分类', '', 5, 1, 0),
('/design/advertisement', 'C', '/design', '', '开屏广告', '', 0, 1, 0),
('/design/mall_theme', 'C', '/design', '', '商城主题', '', 98, 1, 0),
('/design/micro_theme', 'C', '/design', '', '专题页面', '', 96, 1, 0),
('/design/my_theme', 'C', '/design', '', '我的主题', '', 97, 1, 0),
('/hidden/panel', 'C', '/hidden', '', '运维面板', '', 0, 1, 0),
('/content/articleManager', 'C', '/content', 'admin:article:list', '文章管理', '', 1, 1, 0),
('/content/classifManager', 'C', '/content', '', '文章分类', '', 99999, 1, 0),
('/daili', 'M', '/yunying', '', '区域代理', 'success', 102, 1, 0),
('/distribution', 'M', '/yunying', '', '分销商', 's-check', 105, 1, 0),
('/distribution/teamGrade', 'C', '/tuandui', 'admin:system:team:level:list', '团队等级', '', 90, 1, 0),
('/distribution/teamLevelConfig', 'C', '/tuandui', 'admin:system:team:level:list', '团队等级配置', '', 100, 1, 0),
('/distribution/teamRecord', 'C', '/tuandui', 'admin:system:team:level:record:list', '团队变更记录', '', 7, 1, 0),
('/distribution/teamUser', 'C', '/tuandui', 'admin:system:team:level:user:list', '团队关联用户', '', 110, 1, 0),
('/merchantStore', 'M', '/yunying', '', '门店', 's-platform', 101, 1, 0),
('/stock', 'M', '/yunying', '', '订货商', 'success', 103, 1, 0),
('/tuandui', 'M', '/yunying', '', '团队奖', 'user-solid', 104, 1, 0),
('/maintain//devconfiguration', 'M', '/maintain', '', '开发配置', '', 99999, 1, 0),
('/maintain/logistics', 'M', '/maintain', '', '物流设置', '', 99999, 1, 0),
('/maintain/picture', 'C', '/maintain', 'admin:system:attachment:list', '素材管理', '', 1, 1, 0),
('/operation/maintain/schedule', 'C', '/maintain', '', '定时任务管理', '', 99, 1, 0),
('/dashboard', 'C', '/dashboard', '', '控制台', '', 9, 1, 0),
('/distribution/teamBrokerageRecord', 'C', '/financial', 'admin:system:team:level:brokerage:record', '团队奖记录', '', 8, 1, 0),
('/financial/brokerage', 'C', '/financial', 'admin:finance:monitor:brokerage:record', '佣金记录', '', 1, 1, 0),
('/financial/commission', 'M', '/financial', '', '财务操作', '', 99999, 1, 0),
('/financial/record', 'M', '/financial', '', '财务记录', '', 99999, 1, 0),
('/marketing/bargain', 'M', '/marketing', '', '砍价管理', '', 99999, 1, 0),
('/marketing/coupon', 'M', '/marketing', '', '优惠券', '', 1, 1, 0),
('/marketing/groupBuy', 'M', '/marketing', '', '拼团管理', '', 99999, 1, 0),
('/marketing/integral', 'M', '/marketing', '', '积分', '', 99999, 1, 0),
('/marketing/seckill', 'M', '/marketing', '', '秒杀管理', '', 99999, 1, 0),
('/operation/agreement', 'M', '/operation', '', '协议管理', '', 0, 1, 0),
('/operation/deliverGoods', 'M', '/operation', '', '发货设置', '', 6, 1, 0),
('/operation/design', 'M', '/operation', '', '页面管理', '', 5, 1, 1),
('/operation/guide', 'C', '/operation', '', '配置引导', '', 0, 1, 0),
('/operation/logManager', 'M', '/operation', '', '日志管理', '', 8, 1, 0),
('/operation/notification', 'C', '/operation', 'admin:system:notification:list', '消息通知', '', 4, 1, 0),
('/operation/onePass', 'C', '/operation', '', '一号通', '', 9, 0, 0),
('/operation/onePassConfig', 'C', '/operation', '', '一号通配置', '', 9, 0, 0),
('/operation/roleManager', 'M', '/operation', '', '管理权限', '', 7, 1, 0),
('/operation/setting', 'C', '/operation', 'admin:system:config:info', '系统设置', '', 10, 1, 0),
('/appSetting/publicAccount', 'M', '/appSetting', '', '公众号', '', 99999, 1, 0),
('/appSetting/publicRoutine', 'M', '/appSetting', '', '小程序', '', 99999, 1, 0),
('/javaMobile/orderCancellation', 'C', '/javaMobile', '', '订单核销', '', 99999, 1, 0),
('/javaMobile/orderStatistics', 'C', '/javaMobile', '', '订单统计', '', 99999, 1, 0),
('/distribution/distributionconfig', 'C', '/distribution', 'admin:retail:spread:manage:get', '分销配置', '', 1, 1, 0),
('/distribution/distributorLevel', 'C', '/distribution', 'admin:system:user:level:brokerage:list', '分销商等级', '', 3, 1, 0),
('/distribution/index', 'C', '/distribution', 'admin:retail:list', '分销商管理', '', 100, 1, 0),
('/merchantStore/list', 'C', '/merchantStore', 'admin:merchant:store:list', '门店管理', '', 10, 1, 0),
('/merchantStore/verify', 'C', '/merchantStore', 'admin:merchant:verify:list', '核销记录', '', 2, 1, 0),
('/financial/record/charge', 'C', '/financial/record', 'admin:recharge:list', '充值记录', '', 1, 1, 0),
('/financial/record/monitor', 'C', '/financial/record', 'admin:finance:monitor:list', '资金监控', '', 1, 1, 0),
('/marketing/coupon/list', 'C', '/marketing/coupon', 'admin:coupon:list', '优惠券', '', 1, 1, 0),
('/marketing/coupon/record', 'C', '/marketing/coupon', 'admin:coupon:user:list', '领取记录', '', 1, 1, 0),
('/operation/design/devise', 'M', '/operation/design', '', '页面装修', '', 0, 1, 1),
('/operation/design/theme', 'C', '/operation/design', '', '一键换色', '', 99999, 1, 1),
('/operation/design/viewDesign', 'C', '/operation/design', 'admin:page:layout:index', '页面设计', '', 99999, 1, 1),
('/marketing/bargain/bargainGoods', 'C', '/marketing/bargain', 'admin:bargain:list', '砍价商品', '', 1, 1, 0),
('/marketing/bargain/bargainList', 'C', '/marketing/bargain', 'admin:bargain:user:list', '砍价列表', '', 1, 1, 0),
('/marketing/seckill/config', 'C', '/marketing/seckill', 'admin:seckill:manger:list', '秒杀配置', '', 1, 1, 0),
('/marketing/seckill/list', 'C', '/marketing/seckill', 'admin:seckill:list', '秒杀商品', '', 1, 1, 0),
('/maintain/logistics/cityList', 'C', '/maintain/logistics', 'admin:system:city:list', '城市数据', '', 1, 1, 0),
('/maintain/logistics/companyList', 'C', '/maintain/logistics', 'admin:express:list', '物流公司', '', 2, 1, 0),
('/marketing/groupBuy/groupGoods', 'C', '/marketing/groupBuy', 'admin:combination:list', '拼团商品', '', 1, 1, 0),
('/marketing/groupBuy/groupList', 'C', '/marketing/groupBuy', 'admin:combination:combine:list', '拼团列表', '', 1, 1, 0),
('/marketing/integral/integralconfig', 'C', '/marketing/integral', '', '积分配置', '', 99999, 1, 0),
('/marketing/integral/integrallog', 'C', '/marketing/integral', 'admin:user:integral:list', '积分日志', '', 1, 1, 0),
('/financial/commission/setting', 'C', '/financial/commission', 'admin:finance:extract:setting:get', '提现设置', '', 2, 1, 0),
('/financial/commission/template', 'C', '/financial/commission', 'admin:finance:apply:list', '申请提现', '', 1, 1, 0),
('/operation/logManager/adminLoginLog', 'C', '/operation/logManager', 'admin:log:login:list', '管理员登录日志', '', 1, 1, 0),
('/operation/logManager/adminOperateLog', 'C', '/operation/logManager', 'admin:log:sensitive:list', '管理员操作日志', '', 2, 1, 0),
('/operation/roleManager/adminList', 'C', '/operation/roleManager', 'admin:system:admin:list', '管理员列表', '', 1, 1, 0),
('/operation/roleManager/identityManager', 'C', '/operation/roleManager', 'admin:system:role:list', '角色管理', '', 1, 1, 0),
('/operation/roleManager/promiseRules', 'C', '/operation/roleManager', 'admin:system:menu:list', '权限规则', '', 1, 1, 0),
('/operation/deliverGoods/freightSet', 'C', '/operation/deliverGoods', 'admin:shipping:templates:list', '运费模板', '', 2, 1, 0),
('/appSetting/publicAccount/wxMenus', 'C', '/appSetting/publicAccount', 'admin:wechat:menu:public:get', '微信菜单', '', 0, 1, 0),
('/appSetting/publicAccount/wxReply', 'M', '/appSetting/publicAccount', '', '自动回复', '', 1, 1, 0),
('/appSetting/publicRoutine/deliveryManagement', 'C', '/appSetting/publicRoutine', '', '发货管理', '', 0, 1, 0),
('/appSetting/publicRoutine/download', 'C', '/appSetting/publicRoutine', 'admin:wechat:code:download', '下载小程序', '', 0, 1, 0),
('/maintain/devconfiguration/combineddata', 'C', '/maintain//devconfiguration', 'admin:system:group:list', '组合数据', '', 1, 1, 0),
('/maintain/devconfiguration/configCategory', 'C', '/maintain//devconfiguration', '', '配置分类', '', 99999, 1, 0),
('/maintain/devconfiguration/formConfig', 'C', '/maintain//devconfiguration', 'admin:system:form:list', '表单配置', '', 1, 1, 0),
('/maintain/schedule/list', 'C', '/operation/maintain/schedule', '', '定时任务', '', 0, 1, 0),
('/maintain/schedule/logList', 'C', '/operation/maintain/schedule', '', '定时任务日志', '', 0, 1, 0),
('/appSetting/publicAccount/wxReply/follow', 'C', '/appSetting/publicAccount/wxReply', 'admin:wechat:keywords:reply:info:keywords', '微信关注回复', '', 99999, 1, 0),
('/appSetting/publicAccount/wxReply/keyword', 'C', '/appSetting/publicAccount/wxReply', 'admin:wechat:keywords:reply:list', '关键字回复', '', 99999, 1, 0),
('/appSetting/publicAccount/wxReply/replyIndex', 'C', '/appSetting/publicAccount/wxReply', '', '无效关键词回复', '', 99999, 1, 0);

-- 0) 每个 (component, menu_type) 的「规范行」= id 最小的一条。
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

-- 1) 补插线上缺失的菜单。父级必须先落库，故「物化父级查找表 + 补插」重复 3 次
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

-- 1) 补插线上缺失的菜单。父级必须先落库，故「物化父级查找表 + 补插」重复 3 次
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

-- 1) 补插线上缺失的菜单。父级必须先落库，故「物化父级查找表 + 补插」重复 3 次
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
SELECT 'QIANXU menu_sync done' AS result,
       SUM(`menu_type` = 'M') AS dirs,
       SUM(`menu_type` = 'C') AS menus,
       SUM(IFNULL(`is_delte`, 0) = 1 AND `menu_type` IN ('M', 'C')) AS deleted_mark
  FROM eb_system_menu;
