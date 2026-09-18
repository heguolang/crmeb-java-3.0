package com.zbkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品拿货价设置请求 / 换货申请 / 发货请求等
 */
public class StockRequests {

    private StockRequests() {}

    @Data
    @ApiModel(value = "StockPriceSetRequest对象", description = "商品层级拿货价设置")
    public static class StockPriceSetRequest implements Serializable {

        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "商品ID")
        @NotNull(message = "商品不能为空")
        private Integer productId;

        @ApiModelProperty(value = "价格项")
        private List<PriceItem> prices;
    }

    @Data
    @ApiModel(value = "StockPriceItem对象", description = "拿货价项")
    public static class PriceItem implements Serializable {

        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "层级ID")
        @NotNull(message = "层级不能为空")
        private Integer levelId;

        @ApiModelProperty(value = "拿货价（null=清除该层级专用价，走默认折扣）")
        private BigDecimal price;
    }

    @Data
    @ApiModel(value = "StockAdjustRequest对象", description = "库存调整请求")
    public static class StockAdjustRequest implements Serializable {

        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "商品ID")
        @NotNull(message = "商品不能为空")
        private Integer productId;

        @ApiModelProperty(value = "变动数量（正=入库，负=出库）")
        @NotNull(message = "变动数量不能为空")
        private Integer changeNum;

        @ApiModelProperty(value = "调整原因")
        private String mark;
    }

    @Data
    @ApiModel(value = "StockExchangeApplyRequest对象", description = "换货申请请求")
    public static class StockExchangeApplyRequest implements Serializable {

        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "关联订货订单ID")
        @NotNull(message = "订单不能为空")
        private Integer orderId;

        @ApiModelProperty(value = "商品ID")
        @NotNull(message = "商品不能为空")
        private Integer productId;

        @ApiModelProperty(value = "换货数量")
        @NotNull(message = "数量不能为空")
        private Integer num;

        @ApiModelProperty(value = "换货原因")
        @NotBlank(message = "换货原因不能为空")
        private String reason;
    }

    @Data
    @ApiModel(value = "StockSendRequest对象", description = "发货请求")
    public static class StockSendRequest implements Serializable {

        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "快递公司")
        @NotBlank(message = "快递公司不能为空")
        private String expressName;

        @ApiModelProperty(value = "快递单号")
        @NotBlank(message = "快递单号不能为空")
        private String expressNum;
    }

    @Data
    @ApiModel(value = "StockWithdrawAuditRequest对象", description = "提现审核请求")
    public static class StockWithdrawAuditRequest implements Serializable {

        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "审核结果：1=通过打款 -1=驳回")
        @NotNull(message = "审核结果不能为空")
        private Integer status;

        @ApiModelProperty(value = "审核备注")
        private String auditMark;
    }

    @Data
    @ApiModel(value = "StockAuditRequest对象", description = "审核请求")
    public static class StockAuditRequest implements Serializable {

        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "审核结果：1=通过 -1=驳回")
        @NotNull(message = "审核结果不能为空")
        private Integer status;

        @ApiModelProperty(value = "驳回原因")
        private String reason;
    }

    @Data
    @ApiModel(value = "StockVirtualPickupRequest对象", description = "虚拟库存提货请求")
    public static class StockVirtualPickupRequest implements Serializable {

        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "虚拟库存记录ID")
        @NotNull(message = "请选择虚拟库存")
        private Integer virtualId;

        @ApiModelProperty(value = "提货数量")
        @NotNull(message = "请填写提货数量")
        private Integer num;

        @ApiModelProperty(value = "收货地址ID（系统收货地址簿）")
        @NotNull(message = "请选择收货地址")
        private Integer addressId;

        @ApiModelProperty(value = "备注")
        private String mark;
    }

    @Data
    @ApiModel(value = "StockExchangeBackRequest对象", description = "旧品退回快递信息")
    public static class StockExchangeBackRequest implements Serializable {

        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "旧品退回快递公司")
        @NotBlank(message = "快递公司不能为空")
        private String backExpressName;

        @ApiModelProperty(value = "旧品退回快递单号")
        @NotBlank(message = "快递单号不能为空")
        private String backExpressNum;
    }

    @Data
    @ApiModel(value = "StockMonthlySettleRequest对象", description = "阶梯业绩结算请求")
    public static class StockMonthlySettleRequest implements Serializable {

        private static final long serialVersionUID = 1L;

        @ApiModelProperty(value = "结算周期类型：1=月度 2=季度 3=年度")
        private Integer type;

        @ApiModelProperty(value = "结算月份（yyyy-MM，季度/年度取该周期内任一月自动归集）")
        @NotBlank(message = "结算月份不能为空")
        private String month;

        public Integer getType() {
            return type == null || type <= 0 ? 1 : type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }
    }
}
