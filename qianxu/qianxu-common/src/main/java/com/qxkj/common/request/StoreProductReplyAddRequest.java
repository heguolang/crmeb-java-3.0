package com.qxkj.common.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
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
@ApiModel(value="StoreProductReplyAddRequest对象", description="评论添加对象")
public class StoreProductReplyAddRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "订单编号， 移动端必须传递此参数")
    private String orderNo;

    @ApiModelProperty(value = "用户id， 后端必须传递此参数")
    private Integer userId;

    @ApiModelProperty(value = "商品id", required = true)
    @Min(value = 1, message = "请选择商品")
    private Integer productId;

    @ApiModelProperty(value = "商品 属性id")
    private String unique;

    @ApiModelProperty(value = "商品分数", example = "5", required = true)
    @Range(min = 1, max = 5, message = "商品分数为1-5")
    private Integer productScore;

    @ApiModelProperty(value = "服务分数", example = "5", required = true)
    @Range(min = 1, max = 5, message = "服务分数为1-5")
    private Integer serviceScore;

    @ApiModelProperty(value = "评论内容", required = true)
    @NotBlank(message = "请填写评论内容")
    @Length(max = 512, message = "评论内容长度不能超过512个字符")
    private String comment;

    @ApiModelProperty(value = "评论图片", required = true)
    private String pics;

    @ApiModelProperty(value = "评论人头像 [虚拟评论参数]")
    private String avatar;

    @ApiModelProperty(value = "评论人昵称 [虚拟评论参数]")
    private String nickname;

    @ApiModelProperty(value = "商品规格属性值，多规格时用英文逗号拼接")
    private String sku;
}
