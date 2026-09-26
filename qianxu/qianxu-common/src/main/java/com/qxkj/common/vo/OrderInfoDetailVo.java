package com.qxkj.common.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

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
public class OrderInfoDetailVo {

    /** 商品id */
    private Integer productId;

    /** 商品名称 */
    private String productName;

    /** 规格属性id */
    private Integer attrValueId;

    /** 商品图片 */
    private String image;

    /** sku */
    private String sku;

    /** 单价 */
    private BigDecimal price;

    /** 购买数量 */
    private Integer payNum;

    /** 重量 */
    private BigDecimal weight;

    /** 体积 */
    private BigDecimal volume;

    /** 运费模板ID */
    private Integer tempId;

    /** 获得积分 */
    private Integer giveIntegral;

    /** 是否支持赠送积分（false 则本商品不赠送） */
    private Boolean isGiveIntegral;

    /** 单品最多可用积分抵扣数（每件），0表示不支持积分抵扣 */
    private Integer integralDeduct;

    /** 积分抵扣金额是否参与分佣：true参与 false不参与 */
    private Boolean isIntegralDeductBrokerage;

    /** 本行实际使用的抵扣积分数 */
    private Integer lineUseIntegral;

    /** 本行积分抵扣金额（元） */
    private BigDecimal lineDeductionPrice;

    /** 是否评价 */
    private Integer isReply;

    /** 是否单独分佣 */
    private Boolean isSub;

    /** 会员价 */
    private BigDecimal vipPrice;

    /** 商品类型:0-普通，1-秒杀，2-砍价，3-拼团，4-视频号 */
    private Integer productType;
}
