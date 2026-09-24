package com.zbkj.common.model.product;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品级佣金配置（存 StoreProduct.commissionConfig JSON）
 * <p>
 * 约定：字段为 null 表示「取全局/等级配置」；数值 0 表示「该商品无此项」。
 * <p>
 * 注意：订货商不在此配置（拿货价/平级在「运营 → 订货 → 商品与库存」按商品独立设置），
 * 历史 JSON 中的 stock 段已被忽略，不再参与任何结算。
 */
@Data
@ApiModel(value = "ProductCommissionConfig", description = "商品级佣金配置")
public class ProductCommissionConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "分销商")
    private Distributor distributor = new Distributor();

    @ApiModelProperty(value = "区域代理")
    private Agent agent = new Agent();

    @ApiModelProperty(value = "门店")
    private Store store = new Store();

    @ApiModelProperty(value = "团队奖")
    private Team team = new Team();

    @Data
    public static class Distributor implements Serializable {
        private static final long serialVersionUID = 1L;
        /** null=跟随全局分销开关；true/false=商品级开关 */
        private Boolean enabled;
        private BigDecimal directAmount;
        private BigDecimal directRate;
        private BigDecimal indirectAmount;
        private BigDecimal indirectRate;
        /** 按分销商等级单独配置（优先于上方一刀切字段；等级未配置时仍回落一刀切 → 全局） */
        private List<DistributorLevel> levels;
    }

    @Data
    public static class DistributorLevel implements Serializable {
        private static final long serialVersionUID = 1L;
        /** eb_distributor_level.id */
        private Integer levelId;
        private BigDecimal selfAmount;
        private BigDecimal selfRate;
        private BigDecimal oneAmount;
        private BigDecimal oneRate;
        private BigDecimal twoAmount;
        private BigDecimal twoRate;
    }

    @Data
    public static class Agent implements Serializable {
        private static final long serialVersionUID = 1L;
        private Boolean enabled;
        /** 同总设置模式：上级比例含下级份额（商品页已不提供入口，保留兼容历史 JSON） */
        private Boolean syncMode;
        /** 无下级时代理是否领取下级份额（商品页已不提供入口，保留兼容历史 JSON） */
        private Boolean superiorClaim;
        private BigDecimal provinceAmount;
        private BigDecimal provinceRate;
        private BigDecimal cityAmount;
        private BigDecimal cityRate;
        private BigDecimal districtAmount;
        private BigDecimal districtRate;
        /** 已下线（区域代理无平级/越级推荐奖，仅保留字段兼容历史 JSON） */
        private BigDecimal peerAmount;
        private BigDecimal peerRate;
        /** 已下线（区域代理无平级/越级推荐奖，仅保留字段兼容历史 JSON） */
        private BigDecimal leapAmount;
        private BigDecimal leapRate;
    }

    @Data
    public static class Team implements Serializable {
        private static final long serialVersionUID = 1L;
        /** 团队奖开关：null=跟随全局 team_brokerage_status；false=本商品不参与团队奖 */
        private Boolean enabled;
        /** 按团队等级单独配置（极差/平级），未配置的等级走全局 eb_system_team_level_config */
        private List<TeamLevel> levels;
    }

    @Data
    public static class TeamLevel implements Serializable {
        private static final long serialVersionUID = 1L;
        /** eb_system_team_level.id */
        private Integer levelId;
        /** 极差奖覆盖 */
        private BigDecimal diffAmount;
        private BigDecimal diffRate;
        /** 平级奖覆盖 */
        private BigDecimal peerAmount;
        private BigDecimal peerRate;
    }

    @Data
    public static class Store implements Serializable {
        private static final long serialVersionUID = 1L;
        /** @deprecated 历史「门店佣金」字段，仅兼容旧 JSON，不再参与结算与后台录入 */
        private Boolean brokerageEnabled;
        /** @deprecated 见 brokerageEnabled */
        private BigDecimal brokerageAmount;
        /** @deprecated 见 brokerageEnabled */
        private BigDecimal brokerageRate;
        /** @deprecated 历史「奖励金」字段，仅兼容旧 JSON */
        private Boolean bonusEnabled;
        /** @deprecated 见 bonusEnabled */
        private BigDecimal bonusAmount;
        /** @deprecated 见 bonusEnabled */
        private BigDecimal bonusRate;

        @ApiModelProperty(value = "自提服务费（商品级覆盖门店默认）")
        private FeeItem pickup = new FeeItem();

        @ApiModelProperty(value = "核销服务费（商品级覆盖门店默认）")
        private FeeItem verify = new FeeItem();

        @ApiModelProperty(value = "配送服务费（商品级覆盖门店默认）")
        private FeeItem delivery = new FeeItem();
    }

    /**
     * 门店服务费单项：enabled null=跟随门店默认；true=启用商品覆盖；false=停用（0）。
     * amount / rate 留空时跟随门店默认；同时填写时金额优先。
     */
    @Data
    public static class FeeItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private Boolean enabled;
        private BigDecimal amount;
        private BigDecimal rate;
    }
}
