package com.qxkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;
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
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="ProductReplyResponse对象", description="商品评论H5详情响应对象")
public class ProductReplyResponse {
    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "评论ID")
    private Integer id;

    @ApiModelProperty(value = "用户ID")
    private Integer uid;

    @ApiModelProperty(value = "商品分数")
    private Integer score;

    @ApiModelProperty(value = "评论内容")
    private String comment;

    @ApiModelProperty(value = "评论图片")
    private List<String> pics;

    @ApiModelProperty(value = "管理员回复内容")
    private String merchantReplyContent;

    @ApiModelProperty(value = "用户名称")
    private String nickname;

    @ApiModelProperty(value = "用户头像")
    private String avatar;

    @ApiModelProperty(value = "评论时间")
    private Date createTime;

    @ApiModelProperty(value = "商品规格属性值")
    private String sku;
}
