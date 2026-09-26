package com.qxkj.common.request;

import com.baomidou.mybatisplus.annotation.TableName;
import com.qxkj.common.annotation.StringContains;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 *
 *  +----------------------------------------------------------------------
 *  | 黔序商城 [ 黔序科技，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2021~2026 https://www.qianxutec.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed 黔序商城系统软件V1.0（软著登记号2025SR2146980），未经许可不得去除版权声明
 *  +----------------------------------------------------------------------
 *  | Author: 贵州黔序科技有限公司
 *  +----------------------------------------------------------------------
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("eb_store_order")
@ApiModel(value="StoreOrderSearchRequest对象", description="订单列表请求对象")
public class StoreOrderSearchRequest extends UserCommonSearchRequest implements Serializable {
    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "创建时间区间")
    private String dateLimit;

    @ApiModelProperty(value = "订单状态（all 总数； 未支付 unPaid； 未发货 notShipped；待收货 spike；待评价 bargain；已完成 complete；待核销 toBeWrittenOff；退款中:refunding；已退款:refunded；已删除:deleted")
    @StringContains(limitValues = {"all","unPaid","notShipped","spike","bargain","complete","toBeWrittenOff","refunding","refunded","deleted"}, message = "未知的订单状态")
    private String status;

    @ApiModelProperty(value = "订单类型：0普通订单，1-视频号订单, 2-全部订单")
    @NotNull(message = "订单类型不能为空")
    @Range(min = 0, max = 2, message = "未知的订单类型")
    private Integer type;

    @ApiModelProperty(value = "物流单号")
    private String deliveryId;

}
