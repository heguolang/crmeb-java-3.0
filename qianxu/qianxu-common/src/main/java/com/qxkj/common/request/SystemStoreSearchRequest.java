package com.qxkj.common.request;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import java.io.Serializable;
import java.util.Date;

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
@TableName("eb_system_store")
@ApiModel(value="SystemStoreSearchRequest对象", description="提货点搜索")
public class SystemStoreSearchRequest implements Serializable {

    private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "搜索关键字，支持 门店名称|简介|手机号码||省市区|详细地址")
    private String keywords;

    @ApiModelProperty(value = "状态，0隐藏，1显示，2回收站", example = "1")
    @Range(min = 0, max = 2, message = "未知的状态")
    private int status;

}
