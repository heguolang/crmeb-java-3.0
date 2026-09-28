package com.qxkj.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @program: qianxu
 * @author: 大粽子
 * @create: 2021-10-27 10:38
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="OrderExcelVo", description = "产品导出")
public class OrderExcelVo implements Serializable {

    @ApiModelProperty(value = "订单号")
    private String orderId;

    @ApiModelProperty(value = "实际支付金额")
    private String payPrice;

//    @ApiModelProperty(value = "支付方式")
//    private String payType;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

//    @ApiModelProperty(value = "订单状态（0：待发货；1：待收货；2：已收货，待评价；3：已完成；）")
//    private String status;

    @ApiModelProperty(value = "商品信息")
    private String productName;

    @ApiModelProperty(value = "订单状态")
    private String statusStr;

    @ApiModelProperty(value = "支付方式")
    private String payTypeStr;


    @ApiModelProperty(value = "订单类型")
    private String orderType;

//    @ApiModelProperty(value = "订单管理员备注")
//    private String remark;

    @ApiModelProperty(value = "用户姓名")
    private String realName;

}
