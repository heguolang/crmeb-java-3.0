package com.qxkj.common.constants;

/**
 * 2+1 链动默认规则与常量。
 * <p>
 * 已确认：代理礼包=配置的固定商品ID；有效代理时机可配置（支付/完成）。
 * 其余默认（后续可改配置/规则表）：
 * <ul>
 *   <li>直推位容量 N=2，满员滑落（最小直推团队优先）</li>
 *   <li>邀请人拿直推奖；见单/平级走安置链 chain_parent</li>
 *   <li>滑落产生的首个溢出用户记为老板帮扶对象</li>
 *   <li>礼包订单默认跳过普通分销/团队奖，避免叠佣</li>
 *   <li>平级奖从见单金额中抽成；佣金进现有佣金钱包</li>
 *   <li>退款默认不降级身份；未提现佣金失效（复用佣金流水退款逻辑）</li>
 * </ul>
 */
public class ChainConstants {

    public static final String CONFIG_FUNC_STATUS = "chain_func_status";
    public static final String CONFIG_GIFT_PRODUCT_IDS = "chain_gift_product_ids";
    /** 1=支付即有效 2=订单完成才有效 */
    public static final String CONFIG_EFFECTIVE_TIMING = "chain_effective_timing";
    /** 1=支付到账 2=完成到账 */
    public static final String CONFIG_CREDIT_TIMING = "chain_credit_timing";
    public static final String CONFIG_FREEZE_DAYS = "chain_freeze_days";
    public static final String CONFIG_SLIDE_ENABLED = "chain_slide_enabled";
    public static final String CONFIG_SLIDE_CAPACITY = "chain_slide_capacity";
    public static final String CONFIG_SLIDE_DEPTH = "chain_slide_depth";
    /** 滑落方向：min_team / left / right */
    public static final String CONFIG_SLIDE_MODE = "chain_slide_mode";
    public static final String CONFIG_SEE_GENERATIONS = "chain_see_generations";
    public static final String CONFIG_IDENTITY_DOWNGRADE = "chain_identity_downgrade";
    public static final String CONFIG_SKIP_NORMAL_BROKERAGE = "chain_skip_normal_brokerage";
    /** 帮扶超时天数，0=关闭（旧滑落帮扶超时释放） */
    public static final String CONFIG_HELP_TIMEOUT_DAYS = "chain_help_timeout_days";
    /** 帮扶解锁：每位留下的代理需直推完成的礼包单数 X */
    public static final String CONFIG_HELP_UNLOCK_ORDERS = "chain_help_unlock_orders";
    /** 未解锁时见单奖冻结进帮扶基金的比例%（默认50） */
    public static final String CONFIG_HELP_FREEZE_RATIO = "chain_help_freeze_ratio";
    /** 规则说明文案（前端展示） */
    public static final String CONFIG_RULES_CONTENT = "chain_rules_content";

    public static final String SLIDE_MODE_MIN_TEAM = "min_team";
    public static final String SLIDE_MODE_LEFT = "left";
    public static final String SLIDE_MODE_RIGHT = "right";
    /** 分享链接链动标识 */
    public static final String SHARE_QUERY_FLAG = "chain=1";

    public static final int IDENTITY_NONE = 0;
    public static final int IDENTITY_AGENT = 1;
    public static final int IDENTITY_BOSS = 2;

    public static final String REWARD_DIRECT = "direct";
    public static final String REWARD_SEE = "see";
    public static final String REWARD_PEER = "peer";
    public static final String REWARD_HELP = "help";
    /** 见单帮扶基金（未解锁冻结部分） */
    public static final String REWARD_SEE_HELP = "see_help";

    public static final int COMMISSION_PENDING = 1;
    public static final int COMMISSION_FROZEN = 2;
    public static final int COMMISSION_SETTLED = 3;
    public static final int COMMISSION_INVALID = 4;
    /** 帮扶基金冻结（见单未解锁部分，不进可提现佣金） */
    public static final int COMMISSION_HELP_FUND = 5;
    public static final int COMMISSION_CLAWBACK = 6;

    public static final String TITLE_SEE_HELP = "链动见单帮扶基金";

    public static final int HELP_DOING = 0;
    public static final int HELP_DONE = 1;
    public static final int HELP_RELEASED = 2;

    /** 佣金流水 brokerageLevel */
    public static final int BROKERAGE_LEVEL_DIRECT = 20;
    public static final int BROKERAGE_LEVEL_SEE = 21;
    public static final int BROKERAGE_LEVEL_PEER = 22;
    public static final int BROKERAGE_LEVEL_HELP = 23;

    public static final String TITLE_DIRECT = "获得链动直推奖";
    public static final String TITLE_SEE = "获得链动见单奖";
    public static final String TITLE_PEER = "获得链动平级奖";
    public static final String TITLE_HELP = "获得链动帮扶奖";
    public static final String TITLE_DIVIDEND = "获得链动团队分红";

    public static final String REWARD_DIVIDEND = "dividend";
    public static final int BROKERAGE_LEVEL_DIVIDEND = 24;

    public static final String CONFIG_RISK_STATUS = "chain_risk_status";
    public static final String CONFIG_RISK_WINDOW_MINUTES = "chain_risk_window_minutes";
    public static final String CONFIG_RISK_MAX_UID = "chain_risk_max_uid";
    public static final String CONFIG_RISK_MAX_PHONE = "chain_risk_max_phone";
    public static final String CONFIG_RISK_MAX_IP = "chain_risk_max_ip";
    public static final String CONFIG_RISK_MAX_DEVICE = "chain_risk_max_device";
    public static final String CONFIG_NOTIFY_STATUS = "chain_notify_status";
    public static final String CONFIG_DIVIDEND_STATUS = "chain_dividend_status";
    public static final String CONFIG_DIVIDEND_RATIO = "chain_dividend_ratio";
    public static final String CONFIG_DIVIDEND_MIN_TEAM = "chain_dividend_min_team";

    public static final String NOTIFY_UPGRADE = "upgrade";
    public static final String NOTIFY_COMMISSION = "commission";
    public static final String NOTIFY_HELP = "help";
    public static final String NOTIFY_DIVIDEND = "dividend";
    public static final String NOTIFY_RISK = "risk";
}
