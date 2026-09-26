package com.qxkj.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@ApiModel(value="StoreProductReplayCountResponse对象", description="产品评价数量和好评度")
public class StoreProductReplayCountResponse implements Serializable {

    private static final long serialVersionUID=1L;

    public StoreProductReplayCountResponse() {}
    public StoreProductReplayCountResponse(Long sumCount, Long goodCount, Long inCount, Long poorCount, String replyChance, Integer replyStar) {
        this.sumCount = sumCount;
        this.goodCount = goodCount;
        this.inCount = inCount;
        this.poorCount = poorCount;
        this.replyChance = replyChance;
        this.replyStar = replyStar;
    }

    @ApiModelProperty(value = "评论总数")
    private Long sumCount;

    @ApiModelProperty(value = "好评总数")
    private Long goodCount;

    @ApiModelProperty(value = "中评总数")
    private Long inCount;

    @ApiModelProperty(value = "差评总数")
    private Long poorCount;

    @ApiModelProperty(value = "好评率")
    private String replyChance;

    @ApiModelProperty(value = "评分星数")
    private Integer replyStar;


}
