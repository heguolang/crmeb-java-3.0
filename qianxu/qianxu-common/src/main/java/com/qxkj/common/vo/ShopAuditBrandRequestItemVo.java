package com.qxkj.common.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import javax.validation.constraints.NotBlank;

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
public class ShopAuditBrandRequestItemVo {

    /** 营业执照或组织机构代码证，图片url/media_id */
    @NotBlank(message = "营业执照或组织机构代码证不能为空")
    private String license;

    /** 品牌信息 */
    @TableField(value = "brand_info")
    private ShopAuditBrandRequestItemDataVo brandInfo;
}
