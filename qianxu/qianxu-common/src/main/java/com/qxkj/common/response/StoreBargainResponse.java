package com.qxkj.common.response;

import com.qxkj.common.model.product.StoreProductAttr;
import com.qxkj.common.request.StoreProductAttrValueRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

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
public class StoreBargainResponse {

    @ApiModelProperty(value = "砍价商品ID")
    private Integer id;

    @ApiModelProperty(value = "关联商品ID")
    private Integer productId;

    @ApiModelProperty(value = "砍价活动名称")
    private String title;

    @ApiModelProperty(value = "砍价活动图片")
    private String image;

    @ApiModelProperty(value = "单位名称")
    private String unitName;

    @ApiModelProperty(value = "库存")
    private Integer stock;

    @ApiModelProperty(value = "销量")
    private Integer sales;

    @ApiModelProperty(value = "砍价商品轮播图")
    private String images;

    @ApiModelProperty(value = "砍价开启时间")
    private String startTime;

    @ApiModelProperty(value = "砍价结束时间")
    private String stopTime;

    @ApiModelProperty(value = "砍价商品名称")
    private String storeName;

    @ApiModelProperty(value = "砍价金额")
    private BigDecimal price;

    @ApiModelProperty(value = "砍价商品最低价")
    private BigDecimal minPrice;

    @ApiModelProperty(value = "每次购买的砍价商品数量")
    private Integer num;

    @ApiModelProperty(value = "用户每次砍价的最大金额")
    private BigDecimal bargainMaxPrice;

    @ApiModelProperty(value = "用户每次砍价的最小金额")
    private BigDecimal bargainMinPrice;

    @ApiModelProperty(value = "用户每次砍价的次数")
    private Integer bargainNum;

    @ApiModelProperty(value = "砍价状态 0(到砍价时间不自动开启)  1(到砍价时间自动开启时间)")
    private Boolean status;

    @ApiModelProperty(value = "反多少积分")
    private Integer giveIntegral;

//    @ApiModelProperty(value = "砍价活动简介")
//    private String info;

    @ApiModelProperty(value = "成本价")
    private BigDecimal cost;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "是否推荐0不推荐1推荐")
    private Boolean isHot;

    @ApiModelProperty(value = "是否删除 0未删除 1删除")
    private Boolean isDel;

    @ApiModelProperty(value = "添加时间")
    private String addTime;

    @ApiModelProperty(value = "是否包邮 0不包邮 1包邮")
    private Boolean isPostage;

    @ApiModelProperty(value = "邮费")
    private BigDecimal postage;

    @ApiModelProperty(value = "砍价规则")
    private String rule;

    @ApiModelProperty(value = "砍价商品浏览量")
    private Integer look;

    @ApiModelProperty(value = "砍价商品分享量")
    private Integer share;

    @ApiModelProperty(value = "运费模板ID")
    private Integer tempId;

    @ApiModelProperty(value = "重量")
    private BigDecimal weight;

    @ApiModelProperty(value = "体积")
    private BigDecimal volume;

    @ApiModelProperty(value = "限购总数")
    private Integer quota;

    @ApiModelProperty(value = "限量总数显示")
    private Integer quotaShow;

    @ApiModelProperty(value = "限量剩余")
    private Integer surplusQuota;

    @ApiModelProperty(value = "帮助砍价好友人数")
    private Integer peopleNum;

    @ApiModelProperty(value = "参与人数")
    private Long countPeopleAll;

    @ApiModelProperty(value = "帮忙砍价人数")
    private Long countPeopleHelp;

    @ApiModelProperty(value = "砍价成功人数")
    private Long countPeopleSuccess;

    @ApiModelProperty(value = "商品属性")
    private List<StoreProductAttr> attr;

    @ApiModelProperty(value = "商品属性详情")
    private List<StoreProductAttrValueRequest> attrValue;

}
