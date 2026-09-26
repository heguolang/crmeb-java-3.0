package com.qxkj.common.vo;

import com.baomidou.mybatisplus.annotation.TableId;
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
@ApiModel(value="SystemGroupDataSignConfigVo对象", description="签到记录")
public class SystemGroupDataSignConfigVo implements Serializable {

    private static final long serialVersionUID=1L;

    @TableId(value = "id")
    private Integer id;

    @ApiModelProperty(value = "显示文字")
    private String title;

    @ApiModelProperty(value = "第几天")
    private Integer day;


    @ApiModelProperty(value = "积分")
    private Integer integral;

    @ApiModelProperty(value = "经验")
    private Integer experience;




}
