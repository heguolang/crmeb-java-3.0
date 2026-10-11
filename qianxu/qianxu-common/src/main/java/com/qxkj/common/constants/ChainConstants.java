package com.qxkj.common.constants;

/**
 * 2+1 链动常量与配置键（对齐《2+1链动模式系统需求文档 V1.0》）。
 * <p>
 * 核心口径：
 * <ul>
 *   <li>身份：普通用户 → 代理（买激活礼包，实付≥门槛价）→ 老板（直推满 N 个有效代理）</li>
 *   <li>走人留人：升老板时本人脱离原上级团队；升级前的前 N 个直推继续留在原上级名下</li>
 *   <li>奖励：直推奖（固定额，1 级）、见点奖（订单实付×比例，仅直属上级老板，1 级）、
 *       平级奖（下级见点奖×比例，1 级）、帮扶基金（每笔见点/平级奖按比例冻结）</li>
 *   <li>硬约束：分润层级不超过 2 层；单笔分润不超过订单实付的 N%</li>
 * </ul>
 */
public class ChainConstants {

    // ==================== 基础 ====================
    public static final String CONFIG_FUNC_STATUS = "chain_func_status";
    /** 激活礼包商品 ID（逗号分隔，由「链动产品」同步维护） */
    public static final String CONFIG_GIFT_PRODUCT_IDS = "chain_gift_product_ids";
    /** 激活门槛价（元）：订单实付 >= 该值才算有效订单 */
    public static final String CONFIG_ACTIVE_THRESHOLD = "chain_active_threshold";
    /** 售后保护期（天）：支付满 N 天且无退款才计入链动；0=支付即生效 */
    public static final String CONFIG_AFTER_SALE_DAYS = "chain_after_sale_days";
    /** 1=支付即有效 2=订单完成才有效 */
    public static final String CONFIG_EFFECTIVE_TIMING = "chain_effective_timing";
    /** 1=支付到账 2=完成到账 */
    public static final String CONFIG_CREDIT_TIMING = "chain_credit_timing";
    /** 佣金冻结天数，0=不冻结 */
    public static final String CONFIG_FREEZE_DAYS = "chain_freeze_days";

    // ==================== 身份与升级 ====================
    /** 代理显示名称（可自定义，如「VIP代理」） */
    public static final String CONFIG_LEVEL_AGENT_NAME = "chain_level_agent_name";
    /** 老板显示名称（可自定义，如「合伙人」） */
    public static final String CONFIG_LEVEL_BOSS_NAME = "chain_level_boss_name";
    /** 直推位容量 N：直推满 N 个有效代理晋升老板，2+1 的「2」 */
    public static final String CONFIG_DIRECT_CAPACITY = "chain_direct_capacity";
    /** 走人留人机制开关 */
    public static final String CONFIG_LEAVE_TEAM_ENABLED = "chain_leave_team_enabled";
    /** 退款是否降级身份 0=否 1=是 */
    public static final String CONFIG_IDENTITY_DOWNGRADE = "chain_identity_downgrade";
    /** 礼包订单是否跳过普通分销/团队奖，避免叠佣 */
    public static final String CONFIG_SKIP_NORMAL_BROKERAGE = "chain_skip_normal_brokerage";

    // ==================== 奖励 ====================
    /** 单笔分润封顶：不超过订单实付的 N%（文档硬约束，默认 50） */
    public static final String CONFIG_MAX_BROKERAGE_RATIO = "chain_max_brokerage_ratio";

    // ==================== 帮扶基金 ====================
    /** 每笔见点/平级奖冻结进帮扶池的比例%（默认 30） */
    public static final String CONFIG_HELP_FREEZE_RATIO = "chain_help_freeze_ratio";
    /** 解冻条件 N：每位留人代理的团队有效订单数（默认 10） */
    public static final String CONFIG_HELP_UNLOCK_N = "chain_help_unlock_n";
    /** 解冻条件 M：该老板自身团队有效订单数（默认 20） */
    public static final String CONFIG_HELP_UNLOCK_M = "chain_help_unlock_m";
    /** 解冻方式 1=条件达成一次性解冻 2=按订单完成比例逐笔释放 */
    public static final String CONFIG_HELP_RELEASE_MODE = "chain_help_release_mode";
    /** 规则说明文案（会员端展示） */
    public static final String CONFIG_RULES_CONTENT = "chain_rules_content";

    /** 解冻方式：一次性 */
    public static final String HELP_RELEASE_ONCE = "1";
    /** 解冻方式：按比例逐笔释放 */
    public static final String HELP_RELEASE_RATIO = "2";

    /** 分享链接链动标识 */
    public static final String SHARE_QUERY_FLAG = "chain=1";

    // ==================== 身份 ====================
    public static final int IDENTITY_NONE = 0;
    public static final int IDENTITY_AGENT = 1;
    public static final int IDENTITY_BOSS = 2;

    // ==================== 奖励类型 ====================
    public static final String REWARD_DIRECT = "direct";
    public static final String REWARD_SEE = "see";
    public static final String REWARD_PEER = "peer";
    /** 见点奖冻结进帮扶基金 */
    public static final String REWARD_SEE_HELP = "see_help";
    /** 平级奖冻结进帮扶基金 */
    public static final String REWARD_PEER_HELP = "peer_help";
    public static final String REWARD_DIVIDEND = "dividend";

    // ==================== 链动流水状态 ====================
    public static final int COMMISSION_PENDING = 1;
    public static final int COMMISSION_FROZEN = 2;
    public static final int COMMISSION_SETTLED = 3;
    public static final int COMMISSION_INVALID = 4;
    /** 帮扶基金冻结中（不进可提现佣金） */
    public static final int COMMISSION_HELP_FUND = 5;
    public static final int COMMISSION_CLAWBACK = 6;

    // ==================== 佣金流水 brokerageLevel ====================
    public static final int BROKERAGE_LEVEL_DIRECT = 20;
    public static final int BROKERAGE_LEVEL_SEE = 21;
    public static final int BROKERAGE_LEVEL_PEER = 22;
    public static final int BROKERAGE_LEVEL_HELP = 23;
    public static final int BROKERAGE_LEVEL_DIVIDEND = 24;

    // ==================== 标题 ====================
    public static final String TITLE_DIRECT = "获得链动直推奖";
    public static final String TITLE_SEE = "获得链动见点奖";
    public static final String TITLE_PEER = "获得链动平级奖";
    public static final String TITLE_HELP = "获得链动帮扶奖";
    public static final String TITLE_SEE_HELP = "链动帮扶基金";
    public static final String TITLE_DIVIDEND = "获得链动团队分红";

    // ==================== 通知 ====================
    public static final String CONFIG_NOTIFY_STATUS = "chain_notify_status";
    public static final String NOTIFY_UPGRADE = "upgrade";
    public static final String NOTIFY_COMMISSION = "commission";
    public static final String NOTIFY_HELP = "help";
    public static final String NOTIFY_DIVIDEND = "dividend";

    // ==================== 团队分红（独立于本文档，保留） ====================
    public static final String CONFIG_DIVIDEND_STATUS = "chain_dividend_status";
    public static final String CONFIG_DIVIDEND_RATIO = "chain_dividend_ratio";
    public static final String CONFIG_DIVIDEND_MIN_TEAM = "chain_dividend_min_team";
}
