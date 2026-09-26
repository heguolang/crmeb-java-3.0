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
@ApiModel(value="ExpressSheetVo对象", description="系统电子面单对象")
public class ExpressSheetVo {

    public ExpressSheetVo() {
    }

    public ExpressSheetVo(Integer exportId, String exportCom, String exportTempId, String exportToName, String exportToTel, String exportToAddress, String exportSiid, Integer exportOpen) {
        this.exportId = exportId;
        this.exportCom = exportCom;
        this.exportTempId = exportTempId;
        this.exportToName = exportToName;
        this.exportToTel = exportToTel;
        this.exportToAddress = exportToAddress;
        this.exportSiid = exportSiid;
        this.exportOpen = exportOpen;
    }

    @ApiModelProperty(value = "快递公司简称，物流、电子面单开通必填")
    private Integer exportId;

    @ApiModelProperty(value = "快递公司简称，物流、电子面单开通必填")
    private String exportCom;

    @ApiModelProperty(value = "快递公司模板Id、电子面单开通必填")
    private String exportTempId;

    @ApiModelProperty(value = "快递面单发货人姓名，物流、电子面单开通必填")
    private String exportToName;

    @ApiModelProperty(value = "快递面单发货人电话，物流、电子面单开通必填")
    private String exportToTel;

    @ApiModelProperty(value = "发货人详细地址，物流、电子面单开通必填")
    private String exportToAddress;

    @ApiModelProperty(value = "电子面单打印机编号，物流、电子面单开通必填")
    private String exportSiid;

    @ApiModelProperty(value = "电子面单是否开启")
    private Integer exportOpen;

}
