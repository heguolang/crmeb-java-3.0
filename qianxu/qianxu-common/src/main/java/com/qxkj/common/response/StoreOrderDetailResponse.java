package com.qxkj.common.response;

import com.qxkj.common.vo.StoreOrderInfoOldVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
@ApiModel(value="StoreOrderDetailResponse对象", description="订单信息响应对象（pc列表用）")
public class StoreOrderDetailResponse implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "订单号")
    private String orderId;

    @ApiModelProperty(value = "下单会员ID")
    private Integer uid;

    @ApiModelProperty(value = "下单会员昵称")
    private String nickname;

    @ApiModelProperty(value = "下单会员手机号")
    private String phone;

    @ApiModelProperty(value = "推荐人ID")
    private Integer spreadUid;

    @ApiModelProperty(value = "推荐人昵称")
    private String spreadNickname;

    @ApiModelProperty(value = "推荐人手机号")
    private String spreadPhone;

    @ApiModelProperty(value = "实际支付金额")
    private BigDecimal payPrice;

    @ApiModelProperty(value = "支付方式")
    private String payType;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "订单状态（0：待发货；1：待收货；2：已收货，待评价；3：已完成；）")
    private Integer status;

    @ApiModelProperty(value = "商品信息")
    private List<StoreOrderInfoOldVo> productList = new ArrayList<>();

    @ApiModelProperty(value = "订单状态")
    private Map<String, String> statusStr;

    @ApiModelProperty(value = "支付方式")
    private String payTypeStr;

    @ApiModelProperty(value = "是否删除")
    private Boolean isDel;

    @ApiModelProperty(value = "退款图片")
    private String refundReasonWapImg;

    @ApiModelProperty(value = "退款用户说明")
    private String refundReasonWapExplain;

    @ApiModelProperty(value = "退款时间")
    private Date refundReasonTime;

    @ApiModelProperty(value = "前台退款原因")
    private String refundReasonWap;

    @ApiModelProperty(value = "不退款的理由")
    private String refundReason;

    @ApiModelProperty(value = "退款金额")
    private BigDecimal refundPrice;

    @ApiModelProperty(value = "0 未退款 1 申请中 2 已退款")
    private Integer refundStatus;

    @ApiModelProperty(value = "核销码")
    private String verifyCode;

    @ApiModelProperty(value = "订单类型")
    private String orderType;

    @ApiModelProperty(value = "订单管理员备注")
    private String remark;

    @ApiModelProperty(value = "用户姓名")
    private String realName;

    @ApiModelProperty(value = "收货人电话")
    private String userPhone;

    @ApiModelProperty(value = "收货人地址")
    private String userAddress;

    @ApiModelProperty(value = "商品总价")
    private BigDecimal proTotalPrice;

    @ApiModelProperty(value = "优惠券金额")
    private BigDecimal couponPrice;

    @ApiModelProperty(value = "改价前支付金额")
    private BigDecimal beforePayPrice;

    @ApiModelProperty(value = "支付状态")
    private Boolean paid;

    @ApiModelProperty(value = "订单类型:0-普通订单，1-视频号订单")
    private Integer type;

    @ApiModelProperty(value = "是否改价,0-否，1-是")
    private Boolean isAlterPrice;

    @ApiModelProperty(value = "商家寄件单号图片")
    private String shipmentPic;

    @ApiModelProperty(value = "商家寄件订单任务id")
    private String shipmentTaskId;

    @ApiModelProperty(value = "商家寄件订单单号")
    private String shipmentOrderId;

    @ApiModelProperty(value = "商品总价")
    private BigDecimal totalPrice;

    @ApiModelProperty(value = "原始邮费")
    private BigDecimal totalPostage;

    @ApiModelProperty(value = "实际支付邮费")
    private BigDecimal payPostage;
}
