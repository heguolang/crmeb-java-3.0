package com.qxkj.common.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
@ApiModel(value="CreateOrderH5SceneInfoDetailVo对象", description="上报支付的场景信息详情")
public class CreateOrderH5SceneInfoDetailVo {
    public CreateOrderH5SceneInfoDetailVo() {
    }

    public CreateOrderH5SceneInfoDetailVo(String url, String name) {
        this.wap_url = url;
        this.wap_name = name;
    }

    @ApiModelProperty(value = "场景类型", required = true)
    private String type = "Wap";

    @ApiModelProperty(value = "WAP网站URL地址", required = true)
    private String wap_url;

    @ApiModelProperty(value = "WAP 网站名", required = true)
    private String wap_name;
}
