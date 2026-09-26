package com.qxkj.common.response.pagelayout;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
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
@ApiModel(value = "PageLayoutBottomNavigationResponse对象", description = "页面设计底部导航响应对象")
public class PageLayoutBottomNavigationResponse implements Serializable {

    private static final long serialVersionUID = 8350218800271787826L;

    @ApiModelProperty(value = "底部导航")
    private List<HashMap<String, Object>> bottomNavigationList;

    @ApiModelProperty(value = "是否自定义")
    private String isCustom;

}
