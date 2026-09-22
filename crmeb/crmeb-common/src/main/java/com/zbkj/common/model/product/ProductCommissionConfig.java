package com.zbkj.common.model.product;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品级佣金配置（存 StoreProduct.commissionConfig JSON）
 * <p>
 * 约定：字段为 null 表示「取全局/等级配置」；数值 0 表示「该商品无此项」。
 */
@Data
@ApiModel(value = "ProductCommissionConfig", description = "商品级佣金配置")
public class ProductCommissionConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "分销商")
    private Distributor distributor = new Distributor();

    @ApiModelProperty(value = "区域代理")
    private Agent agent = new Agent();

    @ApiModelProperty(value = "订货商")
    private Stock stock = new Stock();

    @ApiModelProperty(value = "门店")
    private Store store = new Store();

    @Data
    public static class Distributor implements Serializable {
        private static final long serialVersionUID = 1L;
        /** null=跟随全局分销开关；true/false=商品级开关 */
        private Boolean enabled;
        private BigDecimal directAmount;
        private BigDecimal directRate;
        private BigDecimal indirectAmount;
        private BigDecimal indirectRate;
    }

    @Data
    public static class Agent implements Serializable {
        private static final long serialVersionUID = 1L;
        private Boolean enabled;
        /** 同总设置模式：上级比例含下级份额 */
        private Boolean syncMode;
        /** 无下级时代理是否领取下级份额 */
        private Boolean superiorClaim;
        private BigDecimal provinceAmount;
        private BigDecimal provinceRate;
        private BigDecimal cityAmount;
        private BigDecimal cityRate;
        private BigDecimal districtAmount;
        private BigDecimal districtRate;
        private BigDecimal peerAmount;
        private BigDecimal peerRate;
        private BigDecimal leapAmount;
        private BigDecimal leapRate;
    }

    @Data
    public static class Stock implements Serializable {
        private static final long serialVersionUID = 1L;
        /** 返差价开关：null=跟随全局 stock_diff_reward_status */
        private Boolean diffEnabled;
        private BigDecimal peerAmount;
        private BigDecimal peerRate;
        private BigDecimal leapAmount;
        private BigDecimal leapRate;
    }

    @Data
    public static class Store implements Serializable {
        private static final long serialVersionUID = 1L;
        private Boolean brokerageEnabled;
        private BigDecimal brokerageAmount;
        private BigDecimal brokerageRate;
        private Boolean bonusEnabled;
        private BigDecimal bonusAmount;
        private BigDecimal bonusRate;
    }
}
